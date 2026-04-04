import java.nio.ByteBuffer;

public class ArrayParser implements IParser {
    public ArrayParser() {}

    public String parse(ByteBuffer payload) {
        StringBuilder builder = new StringBuilder();
        while (payload.hasRemaining()) {
           byte b = payload.get();

           builder.append((char) b);

           if (builder.toString().equalsIgnoreCase("echo")) {
               ByteBuffer duplicate = payload.duplicate();
               duplicate.position(payload.position());
               duplicate.limit(payload.position()+payload.limit());

               return duplicate.slice().toString();
           }
        }
        return "";
    }
}
