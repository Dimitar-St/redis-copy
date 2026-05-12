package commands;

import storage.Storage;
import storage.Value;

public class Incr extends BaseCommand {
    private Storage storage;
    public Incr(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute() {
        String key = this.arguments[0];
        Value value = this.storage.get(key);
        if (value == null) {
            return "$-1\r\n";
        }

        Integer content = Integer.valueOf((String) value.getValue());
        content++;

        this.storage.set(key, new Value(content.toString()));

        return "";
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
