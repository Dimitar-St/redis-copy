package commands;

import eventLoop.BlockingClientManager;
import eventLoop.WaitingClient;
import storage.Storage;
import storage.Value;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Xadd extends BaseCommand {
    private final Storage storage;
    private final BlockingClientManager blockingManager;

    public Xadd(Storage storage, BlockingClientManager blockingManager) {
        this.storage = storage;
        this.blockingManager = blockingManager;
    }

    @Override
    public String execute() {
        String streamKey = this.arguments[0];

        StreamID streamID = StreamID.parse(this.arguments[1]);
        if (this.arguments[1].equals("0-0")) {
            return "-ERR The ID specified in XADD must be greater than 0-0\r\n";
        }

        StreamStore store = this.storage.getStreamStore(streamKey);
        if (store == null) {
            store = new StreamStore();
            this.storage.setStreamStore(streamKey, store);
        }


        streamID = store.generateId(streamID);
        String response = store.put(streamID, Arrays.stream(arguments, 2, arguments.length).toArray(String[]::new));
        if (!response.isEmpty()) {
            return response;
        }

        String idString = streamID.toString();

        Optional<WaitingClient> waiter = blockingManager.tryResolve(streamKey);

        waiter.ifPresent(blockingManager::respondValue);

        return "$" + idString.length() + "\r\n" +  idString + "\r\n";
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
