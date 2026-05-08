package commands;

import storage.Storage;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.*;
import java.util.stream.Collectors;

public class Xread extends BaseCommand {
    private final Storage storage;

    public Xread(Storage storage) {
        this.storage = storage;
    }
    public class Order {
        private String id;
        private double price;

        public Order(String id, double price) {
            this.id = id;
            this.price = price;
        }

        public double getPrice() {
            return price;
        }
    }

    private List<Pair> readMultipleStreams() {
        int remaining = arguments.length - 2;

        if (remaining % 2 != 0) {
            throw new IllegalArgumentException("arguments are invalid");
        }

        int streamCount = remaining / 2;
        List<Pair> pairs = new ArrayList<>();

        for (int i = 0; i < streamCount; i++) {
            String streamKey = arguments[2 + i];
            StreamID id = StreamID.parse(arguments[2 + streamCount + i]);

            pairs.add(new Pair(streamKey, id));
        }

        return  pairs;
    }

    record Pair (String streamKey, StreamID streamID) {}

    @Override
    public String execute() {
        if (this.arguments[1].toLowerCase().equals("streams")) {
            List<Pair> pairs = this.readMultipleStreams();
            StringBuilder result = new StringBuilder();

            result.append("*");
            result.append(pairs.size());
            result.append("\r\n");

            for (int i = 0; i < pairs.size(); i++) {
                Pair pair = pairs.get(i);
                StreamStore store = this.storage.getStreamStore(pair.streamKey);

                if (store == null) {
                    return "+none\r\n";
                }


                SortedMap<StreamID, Block> map = store.getFrom(pair.streamID);

                result.append(this.parseResultString(pair.streamKey, map));
            }

            return result.toString();
        }

        String streamKey = this.arguments[1];
        StreamStore store = this.storage.getStreamStore(streamKey);

        if (store == null) {
            return "+none\r\n";
        }

        StreamID streamID = StreamID.parse(this.arguments[2]);

        SortedMap<StreamID, Block> map = store.getFrom(streamID);

        StringBuilder result = new StringBuilder();

        result.append("*1\r\n");

        result.append(this.parseResultString(streamKey, map));
        return result.toString();
    }


    private String parseResultString (String streamKey,  SortedMap<StreamID, Block> map) {
        StringBuilder result = new StringBuilder();

        result.append("*2\r\n");

        result.append("$");
        result.append(streamKey.length());
        result.append("\r\n");
        result.append(streamKey);
        result.append("\r\n");

        result.append("*");
        result.append(map.size());
        result.append("\r\n");

        for (StreamID key : map.keySet()) {
            String keyString = key.toString();

            result.append("*2\r\n");

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
