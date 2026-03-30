import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ParserFactory {
    private final Map<Character, IParser> parsers;

    public ParserFactory() {
        this.parsers = new HashMap<>();
        parsers.put( '*', new ArrayParser());
        parsers.put( '+', new SimpleStringParser());
    }

    IParser newParser(ByteBuffer payload) {
        byte firstByte = payload.get(0);
        System.out.println(new String(payload.array()).charAt(0));
        IParser parser = parsers.get(new String(payload.array()).charAt(0));
        if (parser == null) {
            throw new IllegalArgumentException("parser does not exist");
        }

        return parser;
    }

    public static ParserFactory create() { return new ParserFactory(); }
}
