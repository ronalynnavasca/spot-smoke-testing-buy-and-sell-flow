package utils;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class BaseTest {

    protected AndroidDriver driver;

    @BeforeMethod
    public void setUp() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();

        // Device capabilities
        options.setDeviceName("Android Device");       // Your physical device name
        options.setUdid("56191FDCR00181");               // Replace with: adb devices
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");

        // App capabilities - choose ONE of the following:

        // Option 1: Install and launch an APK file
        // options.setApp("/path/to/your/app.apk");

        // Option 2: Launch an already-installed app
        options.setAppPackage("com.android.settings");  // Example: Settings app
        options.setAppActivity(".Settings");             // Main activity

        // Timeouts
        options.setNewCommandTimeout(Duration.ofSeconds(60));

        // Connect to Appium server
        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
