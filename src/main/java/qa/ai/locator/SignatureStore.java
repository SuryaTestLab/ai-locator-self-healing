package qa.ai.locator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** JSON file-backed store for element signatures keyed by alias. */
public class SignatureStore {
    private final Path file;
    private final Map<String, ElementSignature> cache = new LinkedHashMap<>();
    private final ObjectMapper M = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public SignatureStore(Path file) {
        this.file = file;
        load();
    }

    public synchronized Optional<ElementSignature> get(String alias) {
        return Optional.ofNullable(cache.get(alias));
    }

    public synchronized void put(String alias, ElementSignature sig) {
        if (alias == null || alias.isBlank() || sig == null) return;
        cache.put(alias, sig);
        save();
    }

    public synchronized void remove(String alias) {
        cache.remove(alias);
        save();
    }

    private void load() {
        try {
            if (Files.exists(file)) {
                byte[] bytes = Files.readAllBytes(file);
                if (bytes.length > 0) {
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String,Object>> raw = M.readValue(bytes, Map.class);
                    raw.forEach((k,v) -> cache.put(k, M.convertValue(v, ElementSignature.class)));
                }
            } else {
                if (file.getParent() != null) Files.createDirectories(file.getParent());
                Files.writeString(file, "{}", java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.err.println("[SignatureStore] load failed: " + e.getMessage());
        }
    }

    private void save() {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            M.writeValue(file.toFile(), cache);
        } catch (IOException e) {
            System.err.println("[SignatureStore] save failed: " + e.getMessage());
        }
    }
}
