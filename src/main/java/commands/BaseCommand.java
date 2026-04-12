package commands;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;

public abstract class BaseCommand implements ICommand {
    String name;
    String[] arguments;
    public Instant elapsedTime = null;

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getArguments() {
        return arguments;
    }

    public boolean isExpired() {
        Instant now = Instant.now();

        if (now.isAfter(elapsedTime)) {
            return true;
        }
        return false;
    }

    public void setTime() {
        if (elapsedTime != null) {
            return;
        }
        double timeout = Double.parseDouble(arguments[1]);
        elapsedTime = Instant.now().plus((long) (timeout * 1000), ChronoUnit.MILLIS);
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
