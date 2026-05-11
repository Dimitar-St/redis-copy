package parsers;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ParserFactory {
    private final Map<Integer, IParser> parsers;

    public ParserFactory() {
        this.parsers = new HashMap<>();
        parsers.put(42, new ArrayParser());
        parsers.put(43, new SimpleStringParser());
        parsers.put(36, new BulkStringParser());
    }

    public IParser newParser(ByteBuffer payload) {
        byte firstByte = payload.get(0);
        IParser parser = parsers.get((int) firstByte);

        if (parser == null) {
            throw new IllegalArgumentException("parser does not exist");
        }

        return parser;
    }

    public static ParserFactory create() { return new ParserFactory(); }
}
