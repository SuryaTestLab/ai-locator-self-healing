package qa.ai.locator;

import org.openqa.selenium.*;

import java.util.List;

/* ======== Utils ======== */
public class Utils {

    static class HealingAttempt {
        WebElement resolvedElement;
        By resolvedBy;
        String reason;
        double confidence;
        int candidatesTried;
        String domSnippet;
        String screenshotBase64;
    }

    static String normalizeAlias(String a) {
        String x = (a == null) ? "" : a.trim();
        return x.isEmpty() ? "alias-" + java.util.UUID.randomUUID() : x;
    }

    static List<WebElement> safeFind(WebDriver d, By by) {
        try {
            return d.findElements(by);
        } catch (Exception e) {
            return java.util.List.of();
        }
    }

    static boolean isDisplayedSafe(WebElement el) {
        try {
            return el.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public static String attr(WebElement el, String k) {
        try {
            return el.getAttribute(k);
        } catch (Exception e) {
            return "";
        }
    }

    public static String lower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    public static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    static <T> T safeCall(java.util.concurrent.Callable<T> c, T d) {
        try {
            T r = c.call();
            return r == null ? d : r;
        } catch (Exception e) {
            return d;
        }
    }

    public static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    public static boolean eq(String a, String b) {
        return java.util.Objects.equals(safe(a), safe(b));
    }

    public static boolean contains(String a, String b) {
        return notBlank(a) && notBlank(b) && a.toLowerCase().contains(b.toLowerCase());
    }

    public static int overlap(String a, String b) {
        if (!notBlank(a) || !notBlank(b)) return 0;
        java.util.Set<String> sa = new java.util.HashSet<>(java.util.Arrays.asList(a.split("\\s+")));
        java.util.Set<String> sb = new java.util.HashSet<>(java.util.Arrays.asList(b.split("\\s+")));
        sa.retainAll(sb);
        return sa.size();
    }

    static java.util.Set<String> extractTokens(String s) {
        java.util.LinkedHashSet<String> out = new java.util.LinkedHashSet<>();
        if (s == null) return out;
        s = s.toLowerCase().replaceAll("[^a-z0-9_\\-]+", " ");
        for (String p : s.split("\\s+")) {
            if (p.length() < 3) continue;
            if (java.util.Set.of("by", "id", "xpath", "cssselector", "name", "classname", "linktext").contains(p))
                continue;
            out.add(p);
            if (p.contains("-") || p.contains("_")) out.addAll(java.util.Arrays.asList(p.split("[-_]")));
        }
        return out.stream().limit(8).collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    static String bestCss(WebElement el) {
        String id = attr(el, "id");
        if (notBlank(id)) return "#" + id;
        String testid = attr(el, "data-testid");
        if (notBlank(testid)) return "[data-testid='" + cssEsc(testid) + "']";
        String name = attr(el, "name");
        if (notBlank(name)) return "[name='" + cssEsc(name) + "']";
        String tag = lower(el.getTagName());
        String cls = attr(el, "class");
        if (notBlank(cls)) {
            String first = cls.trim().split("\\s+")[0];
            return tag + "." + first;
        }
        return tag;
    }

    static String nearText(WebDriver d, WebElement el) {
        try {
            return (String) ((JavascriptExecutor) d).executeScript(
                    "const e=arguments[0];function t(n){return (n&&n.innerText)||''} let x=''; if(e&&e.previousElementSibling){x=t(e.previousElementSibling);} if(!x&&e&&e.parentElement){x=t(e.parentElement);} return x.length>120?x.substring(0,120):x;", el);
        } catch (Exception e) {
            return "";
        }
    }

    static String outerHtml(WebDriver d, WebElement el, int max) {
        try {
            String html = (String) ((JavascriptExecutor) d).executeScript("return arguments[0].outerHTML;", el);
            if (html == null) return "";
            return html.length() > max ? html.substring(0, max) + "…" : html;
        } catch (Exception e) {
            return "";
        }
    }

    static String domSnapshot(WebDriver d, int max) {
        try {
            String html = (String) ((JavascriptExecutor) d).executeScript("return new XMLSerializer().serializeToString(document.documentElement);");
            if (html == null) return "";
            return html.length() > max ? html.substring(0, max) + "…" : html;
        } catch (Exception e) {
            return "";
        }
    }

    static String captureScreenshot(WebDriver d, boolean enable) {
        if (!enable) return "";
        try {
            return ((TakesScreenshot) d).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            return "";
        }
    }

    static String cssEsc(String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
    }

    static String xpText(String s) {
        if (!s.contains("'")) return "'" + s + "'";
        if (!s.contains("\"")) return "\"" + s + "\"";
        String[] parts = s.split("'");
        StringBuilder sb = new StringBuilder("concat(");
        for (int i = 0; i < parts.length; i++) {
            sb.append("'").append(parts[i]).append("'");
            if (i < parts.length - 1) sb.append(",\"'\",");
        }
        sb.append(")");
        return sb.toString();
    }
}
