package parsers;

import commands.BaseCommand;
import commands.ICommand;

import java.nio.ByteBuffer;

public interface IParser {
    BaseCommand parse(ByteBuffer payload);
}
