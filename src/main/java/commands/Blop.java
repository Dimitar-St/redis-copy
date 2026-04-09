package commands;

import storage.Storage;
import storage.Value;

import java.time.LocalDateTime;
import java.util.List;

public class Blop implements ICommand {
    private final Storage storage;

    public Blop(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute(String[] payload) {
        String key = payload[0];

        LocalDateTime elapsedTime = null;
        if (payload.length > 2) {
            int timeout = Integer.parseInt(payload[1]);

            elapsedTime = LocalDateTime.now().plusSeconds(timeout);
        }

        StringBuilder result = new StringBuilder();
        result.append("*")
                .append("2")
                .append("\r\n");

        while(true) {
            if (elapsedTime != null) {
                LocalDateTime now = LocalDateTime.now();

                if (now.isAfter(elapsedTime)) {
                    if (value == null) {
                        return "*-1\r\n";
                    }


                }
            }

            Value value = this.storage.get(key);
            if (value != null) {
                List<String> list = (List<String>) value.getValue();


                String element = list.removeFirst();

                result.append("$")
                        .append(element.length())
                        .append("\r\n")
                        .append(element)
                        .append("\r\n");            }
                break;
        }




        return result.toString();
    }
}
