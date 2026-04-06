import commands.CommandFactory;
import commands.ICommand;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ArrayParser implements IParser {
    public ArrayParser() {}

    public String parse(ByteBuffer payload) {
        byte firstByte = payload.get();
        if (firstByte != '*') {
            throw new IllegalArgumentException("Expected array");
        }

        StringBuilder lengthBuilder = new StringBuilder();

        byte b;
        while ((b = payload.get()) != '\r') {
            lengthBuilder.append((char) b);
        }

        payload.get(); // skip '\n'

        int arrayLength = Integer.parseInt(lengthBuilder.toString());

        String[] elements = new String[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            elements[i] = readBulkString(payload);
        }

        String recievedCommand = elements[0].toUpperCase();
        CommandFactory commandFactory = CommandFactory.initialize();
        ICommand command = commandFactory.newCommand(recievedCommand);

        return command.execute(Arrays.stream(elements, 1, elements.length).toArray(String[]::new));
    }

    private String readBulkString(ByteBuffer payload) {
        byte b = payload.get();
        if (b != '$') {
            throw new IllegalArgumentException("Expected bulk string");
        }

        StringBuilder lengthBuilder = new StringBuilder();
        while ((b = payload.get()) != '\r') {
            lengthBuilder.append((char) b);
        }

        payload.get(); // skip '\n'

        int length = Integer.parseInt(lengthBuilder.toString());
        if (length == -1) {
            return null;
        }

        byte[] data = new byte[length];

        payload.get(data);
        payload.get(); // skip '\r'
        payload.get(); // skip '\n'

        return new String(data);
    }

    private String encodeBulkString(String s) {
        return "$" + s.length() + "\r\n" + s + "\r\n";
    }
}
