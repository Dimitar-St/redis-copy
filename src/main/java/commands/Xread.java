package commands;

import storage.Storage;

import java.util.List;
import java.util.SortedMap;

public class Xread extends BaseCommand {
    private final Storage storage;

    public Xread(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute() {

        String streamKey = this.arguments[1];
        StreamStore store = this.storage.getStreamStore(streamKey);

        if (store == null) {
            return "+none\r\n";
        }

        StreamID streamID = StreamID.parse(this.arguments[2]);

        SortedMap<StreamID, Block> map = store.getFrom(streamID);

        StringBuilder result = new StringBuilder();

// Top-level: 1 stream
        result.append("*1\r\n");

// Stream wrapper
        result.append("*2\r\n");

// Stream name (NOT key!)
        result.append("$");
        result.append(streamKey.length());
        result.append("\r\n");
        result.append(streamKey);
        result.append("\r\n");

// Entries array
        result.append("*");
        result.append(map.size());
        result.append("\r\n");

// Now iterate entries
        for (StreamID key : map.keySet()) {
            String keyString = key.toString();

            // Entry: [id, fields]
            result.append("*2\r\n");

            // ID
            result.append("$");
            result.append(keyString.length());
            result.append("\r\n");
            result.append(keyString);
            result.append("\r\n");

            Block block = map.get(key);
            List<String> data = block.getData();

            // Field-value array
            result.append("*");
            result.append(data.size());
            result.append("\r\n");

            for (String entry : data) {
                result.append("$");
                result.append(entry.length());
                result.append("\r\n");
                result.append(entry);
                result.append("\r\n");
            }
        }
        System.out.println(result);

        return result.toString();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
