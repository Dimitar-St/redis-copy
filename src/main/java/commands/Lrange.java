package commands;

import storage.Storage;
import storage.Value;

import java.util.List;

public class Lrange implements ICommand {
    private final Storage storage;

    public Lrange(Storage storage) {
       this.storage = storage;
    }

    @Override
    public String execute(String[] payload) {
        String key = payload[0];
        Value value = this.storage.get(key);

        if (value == null) {
            return "*\r\n";
        }

        List<String> content = (List<String>) value.getValue();
        int start = Integer.valueOf(payload[1]);
        int end = payload.length > 2 ? Integer.valueOf(payload[2]) : content.size();

        StringBuilder result = new StringBuilder("*" + String.valueOf(end-start) + "\r\n");

        for (int i = start; i <= end; i++) {
            StringBuilder element = new StringBuilder();

            element.append("$");
            element.append(content.get(i).length());
            element.append("\r\n");
            element.append(content.get(i));
            element.append("\r\n");

            result.append(element);
        }


        return result.toString();
    }
}
