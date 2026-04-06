package storage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Storage {
    private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();

    public Storage() {}

    public void set(String key, String value) {
        cache.put(key, value);
    }

    public String get(String key) {
        return cache.get(key);
    }


}
