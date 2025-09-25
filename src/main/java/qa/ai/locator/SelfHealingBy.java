package qa.ai.locator;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import qa.ai.healing.HealingEvent;
import qa.ai.healing.HealingEventLogger;

import java.util.*;

import static qa.ai.locator.Utils.*;
//import static sun.nio.ch.IOStatus.normalize;

/**
 * SelfHealingBy with SignatureStore + HealingEventLogger + HTML report compatibility.
 */
public class SelfHealingBy extends By {

    private final By primary;
    private final String alias;                 // stable key in SignatureStore (e.g., "login.username.input")
    private final SignatureStore signatures;

    private final long healTimeoutMs;           // reserved for future waits
    private final boolean requireVisible;
    private final boolean captureScreenshotOnHeal;

    static final String[] ATTRS = new String[]{
            "id", "name", "data-testid", "data-test", "data-qa", "aria-label", "placeholder", "title", "value", "alt"
    };

    public SelfHealingBy(By primary, String alias, SignatureStore signatures) {
        this(primary, alias, signatures, 5000, true, true);
    }

    public SelfHealingBy(By primary, String alias, SignatureStore signatures,
                         long healTimeoutMs, boolean requireVisible, boolean captureScreenshotOnHeal) {
        this.primary = Objects.requireNonNull(primary, "primary By is required");
        this.alias = normalizeAlias(alias);
        this.signatures = Objects.requireNonNull(signatures, "SignatureStore is required");
        this.healTimeoutMs = healTimeoutMs;
        this.requireVisible = requireVisible;
        this.captureScreenshotOnHeal = captureScreenshotOnHeal;
    }

    @Override
    public WebElement findElement(SearchContext context) {
        long runStart = System.nanoTime();
        if (!(context instanceof WebDriver driver)) {
            // Healing requires a WebDriver; for nested contexts, delegate to Selenium
            return context.findElement(primary);
        }

        // Try primary first
        try {
            WebElement el = context.findElement(primary);
            // refresh signature on success
            ElementSignature sig = buildSignature(driver, el);
            signatures.put(alias, sig);
            return el;
        } catch (NoSuchElementException | StaleElementReferenceException ignored) {
        }

        // Heal using stored signature (if any) + generic tokens
        ElementSignature stored = signatures.get(alias).orElse(null);
        HealingAttempt attempt = new HealingCore().heal(driver, stored, primary, requireVisible, captureScreenshotOnHeal);

        // Log to healing-events.json (consumed by the HTML report tool)
        HealingEvent ev = new HealingEvent();
        ev.testCase = System.getProperty("current.test.name", "(unknown)");
        ev.page = safeCall(driver::getCurrentUrl, "(unknown)");
        ev.originalLocator = primary.toString();
        ev.healedLocator = attempt.resolvedBy != null ? attempt.resolvedBy.toString() : "";
        ev.confidence = attempt.confidence;
        ev.status = attempt.resolvedElement != null ? "USED" : "FAILED";
        ev.reason = attempt.reason;
        ev.durationMs = (System.nanoTime() - runStart) / 1_000_000;
        ev.domSnippet = attempt.domSnippet != null ? attempt.domSnippet : domSnapshot(driver, 2000);
        ev.screenshotBase64 = attempt.screenshotBase64;
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("candidateCount", attempt.candidatesTried);
        extra.put("hadSignature", stored != null);
        ev.extra = extra;
        HealingEventLogger.log(ev);

        if (attempt.resolvedElement != null) {
            ElementSignature healed = buildSignature(driver, attempt.resolvedElement);
            signatures.put(alias, healed);
            return attempt.resolvedElement;
        }

        throw new NoSuchElementException("SelfHealingBy heal failed for alias=" + alias + " primary=" + primary);
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
        try {
            return context.findElements(primary);
        } catch (Exception e) {
            WebElement el = findElement(context);
            return el != null ? List.of(el) : List.of();
        }
    }


    /* ======== Signature build ======== */
    private ElementSignature buildSignature(WebDriver driver, WebElement el) {
        ElementSignature s = new ElementSignature();
        try {
            s.tag = lower(el.getTagName());
            s.text = safe(el.getText());
            s.id = attr(el, "id");
            s.name = attr(el, "name");
            s.testid = attr(el, "data-testid");
            s.ariaLabel = attr(el, "aria-label");
            s.placeholder = attr(el, "placeholder");
            s.classes = attr(el, "class");
            s.href = attr(el, "href");
            s.type = attr(el, "type");
            s.lastKnownCss = bestCss(el);
            s.url = safeCall(driver::getCurrentUrl, "");
            s.nearText = nearText(driver, el);
        } catch (Exception ignore) {
        }
        return s;
    }


}
