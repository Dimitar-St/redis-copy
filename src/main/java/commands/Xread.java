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

        String streamKey = this.arguments[0];
        StreamStore store = this.storage.getStreamStore(streamKey);

        if (store == null) {
            return "+none\r\n";
        }

        StreamID streamID = StreamID.parse(this.arguments[1]);

        Block block = store.get(streamID);

        StringBuilder result = new StringBuilder();

        result.append("*");
        result.append(1);
        result.append("\r\n");


        result.append("*");
        result.append(2);
        result.append("\r\n");

        result.append("$");
        result.append(streamID.toString().length());
        result.append("\r\n");
        result.append(streamID.toString());
        result.append("\r\n");

        List<String> data = block.getData();

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

        return result.toString();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
