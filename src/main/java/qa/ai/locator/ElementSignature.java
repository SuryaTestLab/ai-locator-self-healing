package qa.ai.locator;

import java.util.*;

public class ElementSignature {
    public String tag;
    public String text;
    public String id;
    public String name;
    public String testid;
    public String ariaLabel;
    public String placeholder;
    public String classes;
    public String href;
    public String type;
    public String lastKnownCss;
    public String url;
    public String nearText;

    /** tokens useful for refinding across runs */
    public Set<String> tokenHints() {
        LinkedHashSet<String> t = new LinkedHashSet<>();
        addTokens(t, tag, text, id, name, testid, ariaLabel, placeholder, classes, type, nearText);
        return t.stream().filter(s -> s.length()>=3).limit(10)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static void addTokens(Set<String> set, String... vals) {
        for (String v : vals) {
            if (v == null) continue;
            String s = v.toLowerCase().replaceAll("[^a-z0-9_\\-]+"," ");
            for (String p : s.split("\\s+")) {
                if (p.length() >= 3) {
                    set.add(p);
                    if (p.contains("-") || p.contains("_")) set.addAll(Arrays.asList(p.split("[-_]")));
                }
            }
        }
    }
}
