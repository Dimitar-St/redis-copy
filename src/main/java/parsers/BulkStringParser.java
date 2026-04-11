package parsers;

import commands.BaseCommand;
import commands.BulkString;

import java.nio.ByteBuffer;

public class BulkStringParser implements IParser {
    @Override
    public BaseCommand parse(ByteBuffer payload) {
        return new BulkString();
    }
}
