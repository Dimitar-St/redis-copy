package commands;

import eventLoop.BlockingClientManager;
import storage.Storage;

import java.util.*;

public class Xread extends BaseCommand {
    private final Storage storage;
    private final BlockingClientManager blockingManager;
    private boolean isBlocking;
    private boolean readOnlyFromNewStream;
    private boolean parsed;
    private String dataStructure;
    private int counter;
    private List<Pair> pairs;

    public Xread(Storage storage, BlockingClientManager blockingManager) {
        this.storage = storage;
        this.blockingManager = blockingManager;
        this.isBlocking = false;
    }

    // This methods parses command arguments into pairs so that it could read from multiple streams
    // Divides the aruments by 2 and walks the array with two indexes at mind the stream key and the stream id.
    // The stream key starts from argsToRemove+i and the paired stream id starts from argsToRemove+streamCount+i;
    // Commands example:
    //              XREAD STREAMS <key1> <key2> ... <id1> <id2> ...
    //              XREAD streams stream_key other_stream_key 0-0 0-1
    private List<Pair> readMultipleStreams() {
        if (!parsed) {
            parsed = true;
        } else {
           return this.pairs;
        }
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
        if (streamCount == 1 && arguments[arguments.length-1].equals("$")) {
            this.readOnlyFromNewStream = true;
            pairs.add(new Pair(arguments[arguments.length-2], new StreamID()));
            return pairs;
        }

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
        System.out.println("Start executing XREAD Command: " + id.toString());
        List<Pair> pairs = this.readMultipleStreams();
        this.pairs = pairs;
        StringBuilder result = new StringBuilder();

        result.append("*");
        result.append(pairs.size());
        result.append("\r\n");

        for (int i = 0; i < pairs.size(); i++) {
            Pair pair = pairs.get(i);
            StreamStore store = this.storage.getStreamStore(pair.streamKey);

            if (store == null) {
                if (isBlocking) {
                    this.blockingManager.addClient(this, "not present", this.connection, this.selectionKey);
                    return "not present";
                }
                return "+none\r\n";
            }

            // Terrible code. It should change based on the Xread command arguments, every Xread behaves differently based on the arguments.
            // Consider implementing SubCommands based on a set of arguments and removing the if else;
            SortedMap<StreamID, Block> map;
            if (isBlocking) {
                if (readOnlyFromNewStream) {
                    map = new TreeMap<>();
                    System.out.println(counter);
                    if (counter > 0) {
                        var latest = store.getLatest();
                        map.put(latest.getKey(), latest.getValue());
                    }
                    counter++;
                } else {
                    map = store.getAfter(pair.streamID);
                }
                if (map.isEmpty()) {
                    this.blockingManager.addClient(this, "not present", this.connection, this.selectionKey);
                    return "not present";
                }
            } else {
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
