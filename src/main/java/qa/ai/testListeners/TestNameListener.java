package qa.ai.testListeners;

import qa.ai.healing.HealingEventLogger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestNameListener implements ITestListener {
    @Override public void onTestStart(ITestResult result) {
        System.setProperty("current.test.name", result.getName());
    }
    @Override public void onFinish(ITestContext context) {
        HealingEventLogger.flush();
    }
}
