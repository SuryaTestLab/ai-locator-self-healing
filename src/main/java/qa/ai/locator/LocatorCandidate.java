package qa.ai.locator;

import org.openqa.selenium.By;

public class LocatorCandidate {
    public enum Type { CSS, XPATH }
    public final Type type;
    public final String value;
    public final double heuristicScore; // optional pre-score

    public LocatorCandidate(Type type, String value, double heuristicScore) {
        this.type = type; this.value = value; this.heuristicScore = heuristicScore;
    }
    public By toBy() {
        return type == Type.CSS ? By.cssSelector(value) : By.xpath(value);
    }
    @Override public String toString(){ return type+":"+value; }
}
