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
        List<String> values = new ArrayList<>();
        if (this.storage.get(key) == null) {
            values.add(payload[1]);
            this.storage.set(key, new Value<>(values));
        } else {
            System.out.println(this.storage.get(key).getClass().getName());
            values = (List<String>) this.storage.get(key);
            values.add(payload[1]);
            this.storage.set(key, new Value<>(values));
        }

        return ":" + values.size() + "\r\n";
    }
}
