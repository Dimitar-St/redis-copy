package storage;

import commands.Options;

import java.util.HashMap;
import java.util.Map;

public class Storage {
    private final Map<String, Value> cache = new HashMap<>();

    public Storage() {}

    public void set(String key, String content, Options options) {
        Value<String> value = new Value(content, options);
        cache.put(key, value);
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
