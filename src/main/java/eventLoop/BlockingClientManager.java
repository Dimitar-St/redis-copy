package eventLoop;

import commands.BaseCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;

public class BlockingClientManager {
    private final PriorityQueue<WaitingClient> clients =
            new PriorityQueue<>(Comparator.comparingLong(c -> c.command.timeout));

    private final Map<String, Deque<WaitingClient>> waitingByKey = new HashMap<>();

    public BlockingClientManager() {
    }

    long nextDeadline() {
        if (clients.isEmpty()) {
               return -1;   // no deadlines
        }

        return Math.max(0, clients.peek().command.timeout - System.currentTimeMillis());
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

            String key = client.command.getDataStructure();
            Deque<WaitingClient> queue = waitingByKey.get(key);

            if (queue != null) {
                queue.remove(client);

                if (queue.isEmpty()) {
                    waitingByKey.remove(key);
                }
            }
            client.responseWithNull();
        }
    }

    public Optional<WaitingClient> tryResolve(String key) {
        System.out.println("try resolve");
        Deque<WaitingClient> queue = waitingByKey.get(key);

        if (queue == null) {
            return Optional.empty();
        }

        WaitingClient client = queue.poll();

        return Optional.of(client);
    }

    public void respondValue(WaitingClient client) {
        String response = client.command.execute();

        ByteBuffer buff = ByteBuffer.wrap(response.getBytes());

        while (buff.hasRemaining()) {
            try {
                client.connection.write(buff);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public void addClient(BaseCommand command, String response, SocketChannel clientSocket, SelectionKey selectionKey) {
        String dataStructure = command.getDataStructure();
        WaitingClient wClient = new WaitingClient(command, clientSocket);

        if (command.timeless) {
            waitingByKey
                    .computeIfAbsent(dataStructure, k -> new ArrayDeque<>())
                    .add(wClient);

            return;
        }

        if (command.isBlocking() && response.equals("not present")) {
            System.out.println("Registering command: " + command.id);
            clients.add(wClient);
            waitingByKey
                    .computeIfAbsent(dataStructure, k -> new ArrayDeque<>())
                    .add(wClient);
            selectionKey.selector().wakeup();
        }
    }

}
