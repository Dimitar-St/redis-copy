package commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Block {
    private ArrayList<String> fields;

    public Block() {}

    public void setFields(List<String> fields) {
       this.fields = (ArrayList<String>) fields;
    }

    public void appendField(List<String> fields) {
        this.fields.addAll(fields);
    }

}
