package commands;

import eventLoop.WaitingClient;
import eventLoop.BlockingClientManager;
import storage.Storage;
import storage.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Rpush extends BaseCommand {
    private final Storage storage;
    private final BlockingClientManager blockingManager;

    public Rpush(Storage storage, BlockingClientManager blockingManager) {
       this.storage = storage;
       this.blockingManager = blockingManager;
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
        int currentSize = values.size();

        Optional<WaitingClient> waiter = blockingManager.tryResolve(key);

        if (waiter.isPresent()) {
            blockingManager.respondValue(waiter.get(), key, ":" + values.size() + "\r\n");
        }

        System.out.println(":" + currentSize + "\r\n");
        return ":" + currentSize + "\r\n";
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
