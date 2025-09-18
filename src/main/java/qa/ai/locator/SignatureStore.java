package qa.ai.locator;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SignatureStore {
    private final Path file;
    private final ObjectMapper om = new ObjectMapper();

    public SignatureStore(Path file){ this.file=file; }

    public synchronized void save(ElementSignature sig){
        try {
            Map<String,ElementSignature> map = loadAll();
            map.put(sig.key, sig);
            om.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), map);
        } catch(Exception e){ throw new RuntimeException(e); }
    }

    public synchronized ElementSignature load(String key){
        try {
            Map<String,ElementSignature> map = loadAll();
            return map.getOrDefault(key, null);
        } catch(Exception e){ return null; }
    }

    private Map<String,ElementSignature> loadAll() throws IOException {
        if (!Files.exists(file)) return new HashMap<>();
        try (InputStream in = Files.newInputStream(file)) {
            return om.readValue(in, om.getTypeFactory()
                    .constructMapType(HashMap.class, String.class, ElementSignature.class));
        }
    }
}
