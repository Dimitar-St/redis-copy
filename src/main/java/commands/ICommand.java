package commands;

public interface ICommand {
    String execute();


    boolean isBlocking();
}
