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

//        System.out.println("Handle timeout connections...");
//        System.out.println("Clients waiting: " + clients.size());
//        clients.stream().sequential().forEach(c -> {
//            System.out.println(c.id);
//        });
        System.out.println("timeout: " + clients.peek().command.timeout);
        System.out.println("now:     " + now);
        while (!clients.isEmpty() && clients.peek().command.timeout <= now) {
            WaitingClient client = clients.poll();
            waitingByKey.remove(client.command.getDataStructure());

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
            clients.remove(client);

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
            clients.add(wClient);

            waitingByKey
                    .computeIfAbsent(dataStructure, k -> new ArrayDeque<>())
                    .add(wClient);
        }
    }

}
