import java.nio.ByteBuffer;

public interface IParser {
    String parse(ByteBuffer payload);
}
