package eventLoop;

import commands.BaseCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.UUID;

public class WaitingClient {
    public boolean completed;
    BaseCommand command;
    SocketChannel connection;
    public UUID id;

    public WaitingClient(BaseCommand command, SocketChannel connection) {
        id = UUID.randomUUID();
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WaitingClient that = (WaitingClient) o;
        return completed == that.completed && Objects.equals(command, that.command) && Objects.equals(connection, that.connection) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(completed, command, connection, id);
    }
}
