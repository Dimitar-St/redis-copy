package utils;

import java.nio.ByteBuffer;

public class ArgumentParser {


    public static String[] parse(ByteBuffer payload) {
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

        return elements;
    }

    private static String readBulkString(ByteBuffer payload) {
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
