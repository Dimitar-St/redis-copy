package commands;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class Options {
    private final Map<String, LocalDateTime> metadata = new HashMap<>();

    private Options() {
    }

    public static Options initialize(String type, String value) {
       Options options  = new Options();
       LocalDateTime now = null;

        if (type.equals("PX")) {
            now = LocalDateTime.now().plus(Long.valueOf(value), ChronoUnit.MILLIS);
            options.metadata.put("expiresAt", now);

            return options;
        }

        if (type.equals("EX")) {
           now = LocalDateTime.now().plusSeconds(Long.valueOf(value));
            options.metadata.put("expiresAt", now);

            return options;
       }


       options.metadata.put("expiresAt", now);

       return options;
    }

    public boolean isExpired() {
        LocalDateTime createdAt =  this.metadata.get("expiresAt");
        if (createdAt != null) {
            return createdAt.isAfter(ChronoLocalDateTime.from(LocalDateTime.now()));
        }

        return true;
    }
}
