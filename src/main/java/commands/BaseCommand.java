package commands;

public abstract class BaseCommand implements ICommand {
    String[] arguments;

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
    public String[] getArguments() {
     return arguments;
    }
}
