package commands;

import storage.Storage;
import storage.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Xadd extends BaseCommand {
    private final Storage storage;

    public Xadd(Storage storage) {
        this.storage = storage;
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
        }


        String response = store.put(streamID, Arrays.stream(arguments, 2, arguments.length).toArray(String[]::new));
        if (!response.isEmpty()) {
            return response;
        }

        return "$" + this.arguments[1].length() + "\r\n" + streamID.getTimestamp() + "-" + streamID.counter() + "\r\n";
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
