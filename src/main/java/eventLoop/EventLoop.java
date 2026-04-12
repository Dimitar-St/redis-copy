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
import java.time.Instant;
import java.util.*;

public class EventLoop {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ParserFactory parserFactory;

    private Map<String, Map<BaseCommand, Stack<SocketChannel>>> waitingClients = new HashMap<>();

    private EventLoop() {
    }

    ;

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

    private void executePendingCommands() {
        waitingClients.forEach((dataStructure, currentWaitingClients) -> {
            currentWaitingClients.forEach((command, stack) -> {

                while (!stack.empty()) {
                    String response2 = command.execute();

                    if (command.isBlocking()) {
                        if (response2.equals("not present")) {
//                                System.out.println("No data present yet.");
                            continue;
                        }
                    }
                    ByteBuffer responseMessage = ByteBuffer.wrap(response2.getBytes());
                    SocketChannel currSocket = stack.pop();

                    System.out.println(response2);
                    System.out.println("Now: " + Instant.now());
                    System.out.println("Exec: " + command.execTime);
                    System.out.println("Elapsed time: " + command.elapsedTime);

                    while (responseMessage.hasRemaining()) {
                        try {
                            currSocket.write(responseMessage);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        });
    }

    public void run() throws IOException {
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

                    buffer.flip();

                    IParser parser = parserFactory.newParser(buffer);
                    BaseCommand command = parser.parse(buffer);

                    this.executePendingCommands();
                    String response = command.execute();


                    if (command.getArguments().length > 0) {
                        String dataStructure = command.getArguments()[0];
                        if (command.isBlocking()) {
                            if (response.equals("not present")) {
                                Map<BaseCommand, Stack<SocketChannel>> cl = waitingClients.get(dataStructure);
                                if (cl == null) {
                                    Stack<SocketChannel> queue = new Stack<>();
                                    queue.add(clientSocket);
                                    Map<BaseCommand, Stack<SocketChannel>> commandQueue = new HashMap<>();
                                    commandQueue.put(command, queue);
                                    waitingClients.put(dataStructure, commandQueue);
                                    continue;
                                }

                                Stack<SocketChannel> cq = cl.computeIfAbsent(command, k -> new Stack<>());
                                cq.add(clientSocket);

                                key.cancel();

                                continue;
                            }
                        }
                    }


                    System.out.println("Response");
                    System.out.println(response);
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
