package commands;

import storage.Storage;
import storage.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lpush implements ICommand {

    private final Storage storage;

    public Lpush(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String execute(String[] payload) {
        String key = payload[0];

        Value value = this.storage.get(key);

        String[] elements = Arrays.stream(payload, 1, payload.length).toArray(String[]::new);

        if (value == null) {
            value = new Value(new ArrayList<String>());

            insertElements(elements, (List<String>) value.getValue());
            this.storage.set(key, value);
            return ":" + (payload.length - 1) + "\r\n";
        }

        List<String> list = (List<String>) value.getValue();

        insertElements(elements, list);
        return ":" + list.size() + "\r\n";
    }


    private void insertElements(String[] elements, List<String> list) {
        int length = elements.length-1;
        for (int i = 0; i <= length; i++) {
           list.add(0, elements[i]);
        }
    }
}
