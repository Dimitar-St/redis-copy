package commands;

import storage.Storage;
import storage.Value;

import java.util.List;

public class Lrange extends BaseCommand {
    private final Storage storage;

    public Lrange(Storage storage) {
       this.storage = storage;
    }

    @Override
    public String execute() {
        String key = arguments[0];
        Value value = this.storage.get(key);

        if (value == null) {
            return "*0\r\n";
        }

        List<String> content = (List<String>) value.getValue();
        int start = Integer.parseInt(arguments[1]);

        int length = content.size() - 1;

        int end = arguments.length > 2 ? Integer.parseInt(arguments[2]) : length;
        end = end > length ? length : end;

        if (start < 0) {
            start = content.size() + start;
            start = start < 0 ? 0 : start;
        }

        if (end < 0) {
            end = content.size() + end;
        }

        if (end < start) {
            return "*0\r\n";
        }

        StringBuilder result = new StringBuilder("*" + String.valueOf(end-start+1) + "\r\n");

        for (int i = start; i <= end; i++) {
            StringBuilder element = new StringBuilder();

            element.append("$")
            .append(content.get(i).length())
            .append("\r\n")
            .append(content.get(i))
            .append("\r\n");

            result.append(element);
        }


        return result.toString();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
