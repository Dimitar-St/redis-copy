package storage;

import java.util.HashMap;
import java.util.Map;

public class Storage {
    private final Map<String, String> cache = new HashMap<>();

    public Storage() {}

    public void set(String key, String value) {
        cache.put(key, value);
    }

    public String get(String key) {
        return cache.get(key);
    }


}
