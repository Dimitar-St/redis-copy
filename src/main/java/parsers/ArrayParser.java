package parsers;

import commands.BaseCommand;
import commands.CommandFactory;
import commands.ICommand;
import utils.ArgumentParser;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ArrayParser implements IParser {
    public ArrayParser() {}

    public BaseCommand parse(ByteBuffer payload) {
        byte firstByte = payload.get();
        if (firstByte != '*') {
            throw new IllegalArgumentException("Expected array");
        }

        String[] arguments =  ArgumentParser.parse(payload);
        String recievedCommand = arguments[0].toUpperCase();
        CommandFactory commandFactory = CommandFactory.initialize();
        BaseCommand command = commandFactory.newCommand(recievedCommand);
        command.setArguments(arguments);

        return command;


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
