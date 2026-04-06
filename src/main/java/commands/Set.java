package commands;

import storage.Storage;

public class Set implements ICommand{
    private final Storage storage;

    public Set(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute(String payload) {
        System.out.println(payload);
        return "";
    }
}
