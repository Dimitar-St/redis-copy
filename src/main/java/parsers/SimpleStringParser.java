package parsers;

import commands.BaseCommand;
import commands.CommandFactory;
import commands.ICommand;

import java.nio.ByteBuffer;

public class SimpleStringParser implements IParser {
    public SimpleStringParser() {}

    public BaseCommand parse(ByteBuffer payload) {

        CommandFactory commandFactory = CommandFactory.initialize();
        BaseCommand command = commandFactory.newCommand("simpleString");
        return command;
    }
}
