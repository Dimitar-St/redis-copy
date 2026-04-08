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
            return ":$-1\r\n";
        }

        List<String> list = (List<String>) value.getValue();

        String element = list.remove(0);


        return ":&" + element.length() + "\r\n" + element + "\r\n";
    }
}
