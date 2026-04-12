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
        if (isExpired()) {
            return "*-1\r\n";
        }
        String key = arguments[0];

        StringBuilder result = new StringBuilder();

        Value value = this.storage.get(key);

        if (value != null) {
            List<String> list = (List<String>) value.getValue();

            if (list.isEmpty()) {
                return "not present";
            }

            String element = list.removeFirst();

            result.append("*")
                    .append(2)
                    .append("\r\n");

            result.append("$")
                    .append(key.length())
                    .append("\r\n")
                    .append(key)
                    .append("\r\n");

            result.append("$")
                    .append(element.length())
                    .append("\r\n")
                    .append(element)
                    .append("\r\n");

            this.storage.set(key, new Value<List<String>>(list));
        } else {
            return "not present";
        }

        if (isExpired()) {
            return "*-1\r\n";
        }

        return result.toString();
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
