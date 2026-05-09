package eventLoop;

import commands.BaseCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WaitingClient {
    public boolean completed;
    BaseCommand command;
    SocketChannel connection;

    public WaitingClient(BaseCommand command, SocketChannel connection) {
        this.command = command;
        this.connection = connection;
    }

    public void responseWithNull() {
        ByteBuffer nullResponse = ByteBuffer.wrap("*-1\r\n".getBytes());
        while (nullResponse.hasRemaining()) {
            try {
                connection.write(nullResponse);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
