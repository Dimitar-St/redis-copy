package commands;

public class SimpleSringCommand extends BaseCommand {
    @Override
    public String execute() {
        return "+PONG\r\n maika ti";
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
