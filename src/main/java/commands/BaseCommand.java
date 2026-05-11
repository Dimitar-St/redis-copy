package commands;

import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseCommand implements ICommand {
    String name;
    public UUID id;
    String[] arguments;
    public long timeout = 0;
    public SocketChannel connection;
    public SelectionKey selectionKey;
    public boolean timeless;

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getArguments() {
        return arguments;
    }

    public String getDataStructure() {
        return this.arguments[0];
    }

    public void setTime() {
       setTimeFromIndex(1);
    }

    protected void setTimeFromIndex(int index) {
        double timeout = Double.parseDouble(arguments[index]);

        if (timeout == 0.0) {
            timeless = true;
            return;
        }

        this.timeout = System.currentTimeMillis() + (long) (timeout);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BaseCommand that = (BaseCommand) o;
        return timeout == that.timeout && timeless == that.timeless && Objects.equals(name, that.name) && Objects.deepEquals(arguments, that.arguments) && Objects.equals(connection, that.connection) && Objects.equals(selectionKey, that.selectionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Arrays.hashCode(arguments), timeout, connection, selectionKey, timeless);
    }
}
