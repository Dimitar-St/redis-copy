package commands;

import java.time.LocalDateTime;

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

            if (now.isAfter(elapsedTime)) {
                 return true;
            }
        }
        if (arguments.length > 2) {
            int timeout = Integer.parseInt(arguments[1]);

            elapsedTime = LocalDateTime.now().plusSeconds(timeout);
        }

        return false;
    }

}
