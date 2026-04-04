import java.nio.ByteBuffer;

public class SimpleStringParser implements IParser {
    public SimpleStringParser() {}

    public String parse(ByteBuffer payload) {
        return "+PONG\r\n";
    }
}
