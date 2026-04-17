package commands;

import java.util.Objects;

public class StreamID {
    private String id;

    public StreamID(String id) {
        this.id = id;
    }


    @Override
    public int hashCode() {
       return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StreamID streamID = (StreamID) o;
        return Objects.equals(id, streamID.id);
    }
}
