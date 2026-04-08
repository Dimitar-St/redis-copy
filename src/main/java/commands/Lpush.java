package commands;

import storage.Storage;
import storage.Value;

import java.util.ArrayList;
import java.util.List;

public class Lpush implements ICommand {

    private final Storage storage;

    public Lpush(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute(String[] payload) {
        String key = payload[0];

        Value list = this.storage.get(key);

        if (list == null) {
            list = new Value(new ArrayList<String>());

            insertElements(payload, (List<String>) list.getValue());
            this.storage.set(key, list);
            return ":" + (payload.length - 1) + "\r\n";
        }

        insertElements(payload, (List<String>) list.getValue());
        return ":" + (payload.length - 1) + "\r\n";
    }


    private void insertElements(String[] elements, List<String> list) {
        int length = elements.length-1;
        for (int i = length; i >= 1; i--) {
           list.add(0, elements[i]);
        }
    }
}
