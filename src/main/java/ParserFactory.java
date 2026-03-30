import java.nio.ByteBuffer;
import java.util.Arrays;
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
        System.out.println(Byte.toString(payload.array()[0]));
        IParser parser = parsers.get(String.valueOf(payload.array()[0]));
        if (parser == null) {
            throw new IllegalArgumentException("parser does not exist");
        }

        return parser;
    }

    public static ParserFactory create() { return new ParserFactory(); }
}
