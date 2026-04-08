package commands;

import storage.Storage;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private final Map<String, ICommand> commands = new HashMap<>();
    private final Storage storage = new Storage();

    private CommandFactory() {
        commands.put("ECHO", new Echo());
        commands.put("PING", new Ping());
        commands.put("SET", new Set(this.storage));
        commands.put("GET", new Get(this.storage));
        commands.put("RPUSH", new Rpush(this.storage));
        commands.put("LRANGE", new Lrange(this.storage));
        commands.put("LPUSH", new Lpush(this.storage));
        commands.put("LLEN", new Llen(this.storage));
        commands.put("LPOP", new Lpop(this.storage));
    }

    public ICommand newCommand(String command) {
        if (commands.containsKey(command)) {
            return commands.get(command);
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
