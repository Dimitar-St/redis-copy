import javax.imageio.stream.IIOByteBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

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
                      try (SocketChannel clientSocket = (SocketChannel) key.channel()) {
                          if (clientSocket == null)
                              continue;

                          System.out.println("Socket accepted");

                          byte[] data = new byte[14];
                          ByteBuffer buffer = ByteBuffer.wrap(data, 0, 14);
                          if (clientSocket.read(buffer) == -1) {
                              clientSocket.close();
                              break;
                          }

                          buffer.flip();
                          while (buffer.hasRemaining()) {
                              byte b = buffer.get(); // Reads one byte and advances position
                              System.out.print((char) b);
                          }

                          String pong = "+PONG\r\n";
                          ByteBuffer responseMessage = ByteBuffer.wrap(pong.getBytes());
                          clientSocket.write(responseMessage);
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
