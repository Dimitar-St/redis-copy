import javax.imageio.stream.IIOByteBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    //  Uncomment the code below to pass the first stage
        int port = 6379;
        try {
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.configureBlocking(false);


          while(true) {
              try(SocketChannel clientSocket = serverSocket.accept()) {
                  System.out.println("Socket accepted");

                  byte[] data = new byte[14];
                  ByteBuffer buffer = ByteBuffer.wrap(data, 0, 14);
                  while (clientSocket.read(buffer) != -1) {
                      String pong = "+PONG\r\n";
                      ByteBuffer responseMessage = ByteBuffer.wrap(pong.getBytes());
                      clientSocket.write(responseMessage);
                  }
              }
          }

        } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
        }
  }
}
