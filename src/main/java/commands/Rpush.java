package commands;

import storage.Storage;
import storage.Value;

import java.util.ArrayList;
import java.util.List;

public class Rpush extends BaseCommand {
    private final Storage storage;

    public Rpush(Storage storage) {
       this.storage = storage;
    }

    @Override
    public String execute() {
        String key = arguments[0];
        List<String> values = new ArrayList<>();
        if (this.storage.get(key) == null) {
            for (int i = 1; i < arguments.length; i++) {
                values.add(arguments[i]);
            }
            System.out.println("Create a value with name " + key);
            this.storage.set(key, new Value(values));
        } else {
            values = (List<String>) this.storage.get(key).getValue();
            for (int i = 1; i < arguments.length; i++) {
                values.add(arguments[i]);
            }
            this.storage.set(key, new Value<>(values));
        }

        return ":" + values.size() + "\r\n";
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
