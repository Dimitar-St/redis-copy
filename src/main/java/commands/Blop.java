package commands;

import storage.Storage;
import storage.Value;

import java.time.LocalDateTime;
import java.util.List;

public class Blop extends BaseCommand {
    private final Storage storage;

    public Blop(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute() {
        String key = arguments[0];

        LocalDateTime elapsedTime = null;
        if (arguments.length > 2) {
            int timeout = Integer.parseInt(arguments[1]);

            elapsedTime = LocalDateTime.now().plusSeconds(timeout);
        }

        StringBuilder result = new StringBuilder();
        result.append("*")
                .append("2")
                .append("\r\n");

            Value value = this.storage.get(key);
            if (elapsedTime != null) {
                LocalDateTime now = LocalDateTime.now();

                if (now.isAfter(elapsedTime)) {
                    if (value == null) {
                        return "*-1\r\n";
                    }
                }
            }

            if (value != null) {
                List<String> list = (List<String>) value.getValue();

                String element = list.removeFirst();

                result.append("$")
                        .append(element.length())
                        .append("\r\n")
                        .append(element)
                        .append("\r\n");
            } else {
                return "not present";
            }

        return result.toString();
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
