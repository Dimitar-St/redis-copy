package commands;

import parsers.SimpleStringParser;

public class BulkString extends BaseCommand {

    @Override
    public String execute() {
//        StringBuilder builder = new StringBuilder();
//        int countr = 0;
//        while (countr < this.arguments.length) {
//            char b = this.arguments[countr].;
//
//            builder.append(b);
//
//            return builder.toString();
//        }
        return new SimpleSringCommand().execute();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
