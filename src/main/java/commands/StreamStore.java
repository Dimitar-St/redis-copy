package commands;

import java.util.*;

public class StreamStore {

    TreeMap<StreamID, Block> store;
    HashMap<Long, Long> timestampMap;

    public StreamStore() {
        this.store = new TreeMap<>();
        this.timestampMap = new HashMap<>();
    }


    public StreamID generateId(StreamID key) {
        if (key.isPartialGenerated()) {
            Long counter = key.getTimestamp() == 0 ? timestampMap.getOrDefault(key.getTimestamp(), 0L)+1 : timestampMap.getOrDefault(key.getTimestamp(), -1L)+1 ;

            timestampMap.put(key.getTimestamp(), counter);

            key = new StreamID(key.getTimestamp(), counter);
        }

        return key;
    }

    public String put(StreamID key, String[] data) {
        if (!store.isEmpty()) {
            StreamID lastID = store.lastKey();
            if (store.containsKey(key) || lastID.compareTo(key) > 0) {
                return "-ERR The ID specified in XADD is equal or smaller than the target stream top item\r\n";
            }
        }

        Block block = this.store.get(key);

        if (block == null) {
            block = new Block();
            block.setFields(List.of(data));

            store.put(key, block);
            return "";
        }

        block.appendField(List.of(data));

        if (store.containsKey(key)) {
            key = this.generateId(key);
        }

        store.put(key, block);
        return "";
    }
}
