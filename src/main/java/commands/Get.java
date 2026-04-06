package commands;

import storage.Storage;

public class Get implements ICommand {
    private final Storage storage;

    public Get(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute(String[] payload) {
        String val = this.storage.get(payload[0]);
        if (val == null) {
            return "$-1\r\n";
        }

        return "$" + val.length() + "\r\n" + this.storage.get(payload[0]) + "\r\n";
    }
}
