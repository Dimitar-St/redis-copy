import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    //  Uncomment the code below to pass the first stage
        int port = 6379;
        try(ServerSocketChannel serverSocket = ServerSocketChannel.open()){
            Selector selector = Selector.open();

            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.configureBlocking(false);
            serverSocket.socket().setSoTimeout(1);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            ParserFactory parserFactory = new ParserFactory();

          while(true) {
              selector.select();
              Set<SelectionKey> selectionKeySet = selector.selectedKeys();
              Iterator<SelectionKey> iterator = selectionKeySet.iterator();

              while (iterator.hasNext()) {
                  SelectionKey key = iterator.next();

                  if (key.isAcceptable()) {
                        register(selector, serverSocket);
                  }

                  if (key.isReadable()) {
                          SocketChannel clientSocket = (SocketChannel) key.channel();
                          if (clientSocket == null)
                              continue;

                          System.out.println("Socket accepted");

                          byte[] data = new byte[14];
                          ByteBuffer buffer = ByteBuffer.wrap(data, 0, 100);
                          if (clientSocket.read(buffer) != -1) {
                              IParser parser = parserFactory.newParser(buffer);
                              String response = parser.parse(buffer);
                              ByteBuffer responseMessage = ByteBuffer.wrap(response.getBytes());
                              clientSocket.write(responseMessage);
                              buffer.clear();
                              responseMessage.clear();
                          } else {
                              System.out.println("closing client connection");
                              clientSocket.close();
                          }
                  }

                  iterator.remove();
              }

          }

        } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
        }
  }

  private static void register(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {
      SocketChannel channel = serverSocketChannel.accept();
      if (channel == null)
          return;
      channel.configureBlocking(false);
      channel.register(selector, SelectionKey.OP_READ);
  }
}
