package commands;

import storage.Storage;
import storage.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Xadd extends BaseCommand {
    private final Storage storage;

    public Xadd(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute() {
        String streamKey = this.arguments[0];

        Value value = this.storage.get(streamKey);
        StreamID streamID = new StreamID(this.arguments[1]);
        if (streamID.equals(new StreamID("0-0"))) {
            return "-ERR he ID specified in XADD must be greater than 0-0\r\n";
        }

        if (value == null) {
            TreeMap<StreamID, Block> content = new TreeMap<>();
            Value<TreeMap<StreamID, Block>> newStream = new Value<>(content);

            Block block = new Block();

            List<String> list = new ArrayList<>();
            String[] array = arguments;
            int bound = arguments.length;
            for (int i = 2; i < bound; i++) {
                String s = array[i];
                list.add(s);
            }
            List<String> data = new ArrayList(list);

            block.setFields(data);

            content.put(streamID, block);
            this.storage.set(streamKey, newStream);

            return "$" + this.arguments[1].length() + "\r\n" + this.arguments[1] + "\r\n";
        }

        TreeMap<StreamID, Block> content = (TreeMap<StreamID, Block>) value.getValue();

        StreamID lastID = content.lastKey();
        if (content.containsKey(streamID) || lastID.compareTo(streamID) <= 0) {
            return "-ERR The ID specified in XADD is equal or smaller than the target stream top item\r\n";
        }

        Block block = new Block();

        List<String> data = new ArrayList<>(Arrays.stream(arguments, 2, arguments.length)
                .toList());

        List<String> list = new ArrayList<>();
        String[] array = arguments;
        int bound = arguments.length;
        for (int i = 2; i < bound; i++) {
            String s = array[i];
            list.add(s);
        }

        data.addAll(new ArrayList(list));

        block.setFields(data);

        content.put(streamID, block);

        return "$" + this.arguments[1].length() + "\r\n" + this.arguments[1] + "\r\n";
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
