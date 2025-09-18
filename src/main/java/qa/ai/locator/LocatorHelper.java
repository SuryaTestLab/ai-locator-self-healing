package qa.ai.locator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.util.*;

public class LocatorHelper {

    public static List<LocatorCandidate> generateCandidates(String html, String key){
        Document doc = Jsoup.parse(html);
        List<LocatorCandidate> list = new ArrayList<>();

        // 1) data-testid / id / name / aria-label / placeholder
        addByAttr(doc, list, "data-testid");
        addByAttr(doc, list, "id");
        addByAttr(doc, list, "name");
        addByAttr(doc, list, "aria-label");
        addByAttr(doc, list, "placeholder");

        // 2) label-for relationship
        Elements labels = doc.select("label[for]");
        for (Element lab: labels){
            String forId = lab.attr("for");
            if (!forId.isBlank()){
                list.add(new LocatorCandidate(LocatorCandidate.Type.CSS, "#"+cssEscape(forId), score(0.92)));
            }
        }

        // 3) role-based
        String[] roles = {"button","textbox","link","menuitem","combobox"};
        for (String role: roles){
            Elements els = doc.select("[role="+role+"]");
            for (Element e: els){
                list.add(new LocatorCandidate(LocatorCandidate.Type.CSS,
                        "[role='"+role+"']"+withTextContains(e.ownText()), score(0.75)));
            }
        }

        // 4) class with text
        for (Element e: doc.getAllElements()){
            if (e.hasText() && e.className()!=null && !e.className().isBlank()){
                String txt = trimText(e.text());
                if (!txt.isBlank())
                    list.add(new LocatorCandidate(LocatorCandidate.Type.XPATH,
                            "//*[contains(@class,'"+firstClass(e.className())+"') and contains(normalize-space(.),'"+xpathEscape(txt)+"')]",
                            score(0.65)));
            }
        }

        // 5) tag + nth-of-type (fallback)
        for (Element e: doc.select("input,button,a,select,textarea")){
            String css = cssNthOfType(e);
            list.add(new LocatorCandidate(LocatorCandidate.Type.CSS, css, score(0.40)));
        }

        // Deduplicate by value
        Map<String, LocatorCandidate> dedup = new LinkedHashMap<>();
        for (LocatorCandidate c: list) dedup.put(c.type+":"+c.value, c);
        return new ArrayList<>(dedup.values());
    }

    private static void addByAttr(Document doc, List<LocatorCandidate> out, String attr){
        Elements els = doc.select("["+attr+"]");
        for (Element e: els){
            String v = e.attr(attr);
            if (v.isBlank()) continue;
            out.add(new LocatorCandidate(LocatorCandidate.Type.CSS,
                    "["+attr+"='"+cssEscape(v)+"']", score(0.95)));
            out.add(new LocatorCandidate(LocatorCandidate.Type.XPATH,
                    "//*[@"+attr+"='"+xpathEscape(v)+"']", score(0.93)));
        }
    }

    // score helper
    private static double score(double base){ return base; }

    private static String cssEscape(String s){ return s.replace("'", "\\'"); }
    private static String xpathEscape(String s){ return s.replace("'", "&apos;"); }
    private static String withTextContains(String t){
        t = trimText(t);
        return t.isBlank()? "" : ":containsOwn("+t+")";
    }
    private static String trimText(String t){ return t==null? "": t.trim().replaceAll("\\s+"," ").substring(0, Math.min(40, t.length())); }
    private static String firstClass(String cls){ return cls.split("\\s+")[0]; }

    private static String cssNthOfType(Element e) {
        // approximate: tag:nth-of-type(n)
        Element parent = e.parent();
        if (parent==null) return e.tagName();
        int idx=1;
        for (Element sib: parent.children()){
            if (sib==e) break;
            if (sib.tagName().equals(e.tagName())) idx++;
        }
        return e.tagName()+":nth-of-type("+idx+")";
    }

    // hook to update "primary" at runtime if you keep a registry (no-op in this starter)
    public static void updatePrimary(SelfHealingBy self, LocatorCandidate best) { /* optional */ }
}
