// DemoTest.java
package qa.ai.locator;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.nio.file.Path;
import java.time.Duration;

public class DemoTest {
    WebDriver driver;
    SignatureStore store;

    @BeforeEach
    void setUp(){
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0)); // prefer explicit
        store = new SignatureStore(Path.of("signatures.json"));
    }

    @AfterEach
    void tearDown(){ if (driver!=null) driver.quit(); }

    @Test
    void login_with_self_healing(){
        driver.get("https://www.example.com/login");
        driver.manage().window().maximize();

        // Primary locator may break later; SelfHealingBy will recover
        By username = new SelfHealingBy(By.id("modified1"), "login.username.input", store);
        By password = new SelfHealingBy(By.id("modified2"), "login.password.input", store);
        By submit   = new SelfHealingBy(By.id("modified3"), "login.submit.button", store);

        driver.findElement(username).sendKeys("hi");
        driver.findElement(password).sendKeys("bye");
        driver.findElement(submit).click();
        System.out.println(driver.getTitle());
    }
}
