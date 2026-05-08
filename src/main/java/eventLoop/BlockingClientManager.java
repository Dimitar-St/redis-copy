package eventLoop;

import commands.BaseCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;

public class BlockingClientManager {
    private final PriorityQueue<WaitingClient> clients = new PriorityQueue<>((f, s) ->
            Math.toIntExact(f.command.timeout - s.command.timeout));

    private final Map<String, Deque<WaitingClient>> waitingByKey = new HashMap<>();

    public BlockingClientManager() {
    }

    long nextDeadline(long now) {
        if (clients.isEmpty()) {
            return 0;
        }

        return clients.peek().command.timeout - now;
    }

    public void handleTimeouts(long now) {
        if (clients.isEmpty()) {
            return;
        }

        while (!clients.isEmpty() && clients.peek().command.timeout <= now) {
            WaitingClient client = clients.poll();

            if (client.completed) {
                continue;
            }

            client.completed = true;
            client.responseWithNull();
        }
    }

    public Optional<WaitingClient> tryResolve(String key) {
        Deque<WaitingClient> queue = waitingByKey.get(key);

        if (queue == null) {
            return Optional.empty();
        }

        while (!queue.isEmpty()) {
            WaitingClient client = queue.poll();

            if (client.completed) {
                continue;
            }

            client.completed = true;

            if (queue.isEmpty()) {
                waitingByKey.remove(key);
            }

            return Optional.of(client);
        }

        return Optional.empty();
    }

    public void respondValue(WaitingClient client)  {
        String response = client.command.execute();

        ByteBuffer buff = ByteBuffer.wrap(response.getBytes());

        while (buff.hasRemaining()) {
            try {
                client.connection.write(buff);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.close(client);
    }

    private void close(WaitingClient client) {
        try {
            client.connection.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

    public void addClient(BaseCommand command, String response, SocketChannel clientSocket, SelectionKey selectionKey) {
        String dataStructure = command.getDataStructure();

        if (command.timeless) {
            WaitingClient wClient = new WaitingClient(dataStructure, command, clientSocket);
            waitingByKey
                    .computeIfAbsent(dataStructure, k -> new ArrayDeque<>())
                    .add(wClient);

            selectionKey.cancel();
            return;
        }

        if (command.isBlocking() && response.equals("not present")) {
            WaitingClient wClient = new WaitingClient(dataStructure, command, clientSocket);
            clients.add(wClient);

            waitingByKey
                    .computeIfAbsent(dataStructure, k -> new ArrayDeque<>())
                    .add(wClient);

            selectionKey.cancel();
        }
    }

}
