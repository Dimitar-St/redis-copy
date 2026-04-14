package commands;

import eventLoop.WaitingClient;
import eventLoop.WaitingClientManager;
import storage.Storage;
import storage.Value;

import java.util.List;

public class Blpop extends BaseCommand {
    private final Storage storage;
    private final WaitingClientManager blockingManager;

    public Blpop(Storage storage, WaitingClientManager blockingManager) {
        this.storage = storage;
        this.blockingManager = blockingManager;
    }

    @Override
    public String execute() {
        this.setTime();
        String key = arguments[0];

        StringBuilder result = new StringBuilder();

        Value value = this.storage.get(key);
        if (value != null) {
            List<String> list = (List<String>) value.getValue();

            if (list.isEmpty()) {
                return "not present";
            }

            String element = list.removeFirst();

            result.append("*")
                    .append(2)
                    .append("\r\n");

            result.append("$")
                    .append(key.length())
                    .append("\r\n")
                    .append(key)
                    .append("\r\n");

            result.append("$")
                    .append(element.length())
                    .append("\r\n")
                    .append(element)
                    .append("\r\n");


            if (list.isEmpty()) {
                this.storage.remove(key);
            } else {
                this.storage.set(key, new Value<List<String>>(list));
            }
            System.out.println("here");

            return result.toString();
        }

        this.blockingManager.addClient(this, "not present", this.connection);

        return "not present";
    }

    @Override
    public boolean isBlocking() {
        return true;
    }
}
