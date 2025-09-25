package qa.ai.healing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.util.*;

public class HealingEventLogger {
    private static final List<HealingEvent> EVENTS = new ArrayList<>();
    private static Path outputPath = Paths.get("healing-events.json");
    private static final ObjectMapper M = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static void setOutputPath(String path) { outputPath = Paths.get(path); }

    public static void log(HealingEvent e) {
        if (e.timestamp == null) e.timestamp = ZonedDateTime.now().toString();
        synchronized (EVENTS) {
            EVENTS.add(e);
        }
    }

    public static void flush() {
        try {
            Files.createDirectories(outputPath.getParent() != null ? outputPath.getParent() : Paths.get("."));
            M.writeValue(outputPath.toFile(), EVENTS);
            System.out.println("[HealingEventLogger] Wrote " + EVENTS.size() + " events to " + outputPath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
