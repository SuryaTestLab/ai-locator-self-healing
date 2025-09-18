package qa.ai.locator;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class DomSnapshot {
    public static String snapshot(WebDriver driver) {
        String script = """
          return new XMLSerializer().serializeToString(document);
        """;
        return (String)((JavascriptExecutor)driver).executeScript(script);
    }
}
