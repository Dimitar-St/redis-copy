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
    private BlockingClientManager manager;

    private EventLoop() {
    }

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

        // NOTE: Does not look good
        eventLoop.manager = CommandFactory.initialize().blockingManager;

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

    public void run() throws IOException {
        while (true) {
            long now = System.currentTimeMillis();
            manager.handleTimeouts(now);

            long timeout = manager.nextDeadline(now);

            if (timeout == -1) {
                // no blocked clients: wait indefinitely for network activity
                selector.select();
            } else if (timeout == 0) {
                // timeout already due: don't block
                System.out.println("timeed");
                selector.selectNow();
            } else {
                // wait until the next deadline
                selector.select(timeout);
            }

            now = System.currentTimeMillis();   // recompute
            manager.handleTimeouts(now);

            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeySet.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                if (key.isAcceptable()) {
                    register(this.selector, this.serverSocketChannel);
                }

                if (key.isReadable()) {
                    SocketChannel clientSocket = (SocketChannel) key.channel();
                    if (clientSocket == null) {
                        continue;
                    }

                    ByteBuffer buffer = (ByteBuffer) key.attachment();

                    int bytesRead = clientSocket.read(buffer);

                    if (bytesRead == -1) {
                        System.out.println("closing client connection");
                        clientSocket.close();
                        continue;
                    }

                    buffer.flip();

                    IParser parser = parserFactory.newParser(buffer);
                    BaseCommand command = parser.parse(buffer);

                    command.connection = clientSocket;
                    command.selectionKey = key;

                    System.out.println("Creating new command: " + command.id.toString());

                    String response = command.execute();

                    if (response.equals("not present")) {
                        buffer.clear();          // VERY IMPORTANT
                        iterator.remove();
                        continue;
                    }


                    ByteBuffer responseMessage = ByteBuffer.wrap(response.getBytes());

                    while (responseMessage.hasRemaining()) {
                        clientSocket.write(responseMessage);
                    }

                    buffer.clear();
                    iterator.remove();
                }

            }
        }
    }
}
