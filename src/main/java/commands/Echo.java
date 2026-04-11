package commands;

public class Echo extends BaseCommand {

    public Echo() {}

    @Override
    public String execute() {
        return encodeBulkString(arguments[0]);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    private String encodeBulkString(String s) {
        return "$" + s.length() + "\r\n" + s + "\r\n";
    }
}
