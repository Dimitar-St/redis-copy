import eventLoop.EventLoop;
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

public class Main {
  public static void main(String[] args) throws IOException {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    //  Uncomment the code below to pass the first stage
    int port = 6379;

    EventLoop loop = EventLoop.initialize(port);

    if (loop == null) {
        throw new RuntimeException("Can't initialize EventLoop");
    }

    loop.run();
  }

}
