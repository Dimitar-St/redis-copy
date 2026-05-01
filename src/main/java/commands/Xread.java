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

        System.out.println("tuk");

        if (store == null) {
            return "+none\r\n";
        }

        StreamID streamID = StreamID.parse(this.arguments[1]);

        Block block = store.get(streamID);

        StringBuilder result = new StringBuilder();

        result.append("*1\r\n");
        result.append("*2\r\n");

        result.append("$");
        result.append(streamKey.length());
        result.append("\r\n");
        result.append(streamKey);
        result.append("\r\n");

        result.append("*1\r\n");

        result.append("*2\r\n");

        String id = streamID.toString();
        result.append("$");
        result.append(id.length());
        result.append("\r\n");
        result.append(id);
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
        System.out.println(result);

        return result.toString();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
