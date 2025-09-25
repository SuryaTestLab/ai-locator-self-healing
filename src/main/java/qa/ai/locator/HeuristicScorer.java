package qa.ai.locator;

import org.openqa.selenium.*;

import java.util.*;

import static qa.ai.locator.SelfHealingBy.*;
import static qa.ai.locator.Utils.*;

public class HeuristicScorer {

    public long heuristicScore(WebElement el, Set<String> tokens) {
        long score = 0;
        try {
            String tag = lower(el.getTagName());
            String text = safe(el.getText());
            String id = attr(el, "id"), name = attr(el, "name"), testid = attr(el, "data-testid");
            String aria = attr(el, "aria-label"), placeholder = attr(el, "placeholder"), cls = attr(el, "class");

            if (Set.of("input", "button", "a", "label", "select").contains(tag)) score += 60;
            if (notBlank(testid)) score += 120;
            if (notBlank(id)) score += 100;
            if (notBlank(name)) score += 80;
            if (notBlank(aria)) score += 70;
            if (notBlank(placeholder)) score += 50;

            String hay = String.join(" ", List.of(tag, text, id, name, testid, aria, placeholder, cls)).toLowerCase();
            for (String t : tokens)
                if (t.length() >= 2 && hay.contains(t.toLowerCase())) score += Math.min(180, 18 * t.length());
            if (el.isDisplayed()) score += 40;
            if (el.isEnabled()) score += 30;
        } catch (Exception ignore) {
        }
        return score;
    }

    public long similarityScore(WebElement el, ElementSignature s) {
        long score = 0;
        try {
            if (eq(attr(el, "id"), s.id)) score += 200;
            if (eq(attr(el, "name"), s.name)) score += 150;
            if (contains(attr(el, "data-testid"), s.testid)) score += 180;
            if (contains(attr(el, "aria-label"), s.ariaLabel)) score += 120;

            String text = safe(el.getText());
            if (contains(text, s.text)) score += Math.min(200, 10L * safe(s.text).length());

            score += overlap(attr(el, "class"), s.classes) * 10;
            if (eq(lower(el.getTagName()), s.tag)) score += 60;
        } catch (Exception ignore) {
        }
        return score;
    }

    private double normalize(long score) {
        double s = Math.max(0, Math.min(score, 1600));
        return s / 1600.0;
    }

}
