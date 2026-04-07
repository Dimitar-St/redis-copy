package commands;

import storage.Storage;
import storage.Value;

import java.util.ArrayList;
import java.util.List;

public class Rpush implements ICommand {
    private final Storage storage;

    public Rpush(Storage storage) {
       this.storage = storage;
    }

    @Override
    public String execute(String[] payload) {
        String key = payload[0];
        if (this.storage.get(key) == null) {
            List<String> values = new ArrayList<>();
            values.add(payload[1]);
            this.storage.set(key, new Value<>(values));
        } else {
            List<String> values = (List<String>) this.storage.get(key);
            values.add(payload[1]);
            this.storage.set(key, new Value(values));
        }

        return "(integer) " + this.storage.get(key).length();
    }
}
