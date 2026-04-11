package eventLoop;

import commands.BaseCommand;
import commands.CommandFactory;
import commands.ICommand;
import parsers.IParser;
import parsers.ParserFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class EventLoop {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ParserFactory parserFactory;

    private Map<String, Map<String, Queue<SocketChannel>>> waitingClients = new HashMap<>();

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

                    String dataStructure = command.getArguments()[0];
                    Map<String, Queue<SocketChannel>> currentWaitingClients = waitingClients.get(dataStructure);
                    if (currentWaitingClients != null) {
                        currentWaitingClients.forEach((commandKey, queue) -> {
                            BaseCommand waitingCommand = CommandFactory.initialize().newCommand(commandKey);

                            String response2 = waitingCommand.execute();

                            if (waitingCommand.isBlocking()) {
                                if (response2.equals("not present")) {
                                    return;
                                }
                            }

                            while (!queue.isEmpty()) {
                                ByteBuffer responseMessage = ByteBuffer.wrap(response2.getBytes());
                                SocketChannel currSocket = queue.poll();

                                while (responseMessage.hasRemaining()) {
                                    try {
                                            System.out.println("Writing to socket connection");
                                            currSocket.write(responseMessage);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                try {
                                    assert currSocket != null;
                                    currSocket.close();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    }


                    if (command.isBlocking()) {
                        if (response.equals("not present")) {
                            Map<String, Queue<SocketChannel>> cl = waitingClients.get(dataStructure);
                            if (cl == null) {
                                Queue<SocketChannel> queue = new ArrayDeque();
                                queue.add(clientSocket);
                                Map<String, Queue<SocketChannel>> commandQueue = new HashMap<>();
                                commandQueue.put("BLPOP", queue);
                                waitingClients.put(dataStructure,  commandQueue);
                            }

                            if (cl != null) {
                               Queue cq =  cl.get("BLPOP");
                               cq.add(clientSocket);
                            }

                            continue;
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
