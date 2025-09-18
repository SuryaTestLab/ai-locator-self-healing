package qa.ai.locator;

import org.openqa.selenium.*;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import java.util.*;

public class HeuristicScorer {

    public static LocatorCandidate pickBest(WebDriver driver,
                                            List<LocatorCandidate> candidates,
                                            ElementSignature previous){
        JaroWinklerSimilarity jw = new JaroWinklerSimilarity();
        LocatorCandidate best = null;
        double bestScore = -1;

        for (LocatorCandidate c: candidates){
            try {
                List<WebElement> found = driver.findElements(c.toBy());
                if (found.size()!=1) continue; // want unique
                WebElement el = found.get(0);

                double score = c.heuristicScore;

                if (previous != null){
                    // text similarity
                    String nowText = safe(() -> el.getText());
                    score += 0.3 * nz(jw.apply(previous.text==null?"":previous.text, nowText));

                    // attribute overlap
                    score += 0.2 * attrOverlap(el, previous);

                    // role/neighbor hints
                    score += 0.1 * neighborBoost(driver, el, previous);
                }

                if (score > bestScore){
                    bestScore = score;
                    best = c;
                }
            } catch (Exception ignore){}
        }
        return best;
    }

    private static double attrOverlap(WebElement el, ElementSignature prev){
        int total=0, match=0;
        for (Map.Entry<String,String> e: prev.attrs.entrySet()){
            total++;
            String vNow = safe(() -> el.getAttribute(e.getKey()));
            if (vNow!=null && !vNow.isBlank() && vNow.equalsIgnoreCase(e.getValue())) match++;
        }
        return total==0? 0 : (double)match/total; // 0..1
    }

    private static double neighborBoost(WebDriver d, WebElement el, ElementSignature prev){
        // simplistic: if left/above neighbor text still matches partially, boost
        return 0.0; // keep simple for starter; you can use getBoundingClientRect JS like in ElementSignature
    }

    private static String safe(java.util.concurrent.Callable<String> c){
        try { return Optional.ofNullable(c.call()).orElse(""); } catch(Exception e){ return ""; }
    }
    private static double nz(Double d){ return d==null? 0.0: d; }
}
