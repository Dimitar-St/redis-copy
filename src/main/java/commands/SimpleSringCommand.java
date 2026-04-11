package commands;

public class SimpleSringCommand extends BaseCommand {
    @Override
    public String execute() {
        return "+PONG\r\n";
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
