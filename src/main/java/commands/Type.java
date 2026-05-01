package commands;

import eventLoop.BlockingClientManager;
import storage.Storage;
import storage.Value;

import java.util.Arrays;

public class Type extends BaseCommand {

    private final Storage storage;

    public Type(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute() {
        String key = this.getArguments()[0];

        Value value = this.storage.get(key);

        if (value != null) {

            String typePath = value.getValue().getClass().getName();

            StringBuilder type = new StringBuilder();

            for (int i = typePath.length()-1; i >= 0; i--) {
                if (typePath.charAt(i) == '.') {
                    break;
                }
                type.append(typePath.charAt(i));
            }

            String typeLower = type.reverse().toString().toLowerCase();

            if (typeLower.contains("streamstore")) {
                return "+" + "stream" + "\r\n";
            }

            return "+"+ typeLower + "\r\n";
        }

        return "+none\r\n";
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
