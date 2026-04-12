package eventLoop;

import commands.BaseCommand;
import commands.CommandFactory;
import parsers.IParser;
import parsers.ParserFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class EventLoop {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ParserFactory parserFactory;

    private Map<String, Map<String, Stack<SocketChannel>>> waitingClients = new HashMap<>();

    private EventLoop() {};

    public static EventLoop initialize(int port) throws IOException {
        EventLoop eventLoop = null;

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
            Selector selector = Selector.open();

            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.configureBlocking(false);
            ServerSocket sv = serverSocket.socket();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            eventLoop = new EventLoop();

            eventLoop.serverSocketChannel = serverSocket;
            eventLoop.selector = selector;
            eventLoop.parserFactory = new ParserFactory();

            System.out.println("Socket server is listening on : " + port + " " + sv.toString());

        return eventLoop;
    }

    private void register(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {
        SocketChannel channel = serverSocketChannel.accept();
        if (channel == null)
            return;
        channel.configureBlocking(false);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.register(selector, SelectionKey.OP_READ, buffer);
    }

    public void run () throws IOException {
        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeySet.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                if (key.isAcceptable()) {
                    register(this.selector, this.serverSocketChannel);
                }

                if (key.isReadable()) {
                    SocketChannel clientSocket = (SocketChannel) key.channel();
                    if (clientSocket == null)
                        continue;

                    ByteBuffer buffer = (ByteBuffer) key.attachment();

                    int bytesRead = clientSocket.read(buffer);

                    if (bytesRead == -1) {
                        System.out.println("closing client connection");
                        clientSocket.close();
                        continue;
                    }

                    if (bytesRead == 0) {
                        continue;
                    }

                    buffer.flip(); // 🔥 CRITICAL

                    IParser parser = parserFactory.newParser(buffer);
                    BaseCommand command = parser.parse(buffer);

                    String response = command.execute();

                    if (command.getArguments().length > 0) {
                        String dataStructure = command.getArguments()[0];
                        Map<String, Stack<SocketChannel>> currentWaitingClients = waitingClients.get(dataStructure);
                        if (currentWaitingClients != null) {
                            currentWaitingClients.forEach((commandKey, stack) -> {
                                BaseCommand waitingCommand = CommandFactory.initialize().newCommand(commandKey);

                                while (!stack.empty()) {
                                    String response2 = waitingCommand.execute();

                                    if (waitingCommand.isBlocking()) {
                                        if (response2.equals("not present")) {
                                            System.out.println("No data present yet.");
                                            return;
                                        }
                                    }
                                    ByteBuffer responseMessage = ByteBuffer.wrap(response2.getBytes());
                                    SocketChannel currSocket = stack.pop();

                                    while (responseMessage.hasRemaining()) {
                                        try {
                                            currSocket.write(responseMessage);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }

                                    try {
                                        assert currSocket != null;
                                        currSocket.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                                        selector.wakeup();
                                    } catch (ClosedChannelException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        }

                        if (command.isBlocking()) {
                            if (response.equals("not present")) {
                                Map<String, Stack<SocketChannel>> cl = waitingClients.get(dataStructure);
                                if (cl == null) {
                                    Stack<SocketChannel> queue = new Stack<>();
                                    queue.add(clientSocket);
                                    Map<String, Stack<SocketChannel>> commandQueue = new HashMap<>();
                                    commandQueue.put("BLPOP", queue);
                                    waitingClients.put(dataStructure, commandQueue);
                                }

                                if (cl != null) {
                                    Stack<SocketChannel> cq = cl.computeIfAbsent("BLPOP", k -> new Stack<>());
                                    cq.add(clientSocket);

                                    key.cancel();
                                }

                                continue;
                            }
                        }
                    }


                    ByteBuffer responseMessage = ByteBuffer.wrap(response.getBytes());

                    while (responseMessage.hasRemaining()) {
                        clientSocket.write(responseMessage);
                    }

                    buffer.clear();
                }

                iterator.remove();
            }

        }
    }
}
