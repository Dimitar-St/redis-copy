package eventLoop;

import commands.BaseCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.*;

public class WaitingClientManager {
//    private Map<String, Map<BaseCommand, Stack<SocketChannel>>> waitingClients = new HashMap<>();
    private PriorityQueue<WaitingClient> clients = new PriorityQueue<>((f, s) ->
       Math.toIntExact(f.command.timeout - s.command.timeout));

    private final Map<String, Deque<WaitingClient>> waitingByKey = new HashMap<>();

    public WaitingClientManager() {}

    long nextDeadline(long now) {
        if (clients.isEmpty())
            return 0;

        return Math.max(0, clients.peek().command.elapsedTime.getLong(ChronoField.MILLI_OF_SECOND) - now);
    }

    public void handleTimeouts(long now) {
        if (clients.isEmpty())
            return;

        while (!clients.isEmpty() && clients.peek().command.elapsedTime.getLong(ChronoField.MILLI_OF_SECOND) <= now) {
            WaitingClient client = clients.poll();

            assert client != null;
            if (client.completed) continue;

            client.completed = true;
            client.responseWithNull();
        }
    }

     public Optional<WaitingClient> tryResolve(String key) {
        Deque<WaitingClient> queue = waitingByKey.get(key);

        if (queue == null) return Optional.empty();

        while (!queue.isEmpty()) {
            WaitingClient client = queue.poll();

            if (client.completed) continue;

            client.completed = true;
            return Optional.of(client);
        }

        return Optional.empty();
    }
    public void respondValue(WaitingClient client, String key, String value) {
            client.command.execute();
            this.close(client);
    }

    private void close(WaitingClient client) {
        try {
            client.connection.close();
        } catch (IOException ignored) {}
    }

    public boolean addClient(BaseCommand command, String response, SocketChannel clientSocket) {
        String dataStructure = command.getArguments()[0];
        if (command.isBlocking() && response.equals("not present")) {
            WaitingClient wClient = new WaitingClient(dataStructure, command, clientSocket);
            clients.add(wClient);

            waitingByKey
                    .computeIfAbsent(dataStructure, k -> new ArrayDeque<>())
                    .add(wClient);


            return true;
        }
        return false;
    }

}
