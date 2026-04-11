package eventLoop;

import commands.BaseCommand;
import commands.CommandFactory;
import commands.ICommand;
import parsers.IParser;
import parsers.ParserFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
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

    public static EventLoop initialize(int port) {
        EventLoop eventLoop = null;

        try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
            Selector selector = Selector.open();

            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.configureBlocking(false);
            serverSocket.socket().setSoTimeout(1);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            eventLoop = new EventLoop();

            eventLoop.serverSocketChannel = serverSocket;
            eventLoop.selector = selector;
            eventLoop.parserFactory = new ParserFactory();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }

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
            System.out.println("tuk");
            selector.select();
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeySet.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                if (key.isAcceptable()) {
                    register(selector, this.serverSocketChannel);
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
                    String dataStructure = command.getArguments()[0];



//                    Map<String, Queue<SocketChannel>> currentWaitingClients = waitingClients.get(dataStructure);
//                    if (currentWaitingClients != null) {
//                        currentWaitingClients.forEach((commandKey, queue) -> {
//                            BaseCommand waitingCommand = CommandFactory.initialize().newCommand(commandKey);
//
//                            String response = waitingCommand.execute();
//
//                            if (command.isBlocking()) {
//                                if (response.equals("not present")) {
//                                    return;
//                                }
//                            }
//
//                            while (!queue.isEmpty()) {
//                                ByteBuffer responseMessage = ByteBuffer.wrap(response.getBytes());
//                                SocketChannel currSocket = queue.poll();
//
//                                while (responseMessage.hasRemaining()) {
//                                    try {
//                                        currSocket.write(responseMessage);
//                                        currSocket.close();
//                                    } catch (IOException e) {
//                                        throw new RuntimeException(e);
//                                    }
//                                }
//                            }
//                        });
//                    }

                    String response = command.execute();


//                    if (command.isBlocking()) {
//                        if (response.equals("not present")) {
//                            Queue<SocketChannel> queue = new ArrayDeque();
//                            queue.add(clientSocket);
//                            Map<String, Queue<SocketChannel>> commandQueue = new HashMap<>();
//                            commandQueue.put("BLOP", queue);
//                            waitingClients.put(dataStructure, commandQueue);
//                            continue;
//                        }
//                    }


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
