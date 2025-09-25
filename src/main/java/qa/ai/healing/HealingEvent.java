package qa.ai.healing;

import java.util.LinkedHashMap;
import java.util.Map;

public class HealingEvent {
    public String testCase;
    public String page;
    public String originalLocator;
    public String healedLocator;
    public double confidence;
    public String status;            // USED | FAILED | SKIPPED
    public String reason;
    public long durationMs;
    public String domSnippet;        // optional
    public String screenshotBase64;  // optional
    public Map<String, Object> extra = new LinkedHashMap<>();
    public String timestamp;
}
