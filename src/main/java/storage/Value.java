package storage;

import commands.Options;

public class Value<V> {
    private V value;
    private Options options;

    public Value(V value) {
        this.value = value;
    }

    public Value(V value, Options options) {
        this.value = value;
        this.options = options;
    }

    public boolean isInvalid() {
        return this.options.isExpired();
    }

    public int length() {
        return this.value.toString().length();
    }

    public String toString() {
        return this.value.toString();
    }
}
