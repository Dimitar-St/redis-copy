package commands;

import storage.Storage;
import storage.Value;

public class Set implements ICommand{
    private final Storage storage;

    public Set(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute(String[] payload) {
        if (payload.length > 2) {
            Options options = Options.initialize(payload[2], payload[3]);

            this.storage.set(payload[0], payload[1], options);

            return "+OK\r\n";
        }

        this.storage.set(payload[0], new Value<String>(payload[1]));

        return "+OK\r\n";
    }
}
