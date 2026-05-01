package commands;

import storage.Storage;

import java.util.List;
import java.util.SortedMap;

public class Xrange extends BaseCommand {

    private final Storage storage;

    public Xrange(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute() {
        String streamKey = this.arguments[0];
        StreamStore store = this.storage.getStreamStore(streamKey);

        if (store == null) {
            return "+none\r\n";
        }

        StreamID startKey = StreamID.parse(this.arguments[1]);
        StreamID endKey = StreamID.parse(this.arguments[2]);

        SortedMap<StreamID, Block> map = this.storage.getRange(startKey, endKey);

        StringBuilder result = new StringBuilder();


        for (int i = 0; i < map.size(); i++) {
            result.append("*");
            result.append(2);
            result.append("\r\n");
        }

        for (StreamID key: map.keySet()) {
            String keyString = key.toString();

            result.append("$");
            result.append(keyString.length());
            result.append("\r\n");
            result.append(keyString);
            result.append("\r\n");


            Block block = map.get(key);
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
        }

        return result.toString();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
