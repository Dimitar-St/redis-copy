package commands;

import storage.Storage;
import storage.Value;

import java.util.List;

public class Llen extends BaseCommand {

    private final Storage storage;

    public Llen(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute() {
        String key = arguments[0];

        Value value = this.storage.get(key);

        if (value == null) {
            return ":0\r\n";
        }

        List<String> list = (List<String>) value.getValue();

        return ":" + list.size() + "\r\n";
    }

    @Override
    public boolean isBlocking() {
        return false;
    }


}
