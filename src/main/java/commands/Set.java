package commands;

import storage.Storage;
import storage.Value;

public class Set extends BaseCommand  {
    private final Storage storage;

    public Set(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute() {
        if (this.arguments.length > 2) {
            Options options = Options.initialize(this.arguments[2], this.arguments[3]);

            this.storage.set(this.arguments[0], this.arguments[1]);

            return "+OK\r\n";
        }

        this.storage.set(this.arguments[0], new Value<String>(this.arguments[1]));

        return "+OK\r\n";
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
