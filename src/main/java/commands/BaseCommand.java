package commands;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public abstract class BaseCommand implements ICommand {
    String[] arguments;
    Instant elapsedTime = null;

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
    public String[] getArguments() {
     return arguments;
    }

    public boolean isExpired() {
        if (elapsedTime != null) {
            Instant now = Instant.now();

//            System.out.println("Now" + now);
//            System.out.println("Elapsed time" + elapsedTime);

            if (now.isAfter(elapsedTime)) {
                 return true;
            }
            return false;
        }

//        Arrays.stream(arguments).forEach(System.out::println);

//        if (arguments.length > 1) {
//            System.out.println("checking");
            double timeout = Double.parseDouble(arguments[1]);
//            System.out.println("Now: " + Instant.now());

            elapsedTime = Instant.now().plus((long) (timeout * 100), ChronoUnit.MILLIS);
//        }

        return false;
    }

}
