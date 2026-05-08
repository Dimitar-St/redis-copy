package commands;

import eventLoop.BlockingClientManager;
import storage.Storage;

import java.util.*;

public class Xread extends BaseCommand {
    private final Storage storage;
    private final BlockingClientManager blockingManager;
    private boolean isBlocking;
    private String dataStructure;

    public Xread(Storage storage, BlockingClientManager blockingManager) {
        this.storage = storage;
        this.blockingManager = blockingManager;
        this.isBlocking = false;
    }

    private List<Pair> readMultipleStreams() {
        int argsToRemove = 1;
        if (arguments[0].equals("block")) {
            this.isBlocking = true;
            this.setTimeFromIndex(1);
            argsToRemove = 3;
        }
        int remaining = arguments.length - argsToRemove;

        if (remaining % 2 != 0) {
            throw new IllegalArgumentException("XREAD arguments are invalid");
        }

        int streamCount = remaining / 2;
        List<Pair> pairs = new ArrayList<>();

        for (int i = 0; i < streamCount; i++) {
            String streamKey = arguments[argsToRemove + i];
            StreamID id = StreamID.parse(arguments[argsToRemove + streamCount + i]);

            pairs.add(new Pair(streamKey, id));
        }

        if (pairs.size() == 1) {
            this.dataStructure = pairs.get(0).streamKey;
        }

        return pairs;
    }

    record Pair(String streamKey, StreamID streamID) {
    }

    @Override
    public String execute() {
        List<Pair> pairs = this.readMultipleStreams();
        StringBuilder result = new StringBuilder();

        result.append("*");
        result.append(pairs.size());
        result.append("\r\n");
        System.out.println(pairs.size());

        for (int i = 0; i < pairs.size(); i++) {
            Pair pair = pairs.get(i);
            StreamStore store = this.storage.getStreamStore(pair.streamKey);

            if (store == null) {
                if (isBlocking) {
                    System.out.println("waiting");
                    this.blockingManager.addClient(this, "not present", this.connection, this.selectionKey);
                    return "not present";
                }
                return "+none\r\n";
            }

            SortedMap<StreamID, Block> map;
            if (isBlocking) {
                System.out.println(pair.streamID);
                map = store.getAfter(pair.streamID);
                if (map.isEmpty()) {
                    this.blockingManager.addClient(this, "not present", this.connection, this.selectionKey);
                    return "not present";
                }
            } else {
                System.out.println("tukkaa");
                map = store.getFrom(pair.streamID);
            }


            result.append(this.parseResultString(pair.streamKey, map));
        }

        return result.toString();
    }

    @Override
    public String getDataStructure() {
        return this.dataStructure;
    }


    private String parseResultString(String streamKey, SortedMap<StreamID, Block> map) {
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
        return isBlocking;
    }
}
