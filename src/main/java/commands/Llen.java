package commands;

import storage.Storage;
import storage.Value;

import java.util.List;

public class Llen implements ICommand {

    private final Storage storage;

    public Llen(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute(String[] payload) {
        String key = payload[0];

        Value value = this.storage.get(key);

        if (value == null) {
            return ":0\r\n";
        }

        List<String> list = (List<String>) value.getValue();

        return ":" + list.size() + "\r\n";
    }
}
