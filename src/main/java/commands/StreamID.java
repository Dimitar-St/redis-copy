package commands;

import java.util.Comparator;
import java.util.Objects;

public class StreamID implements Comparable {
    private String id;

    private Long timestamp;
    private Long counter;

    public StreamID(String id) {
//        this.timestamp = System.currentTimeMillis()
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

    @Override
    public int compareTo(Object o) {
        StreamID c = (StreamID) o;
        return this.id.compareTo(c.id);
    }
}
