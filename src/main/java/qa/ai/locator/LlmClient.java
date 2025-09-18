package qa.ai.locator;

import java.net.http.*;
import java.net.URI;

public class LlmClient {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String endpoint; // e.g., http://localhost:11434/api/generate
    private final String model;    // e.g., "llama3"

    public LlmClient(String endpoint, String model){
        this.endpoint = endpoint; this.model = model;
    }

    public double rerank(String signatureJson, String selector){
        // Return an extra boost 0..0.3 based on LLM judgement (stubbed)
        return 0.0;
    }
}
