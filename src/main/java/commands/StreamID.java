package commands;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Comparator;
import java.util.Objects;

public class StreamID implements Comparable {
    private String id;

    private Long timestamp;
    private Long counter;

    public StreamID(String id) {
        this.id = id;
    }

    public StreamID(Long timestamp, Long counter) {
       this.counter = counter;
       this.timestamp = timestamp;
    }


    @Override
    public int hashCode() {
       return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StreamID streamID = (StreamID) o;
        return Objects.equals(timestamp, streamID.timestamp) && Objects.equals(counter, streamID.counter);
    }

    @Override
    public int compareTo(Object o) {
        StreamID other = (StreamID) o;
        int compare = Comparator.comparingLong(StreamID::getTimestamp)
                .thenComparingLong(StreamID::counter)
                .compare(this, other);
        return compare;
    }

    public String toString() {
        return this.timestamp + "" + this.counter;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public Long counter() {
        return this.counter;
    }

    public static StreamID parse(String id) {
        if (id.equals("*")) {
            var timestamp = System.currentTimeMillis();
            Long counter = 1L;
            return new StreamID(timestamp, counter);
        }


        String[] timestampCounter = id.split("-");
        if (timestampCounter[1].equals("*")) {
            var timestamp = Long.parseLong(timestampCounter[0]);
            return new StreamID(timestamp, -1L);
        }


        long timestamp = Instant.ofEpochMilli(Long.parseLong(timestampCounter[0])).getLong(ChronoField.MILLI_OF_SECOND);
        long counter = Long.parseLong(timestampCounter[1]);

        return new StreamID(timestamp, counter);

    }

    public boolean isPartialGenerated() {
        return counter < 0;
    }
}
