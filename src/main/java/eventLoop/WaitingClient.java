package eventLoop;

import commands.BaseCommand;

import java.nio.channels.SocketChannel;

public class WaitingClient {
    BaseCommand command;
    SocketChannel connection;

    public WaitingClient() {}
}
