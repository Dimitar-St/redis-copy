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
      ParserFactory parserFactory = new ParserFactory();
        try(ServerSocketChannel serverSocket = ServerSocketChannel.open()){
            Selector selector = Selector.open();

            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.configureBlocking(false);
            serverSocket.socket().setSoTimeout(1);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);


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
                      String response = parser.parse(buffer);

                      ByteBuffer responseMessage = ByteBuffer.wrap(response.getBytes());

                      while (responseMessage.hasRemaining()) {
                          clientSocket.write(responseMessage);
                      }

                      buffer.clear();
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
      ByteBuffer buffer = ByteBuffer.allocate(1024);
      channel.register(selector, SelectionKey.OP_READ, buffer);
  }
}
