package storage;

import commands.Block;
import commands.Options;
import commands.StreamID;
import commands.StreamStore;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

public class Storage {
    private final Map<String, Value> cache = new HashMap<>();
    private final Map<String, StreamStore> streamStoresCache = new HashMap<>();

    public Storage() {}

    public void set(String key, String content, Options options) {
        Value<String> value = new Value(content, options);
        cache.put(key, value);
    }


    public void setStreamStore(String key, StreamStore store) {
       this.streamStoresCache.put(key, store);
    }

    public StreamStore getStreamStore(String key) {
        return this.streamStoresCache.get(key);
    }

    public SortedMap<StreamID, Block> getRange(StreamID startKey, StreamID endKey) {
        return this.getRange(startKey, endKey);
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public void set(String key, Value content) {
        cache.put(key, content);
    }


    public Value get(String key) {
        if (cache.containsKey(key)) {
            Value<String> value = cache.get(key);
            if (value.isInvalid()) {
                return null;
            }
        }

        return cache.get(key);
    }
}
