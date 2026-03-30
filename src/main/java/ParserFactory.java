import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ParserFactory {
    private final Map<String, IParser> parsers;

    public ParserFactory() {
        this.parsers = new HashMap<>();
        parsers.put( "*", new ArrayParser());
        parsers.put( "+", new SimpleStringParser());
    }

    IParser newParser(ByteBuffer payload) {
        byte firstByte = payload.get(0);

        IParser parser = parsers.get(String.valueOf(firstByte));
        if (parser == null) {
            throw new IllegalArgumentException("parser does not exist");
        }

        return parser;
    }

    public static ParserFactory create() { return new ParserFactory(); }
}
