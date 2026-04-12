package commands;

import storage.Storage;
import storage.Value;

import java.util.List;

public class Blpop extends BaseCommand {
    private final Storage storage;

    public Blpop(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute() {
        String key = arguments[0];

        StringBuilder result = new StringBuilder();

        Value value = this.storage.get(key);
        if (value != null) {
            List<String> list = (List<String>) value.getValue();

            if (list.isEmpty()) {
                if (isExpired()) {
                    return "*-1\r\n";
                }
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
            if (isExpired()) {
                System.out.println("list is empty");
                return "*-1\r\n";
            }
            return "not present";
        }

        return result.toString();
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
