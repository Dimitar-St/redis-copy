package commands;

import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;

public abstract class BaseCommand implements ICommand {
    String name;
    String[] arguments;
    public long timeout = 0;
    public SocketChannel connection;
    public SelectionKey selectionKey;

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getArguments() {
        return arguments;
    }


    public void setTime() {
        double timeout = Double.parseDouble(arguments[1]);
        this.timeout = System.currentTimeMillis() + (long) (timeout * 1000);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BaseCommand that = (BaseCommand) o;
        return Objects.equals(name, that.name) && Objects.deepEquals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Arrays.hashCode(arguments));
    }
}
