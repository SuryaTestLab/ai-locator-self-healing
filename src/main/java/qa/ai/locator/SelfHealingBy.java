package qa.ai.locator;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;

import java.util.*;

public class SelfHealingBy extends By {
    private final By primary;
    private final String key; // stable ID for this element across runs (e.g., "login.username.input")
    private final SignatureStore store;

    public SelfHealingBy(By primary, String key, SignatureStore store) {
        this.primary = primary;
        this.key = key;
        this.store = store;
    }

    @Override
    public WebElement findElement(SearchContext context) {
        try {
            WebElement el = primary.findElement(context);
            persistSignature(context, el);
            return el;
        } catch (NoSuchElementException | StaleElementReferenceException fail) {
            if (!(context instanceof WebDriver driver)) {
                throw fail;
            }
            // 1) Snapshot DOM
            String html = DomSnapshot.snapshot(driver);

            // 2) Generate candidates
            List<LocatorCandidate> candidates = LocatorHelper.generateCandidates(html, key);

            // 3) Score candidates (heuristics + signature similarity)
            ElementSignature previous = store.load(key);
            LocatorCandidate best = HeuristicScorer.pickBest(driver, candidates, previous);

            if (best == null) throw fail;

            WebElement healed = best.toBy().findElement(driver);
            persistSignature(driver, healed);

            // 4) Optionally: update primary locator to healed one (in-memory)
            LocatorHelper.updatePrimary(this, best);
            return healed;
        }
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
        return List.of();
    }

    private void persistSignature(SearchContext ctx, WebElement el) {
        if (!(ctx instanceof WebDriver driver)) return;
        ElementSignature sig = ElementSignature.from(driver, el, key);
        store.save(sig);
    }
}
