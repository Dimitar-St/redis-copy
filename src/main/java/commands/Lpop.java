package commands;

import storage.Storage;
import storage.Value;

import java.util.List;

public class Lpop implements ICommand {
    private final Storage storage;

    public Lpop(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute(String[] payload) {
        String key = payload[0];

        Value value = this.storage.get(key);

        if (value == null) {
            return "$-1\r\n";
        }

        List<String> list = (List<String>) value.getValue();

        if (payload.length >= 2) {
            int countToRemove = Integer.parseInt(payload[1]);

            StringBuilder result = new StringBuilder();
            result.append("*")
                    .append(countToRemove)
                    .append("\r\n");

            for (int i = 0; i < countToRemove; i++) {
                String element = list.removeFirst();

                result.append("$")
                        .append(element.length())
                        .append("\r\n")
                        .append(element)
                        .append("\r\n");
            }

            return result.toString();
        }

        String element = list.remove(0);


        return "$" + element.length() + "\r\n" + element + "\r\n";
    }
}
