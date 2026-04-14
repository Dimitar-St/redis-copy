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

//    class BlockingManager {
//        PriorityQueue<WaitingClient> heap;
//
//        void register(WaitingClient client) {
//            heap.add(client);
//        }
//
//        void handleTimeouts(long now) {
//            while (!heap.isEmpty() && heap.peek().deadline <= now) {
//                WaitingClient client = heap.poll();
//
//                if (client.completed) continue;
//
//                respondNull(client);
//            }
//        }
//
//        long nextDeadline(long now) {
//            if (heap.isEmpty()) return 0;
//            return Math.max(0, heap.peek().deadline - now);
//        }
//    }

    long nextDeadline(long now) {
        if (clients.isEmpty())
            return 0;

        return Math.max(0, clients.peek().command.elapsedTime.getLong(ChronoField.MICRO_OF_SECOND) - now);
    }

//    public void executePendingCommands() {
//        waitingClients.forEach((dataStructure, currentWaitingClients) -> {
//            currentWaitingClients.forEach((command, stack) -> {
//
//                while (!stack.empty()) {
//                    String response = command.execute();
//
//                    if (command.isBlocking()) {
//                        if (response.equals("not present")) {
//                            continue;
//                        }
//                    }
//                    ByteBuffer responseMessage = ByteBuffer.wrap(response.getBytes());
//                    SocketChannel currSocket = stack.pop();
//
//                    while (responseMessage.hasRemaining()) {
//                        try {
//                            currSocket.write(responseMessage);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                }
//            });
//        });
//    }

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
        try {
            String response =
                    "*2\r\n" +
                            "$" + key.length() + "\r\n" + key + "\r\n" +
                            "$" + value.length() + "\r\n" + value + "\r\n";

            ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());

            while (buffer.hasRemaining()) {
                client.connection.write(buffer);
            }
        } catch (IOException e) {
            close(client);
        }
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
