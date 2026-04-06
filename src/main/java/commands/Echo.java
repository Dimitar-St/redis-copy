package commands;

public class Echo implements ICommand {

    public Echo() {}

    @Override
    public String execute(String payload) {
        return encodeBulkString(payload);
    }

    private String encodeBulkString(String s) {
        return "$" + s.length() + "\r\n" + s + "\r\n";
    }
}
