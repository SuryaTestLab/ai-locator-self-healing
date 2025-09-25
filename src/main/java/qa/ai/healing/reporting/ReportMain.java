package qa.ai.healing.reporting;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import java.time.LocalDateTime;

public class ReportMain {


    public static void main(String[] args) throws Exception {

    }

    public static void generateReport(String[] args) throws Exception {
        Map<String, String> cli = parseArgs(args);

        Path in = Paths.get(cli.getOrDefault("--in", "healing-events.json"));
        Path out = Paths.get(cli.getOrDefault("--out", "out/healing-report.html"));
        String title = cli.getOrDefault("--title", "AI Locator Healing Report");
        String app = cli.getOrDefault("--app", "my test app");
        String runId = cli.getOrDefault("--run", ZonedDateTime.now().toString());

        // 1) Load events JSON
        List<HealingEvent> events = loadEvents(in);

        // 2) Build model (never null)
        ReportModel model = ReportModel.from(events, title, app, runId);

        // 3) Setup FreeMarker WITH a BeansWrapper to expose fields/getters
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_33);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setClassForTemplateLoading(ReportMain.class, "/templates"); // template at resources/templates/report.ftl
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        // Explicit ObjectWrapper helps when using public fields instead of getters
        DefaultObjectWrapperBuilder owb = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_33);
        owb.setExposeFields(true); // <— allow accessing public fields like m.title, e.testCase, etc.
        cfg.setObjectWrapper(owb.build());

        // 4) Render
        Template tpl = getTemplate(cfg, "report.ftl");
        Files.createDirectories(out.getParent());
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(out), StandardCharsets.UTF_8)) {
            // Put model into the root under key "m" (matches ${m.*} in the template)
            Map<String, Object> root = new HashMap<>();
            root.put("m", model);
            tpl.process(root, w);
        }

        System.out.println("[✓] Report written to " + out.toAbsolutePath());
        System.out.println("[i] Events: total=" + model.total + ", used=" + model.used + ", failed=" + model.failed);
    }

    /* --------------------------------- helpers --------------------------------- */

    static Map<String, String> parseArgs(String[] args) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length && args[i].startsWith("--")) m.put(args[i], args[i + 1]);
        }
        return m;
    }

    static List<HealingEvent> loadEvents(Path in) throws IOException {
        if (!Files.exists(in)) {
            throw new FileNotFoundException("Input not found: " + in.toAbsolutePath()
                    + "\nHint: Ensure HealingEventLogger.flush() wrote the JSON and pass the correct --in path.");
        }
        ObjectMapper M = new ObjectMapper();
        String raw = Files.readString(in);
        if (raw == null || raw.isBlank()) {
            System.err.println("[!] Input file is empty: " + in.toAbsolutePath());
            return List.of();
        }
        try {
            List<HealingEvent> evs = M.readValue(raw, new TypeReference<List<HealingEvent>>() {
            });
            return (evs != null) ? evs : List.of();
        } catch (Exception ex) {
            throw new IOException("Failed to parse JSON at " + in.toAbsolutePath()
                    + " — ensure the file is an array of HealingEvent objects.", ex);
        }
    }

    static Template getTemplate(Configuration cfg, String name) throws IOException {
        try {
            return cfg.getTemplate(name);
        } catch (TemplateNotFoundException notFound) {
            throw new FileNotFoundException("Template '" + name + "' not found on classpath at /templates/"
                    + "\nExpected path: healing-report/src/main/resources/templates/" + name);
        }
    }

    /* ------------------------------ DTOs / Model ------------------------------ */

    // DTO mirrored from healing-core for report purposes (keep fields public)
    public static class HealingEvent {
        public String testCase, page, originalLocator, healedLocator, status, reason, domSnippet, screenshotBase64, timestamp;
        public double confidence;
        public long durationMs;
        public Map<String, Object> extra = new LinkedHashMap<>();
    }

    public static class ReportModel {
        public String title, app, runId, generatedAt;
        public List<HealingEvent> events;
        public long total, used, failed, skipped, avgDurationMs;
        public double avgConfidence;
        public Map<String, Long> byPage, byTest;
        public List<HealingEvent> topSlow, lowConfidence;

        public static ReportModel from(List<HealingEvent> evs, String title, String app, String runId) {
            ReportModel m = new ReportModel();
            m.title = nz(title, "AI Locator Healing Report");
            m.app = nz(app, "");
            m.runId = nz(runId, ZonedDateTime.now().toString());
            m.generatedAt = LocalDateTime.now().toString(); // ZonedDateTime.now().toString();
            m.events = (evs != null) ? evs : List.of();

            m.total = m.events.size();
            m.used = m.events.stream().filter(e -> "USED".equalsIgnoreCase(nz(e.status, ""))).count();
            m.failed = m.events.stream().filter(e -> "FAILED".equalsIgnoreCase(nz(e.status, ""))).count();
            m.skipped = m.events.stream().filter(e -> "SKIPPED".equalsIgnoreCase(nz(e.status, ""))).count();
            m.avgConfidence = m.events.stream().mapToDouble(e -> e.confidence).average().orElse(0);
            m.avgDurationMs = Math.round(m.events.stream().mapToLong(e -> e.durationMs).average().orElse(0));

            m.byPage = m.events.stream().collect(Collectors.groupingBy(
                    e -> nz(e.page, "(unknown)"),
                    LinkedHashMap::new,
                    Collectors.counting()
            ));
            m.byTest = m.events.stream().collect(Collectors.groupingBy(
                    e -> nz(e.testCase, "(unknown)"),
                    LinkedHashMap::new,
                    Collectors.counting()
            ));

            m.topSlow = m.events.stream()
                    .sorted((a, b) -> Long.compare(nz(b.durationMs, 0L), nz(a.durationMs, 0L)))
                    .limit(5).toList();

            m.lowConfidence = m.events.stream()
                    .sorted(Comparator.comparingDouble(a -> nz(a.confidence, 0.0)))
                    .limit(5).toList();

            return m;
        }

        private static String nz(String s, String d) {
            return (s == null || s.isBlank()) ? d : s;
        }

        private static long nz(Long v, long d) {
            return v == null ? d : v;
        }

        private static double nz(Double v, double d) {
            return v == null ? d : v;
        }
    }
}
