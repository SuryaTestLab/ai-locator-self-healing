// DemoTest.java
package qa.ai.locator;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import qa.ai.healing.HealingEventLogger;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class DemoTest {
    WebDriver driver;
    SignatureStore store;

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0)); // prefer explicit
        store = new SignatureStore(Path.of("signatures.json"));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
        HealingEventLogger.flush();
        try {
            qa.ai.healing.reporting.ReportMain.generateReport(new String[0]);

            // Open the generated HTML report in the default browser
            File htmlFile = Paths.get("out/healing-report.html").toFile();
            if (htmlFile.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(htmlFile.toURI());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void login_with_self_healing() {
        driver.get("https://www.example.com/login/");
        driver.manage().window().maximize();

        // Primary locator may break later; SelfHealingBy will recover
        By username = new SelfHealingBy(By.id("input_personal-id"), "login.username.input", store);
        By password = new SelfHealingBy(By.id("input_password"), "login.password.input", store);
        By submit = new SelfHealingBy(By.id("login-button"), "login.submit.button", store);

        driver.findElement(username).sendKeys("hi");
        driver.findElement(password).sendKeys("bye");
        driver.findElement(submit).click();
        System.out.println(driver.getTitle());
    }
}
