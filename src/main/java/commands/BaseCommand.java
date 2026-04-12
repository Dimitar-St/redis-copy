package commands;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public abstract class BaseCommand implements ICommand {
    String[] arguments;
    public Instant elapsedTime = null;

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public String[] getArguments() {
        return arguments;
    }

    public boolean isExpired() {
        if (elapsedTime != null) {
            Instant now = Instant.now();

            if (now.isAfter(elapsedTime)) {
                return true;
            }
            return false;
        }

        double timeout = Double.parseDouble(arguments[1]);
        System.out.println("Now before: " + Instant.now());

        elapsedTime = Instant.now().plus((long) (timeout * 1000), ChronoUnit.MILLIS);

        return false;
    }

}
