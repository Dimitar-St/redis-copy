import java.nio.ByteBuffer;

public class BulkStringParser implements IParser{
    @Override
    public String parse(ByteBuffer payload) {
        StringBuilder builder = new StringBuilder();
        while (payload.hasRemaining()) {
            char b = payload.getChar();

            builder.append(b);

            System.out.println(builder.toString());

            return builder.toString();
        }
        return "";
    }
}
