package eventLoop;

import commands.BaseCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class WaitingClientManager {
    private Map<String, Map<BaseCommand, Stack<SocketChannel>>> waitingClients = new HashMap<>();

    public WaitingClientManager() {}


    public void executePendingCommands() {
        waitingClients.forEach((dataStructure, currentWaitingClients) -> {
            currentWaitingClients.forEach((command, stack) -> {

                while (!stack.empty()) {
                    String response = command.execute();

                    if (command.isBlocking()) {
                        if (response.equals("not present")) {
                            continue;
                        }
                    }
                    ByteBuffer responseMessage = ByteBuffer.wrap(response.getBytes());
                    SocketChannel currSocket = stack.pop();

                    System.out.println(response);
                    System.out.println("Now: " + Instant.now());
                    System.out.println("Exec: " + command.execTime);
                    System.out.println("Elapsed time: " + command.elapsedTime);

                    while (responseMessage.hasRemaining()) {
                        try {
                            currSocket.write(responseMessage);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        });
    }

    public boolean addClient(BaseCommand command, String response, SocketChannel clientSocket, SelectionKey key) {
        String dataStructure = command.getArguments()[0];
        if (command.isBlocking() && response.equals("not present")) {
            Map<BaseCommand, Stack<SocketChannel>> cl = waitingClients.get(dataStructure);
            if (cl == null) {
                Stack<SocketChannel> queue = new Stack<>();
                queue.add(clientSocket);
                Map<BaseCommand, Stack<SocketChannel>> commandQueue = new HashMap<>();
                commandQueue.put(command, queue);
                waitingClients.put(dataStructure, commandQueue);
                return true;
            }

            Stack<SocketChannel> cq = cl.computeIfAbsent(command, k -> new Stack<>());
            cq.add(clientSocket);

            key.cancel();

            return true;
        }
        return false;
    }

}
