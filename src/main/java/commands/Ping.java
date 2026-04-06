package commands;

public class Ping implements ICommand {

    public Ping() {}

    @Override
    public String execute(String[] payload) {
        return "+PONG\r\n";
    }
}
