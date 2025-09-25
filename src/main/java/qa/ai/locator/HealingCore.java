package qa.ai.locator;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static qa.ai.locator.SelfHealingBy.ATTRS;
import static qa.ai.locator.Utils.*;
import static qa.ai.locator.Utils.captureScreenshot;
import static qa.ai.locator.Utils.cssEsc;
import static qa.ai.locator.Utils.domSnapshot;
import static qa.ai.locator.Utils.notBlank;
import static qa.ai.locator.Utils.outerHtml;
import static qa.ai.locator.Utils.xpText;
//import static sun.nio.ch.IOStatus.normalize;

public class HealingCore {


    /* ======== Healing Core ======== */
    public Utils.HealingAttempt heal(WebDriver driver, ElementSignature sig, By primary, boolean requireVisible, boolean captureScreenshotOnHeal) {
        Utils.HealingAttempt res = new Utils.HealingAttempt();

        // tokens from primary + signature hints
        LinkedHashSet<String> tokens = new LinkedHashSet<>(extractTokens(primary.toString()));
        if (sig != null) tokens.addAll(sig.tokenHints());

        // candidate order: signature-first, then token-based
        LinkedHashSet<By> candidates = new LinkedHashSet<>();
        if (sig != null) candidates.addAll(candidatesFromSignature(sig));
        candidates.addAll(candidatesFromTokens(tokens));
        res.candidatesTried = candidates.size();

        WebElement bestEl = null;
        By bestBy = null;
        long bestScore = Long.MIN_VALUE;

        for (By c : candidates) {
            List<WebElement> found = safeFind(driver, c);
            if (found.size() != 1) continue;
            WebElement el = found.get(0);
            if (requireVisible && !isDisplayedSafe(el)) continue;
            HeuristicScorer heuristicScorer = new HeuristicScorer();
            long score = heuristicScorer.heuristicScore(el, tokens);
            if (sig != null) score += heuristicScorer.similarityScore(el, sig);

            if (score > bestScore) {
                bestScore = score;
                bestEl = el;
                bestBy = c;
            }
            if (bestScore >= 1500) break; // early exit on excellent match
        }

        if (bestEl != null) {
            res.resolvedElement = bestEl;
            res.resolvedBy = bestBy;
//            res.confidence = normalize(bestScore);
            res.confidence = Math.max(0, Math.min(1, bestScore / 2000.0));

            res.reason = "Healed via " + (sig != null ? "signature+tokens" : "tokens");
            res.domSnippet = outerHtml(driver, bestEl, 3000);
            res.screenshotBase64 = captureScreenshot(driver, captureScreenshotOnHeal);
        } else {
            res.resolvedElement = null;
            res.resolvedBy = null;
            res.confidence = 0.0;
            res.reason = "No unique visible candidate";
            res.domSnippet = domSnapshot(driver, 2000);
            res.screenshotBase64 = "";
        }

        return res;
    }

    private List<By> candidatesFromSignature(ElementSignature s) {
        List<By> out = new ArrayList<>();
        if (notBlank(s.testid)) out.add(By.cssSelector("[data-testid='" + cssEsc(s.testid) + "']"));
        if (notBlank(s.id)) out.add(By.id(s.id));
        if (notBlank(s.name)) out.add(By.name(s.name));
        if (notBlank(s.ariaLabel)) out.add(By.cssSelector("[aria-label*='" + cssEsc(s.ariaLabel) + "']"));
        if (notBlank(s.placeholder)) out.add(By.cssSelector("input[placeholder*='" + cssEsc(s.placeholder) + "']"));
        if (notBlank(s.lastKnownCss)) out.add(By.cssSelector(s.lastKnownCss));

        if (notBlank(s.text)) {
            String tag = notBlank(s.tag) ? s.tag : "*";
            out.add(By.xpath("//" + tag + "[contains(normalize-space(.)," + xpText(s.text) + ")]"));
            out.add(By.xpath("//button[contains(normalize-space(.)," + xpText(s.text) + ")]"));
            out.add(By.xpath("//a[contains(normalize-space(.)," + xpText(s.text) + ")]"));
            out.add(By.xpath("//label[contains(normalize-space(.)," + xpText(s.text) + ")]"));
        }
        if (notBlank(s.nearText)) {
            out.add(By.xpath("//*[contains(normalize-space(.)," + xpText(s.nearText) + ")]/following::*[self::input or self::button][1]"));
        }
        return out;
    }

    private List<By> candidatesFromTokens(Set<String> tokens) {
        List<By> out = new ArrayList<>();
        for (String t : tokens) {
            String tl = t.toLowerCase();
            for (String a : ATTRS) out.add(By.cssSelector("[" + a + "*='" + cssEsc(t) + "']"));
            out.add(By.xpath("//*[contains(translate(@id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + tl + "')]"));
            out.add(By.xpath("//*[contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + tl + "')]"));
            out.add(By.xpath("//*[contains(translate(@data-testid,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + tl + "')]"));
            out.add(By.xpath("//button[contains(normalize-space(.)," + xpText(t) + ")]"));
            out.add(By.xpath("//a[contains(normalize-space(.)," + xpText(t) + ")]"));
            out.add(By.xpath("//label[contains(normalize-space(.)," + xpText(t) + ")]"));
        }
        return out;
    }

}
