package commands;

import eventLoop.BlockingClientManager;
import storage.Storage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CommandFactory {
    private final Map<String, Supplier<BaseCommand>> commands = new HashMap<>();
    public  final BlockingClientManager blockingManager = new BlockingClientManager();
    private final Storage storage = new Storage();

    private CommandFactory() {
        commands.put("ECHO", () -> new Echo());
        commands.put("PING", () -> new Ping());
        commands.put("SET", () -> new Set(this.storage));
        commands.put("GET", () -> new Get(this.storage));
        commands.put("RPUSH", () -> new Rpush(this.storage, blockingManager));
        commands.put("LRANGE", () -> new Lrange(this.storage));
        commands.put("LPUSH", () -> new Lpush(this.storage));
        commands.put("LLEN", () -> new Llen(this.storage));
        commands.put("LPOP", () -> new Lpop(this.storage));
        commands.put("BLPOP", () -> new Blpop(this.storage, blockingManager));
        commands.put("simpleString", () -> new SimpleSringCommand());
        commands.put("TYPE", () -> new Type(this.storage));
        commands.put("XADD", () -> new Xadd(this.storage));
    }

    public BaseCommand newCommand(String command) {
        if (commands.containsKey(command)) {
            return commands.get(command).get();
        }
        throw new IllegalArgumentException("Unknown command: " + command);
    }

    private static CommandFactory singleton;

    public static CommandFactory initialize() {
        if (singleton == null) {
            singleton = new CommandFactory();
        }
        return singleton;
    }
}
