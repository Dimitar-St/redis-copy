package commands;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public abstract class BaseCommand implements ICommand {
    String[] arguments;
    LocalDateTime elapsedTime = null;

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
    public String[] getArguments() {
     return arguments;
    }

    public boolean isExpired() {
        if (elapsedTime != null) {
            LocalDateTime now = LocalDateTime.now();

            System.out.println("checking");
            if (now.isAfter(elapsedTime)) {

                 return true;
            }
        }
        if (arguments.length > 2) {
            System.out.println("checking");
            double timeout = Double.parseDouble(arguments[1]);

            elapsedTime = LocalDateTime.now().plus((long) (timeout / 1000), ChronoUnit.MILLIS);
        }

        return false;
    }

}
