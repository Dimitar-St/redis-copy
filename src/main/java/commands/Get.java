package commands;

import storage.Storage;

public class Get implements ICommand {
    private final Storage storage;

    public Get(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute(String payload) {
        System.out.println(payload);
        return "";
    }
}
