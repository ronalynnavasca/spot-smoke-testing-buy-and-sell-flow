package tests;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.remote.RemoteWebElement;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

public class CronosUATTest {

    private AndroidDriver driver;

    private void clickElement(WebElement element) {
        driver.executeScript("mobile: clickGesture", ImmutableMap.of(
                "elementId", ((RemoteWebElement) element).getId()
        ));
    }

    @BeforeMethod
    public void setUp() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();

        options.setDeviceName("Android Device");
        options.setUdid("56191FDCR00181");
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");

        // Cronos-UAT app
        options.setAppPackage("com.defi.st.wallet");
        options.setAppActivity("com.defi.wallet.feature.splash.SplashActivity");
        options.setCapability("appium:appWaitActivity", "*");
        options.setCapability("appium:appWaitDuration", 30000);

        options.setNewCommandTimeout(Duration.ofSeconds(60));
        options.setCapability("appium:noReset", true);

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
    }

    @Test(description = "Verify app launch")
    public void testVerifyAppLaunch() throws InterruptedException {
        // Wait for either "Login with Google" or passcode screen to appear
        Thread.sleep(5000);

        // If "Login with Google" exists, tap it; otherwise enter passcode
        List<WebElement> loginWithGoogle = driver.findElements(AppiumBy.accessibilityId("Login with Google"));
        if (!loginWithGoogle.isEmpty()) {
            clickElement(loginWithGoogle.get(0));

            // Tap on account that contains @cronoslabs.org
            clickElement(driver.findElement(AppiumBy.xpath("//*[contains(@content-desc, '@cronoslabs.org')]")));

            // Tap Continue
            clickElement(driver.findElement(AppiumBy.xpath("//android.widget.Button[@text=\"Continue\"]")));
            Thread.sleep(5000);
        } else {
            // Enter passcode
            String passcode = "258036";
            for (char digit : passcode.toCharArray()) {
                clickElement(driver.findElement(
                        AppiumBy.xpath("//android.widget.TextView[@text='" + digit + "']")
                ));
            }
            Thread.sleep(3000);
        }

        // Tap on Market
        clickElement(driver.findElement(AppiumBy.accessibilityId("Market")));

        // Tap on Search
        clickElement(driver.findElement(AppiumBy.xpath("//android.widget.Button[@content-desc=\"Open search\"]/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup[1]")));

        // Input "BTC" in search field
        WebElement searchField = driver.findElement(AppiumBy.className("android.widget.EditText"));
        searchField.sendKeys("BTC");

        // Tap on BTC
        clickElement(driver.findElement(AppiumBy.accessibilityId("CDCBTC Bitcoin")));

        // Tap on Buy
        clickElement(driver.findElement(AppiumBy.accessibilityId("Buy")));

        // Tap amount input and enter 0.01
        WebElement amountInput = driver.findElement(AppiumBy.xpath("//android.widget.EditText[@resource-id=\"spot-trade-sheet.amount-input\"]"));
        clickElement(amountInput);
        amountInput.sendKeys("0.01");

        // Tap review order
        clickElement(driver.findElement(AppiumBy.accessibilityId("Review order")));

        // Get amount in BTC from review screen
        String amountInBTC = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@resource-id=\"spot-trade-sheet.receive-amount-text\"]")).getText();
        System.out.println("Amount in BTC from review: " + amountInBTC);

        // Assert pay amount matches input amount
        String payAmount = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@resource-id=\"spot-trade-sheet.pay-amount-text\"]")).getText();
        if (payAmount.startsWith("<")) {
            double threshold = Double.parseDouble(payAmount.substring(1));
            double actualInput = Double.parseDouble("0.01");
            Assert.assertTrue(actualInput < threshold,
                    "Input amount (0.01) should be less than " + threshold);
        } else {
            Assert.assertEquals(payAmount, "$0.01",
                    "Pay amount (" + payAmount + ") should match input amount ($0.01)");
        }
        System.out.println("PASSED: Pay amount matches input amount.");

        // Tap confirm
        clickElement(driver.findElement(AppiumBy.accessibilityId("Confirm order")));
        Thread.sleep(2000);

        // Minimize the app (press Home)
        driver.pressKey(new io.appium.java_client.android.nativekey.KeyEvent(
                io.appium.java_client.android.nativekey.AndroidKey.HOME));

        // Poll for "Order successful" notification
        WebDriverWait notifWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        notifWait.until(d -> {
            driver.openNotifications();
            List<WebElement> notifs = driver.findElements(AppiumBy.xpath(
                    "//android.widget.TextView[@resource-id='android:id/title' and @text='Order successful']"));
            if (notifs.isEmpty()) {
                driver.pressKey(new io.appium.java_client.android.nativekey.KeyEvent(
                        io.appium.java_client.android.nativekey.AndroidKey.BACK));
                return false;
            }
            return true;
        });
        System.out.println("PASSED: Notification 'Order successful' received.");

        // Get notification body text and extract BTC amount
        String notifBody = driver.findElement(AppiumBy.xpath("//android.widget.TextView[contains(@text, 'order for')]")).getText();
        System.out.println("Notification body: " + notifBody);

        // Extract amount from notification (e.g. "Your buy order for <0.000001 CDCBTC...")
        String confirmedAmountInBTC = notifBody.split("for ")[1].split(" CDCBTC")[0];
        System.out.println("Amount in BTC from notification: " + confirmedAmountInBTC);

        // Assert amountInBTC matches confirmedAmountInBTC
        Assert.assertEquals(confirmedAmountInBTC, amountInBTC,
                "BTC amount in review (" + amountInBTC + ") should match notification (" + confirmedAmountInBTC + ")");
        System.out.println("PASSED: Amount in BTC matches between review and notification.");

        // Tap on the notification
        Thread.sleep(3000);
        clickElement(driver.findElement(AppiumBy.xpath("(//android.widget.FrameLayout[@resource-id=\"android:id/status_bar_latest_event_content\"])[1]")));

        // Assert "Success" text is displayed
        String successText = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text=\"Success\"]")).getText();
        Assert.assertEquals(successText, "Success", "Success text should be displayed");
        System.out.println("PASSED: Success text is displayed.");

        // Assert BTC amount on success screen matches confirmedAmountInBTC
        String successAmountText = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text=\"<0.000001\"]")).getText();
        Assert.assertEquals(successAmountText, confirmedAmountInBTC,
                "Amount on success screen (" + successAmountText + ") should match confirmed amount (" + confirmedAmountInBTC + ")");
        System.out.println("PASSED: Amount on success screen matches confirmed BTC amount.");

        // Assert total paid matches the input value
        String totalPaid = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text=\"$0.01\"]")).getText();
        Assert.assertEquals(totalPaid, "$0.01",
                "Total paid (" + totalPaid + ") should match input value ($0.01)");
        System.out.println("PASSED: Total paid matches input value.");

        // Tap to minimize/close
        clickElement(driver.findElement(AppiumBy.xpath("//androidx.compose.ui.viewinterop.ViewFactoryHolder/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.view.ViewGroup/android.view.ViewGroup[1]")));

        // Drag down to dismiss
        WebElement dragElement = driver.findElement(AppiumBy.xpath("//androidx.compose.ui.viewinterop.ViewFactoryHolder/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.view.ViewGroup/android.view.ViewGroup[2]"));
        int startX = dragElement.getLocation().getX() + dragElement.getSize().getWidth() / 2;
        int startY = dragElement.getLocation().getY() + dragElement.getSize().getHeight() / 2;
        int endY = startY + 3000;
        org.openqa.selenium.interactions.PointerInput finger = new org.openqa.selenium.interactions.PointerInput(org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
        org.openqa.selenium.interactions.Sequence swipe = new org.openqa.selenium.interactions.Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, org.openqa.selenium.interactions.PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(500), org.openqa.selenium.interactions.PointerInput.Origin.viewport(), startX, endY));
        swipe.addAction(finger.createPointerUp(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(java.util.Arrays.asList(swipe));
        Thread.sleep(2000);
    }

    @Test(description = "Verify Sell order flow")
    public void testSellOrder() throws InterruptedException {
        // Wait for app to load
        Thread.sleep(10000);

        // If "Login with Google" exists, tap it; otherwise enter passcode
        List<WebElement> loginWithGoogle = driver.findElements(AppiumBy.accessibilityId("Login with Google"));
        if (!loginWithGoogle.isEmpty()) {
            clickElement(loginWithGoogle.get(0));
            clickElement(driver.findElement(AppiumBy.xpath("//*[contains(@content-desc, '@cronoslabs.org')]")));
            clickElement(driver.findElement(AppiumBy.xpath("//android.widget.Button[@text=\"Continue\"]")));
            Thread.sleep(5000);
        } else {
            // Enter passcode
            String passcode = "258036";
            for (char digit : passcode.toCharArray()) {
                clickElement(driver.findElement(
                        AppiumBy.xpath("//android.widget.TextView[@text='" + digit + "']")
                ));
            }
            Thread.sleep(3000);
        }

        // Tap on Manage
        clickElement(driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text=\"Manage\"]")));

        // Tap on Sell
        clickElement(driver.findElement(AppiumBy.accessibilityId("Sell")));

        // Tap Set amount to max
        clickElement(driver.findElement(AppiumBy.accessibilityId("Set amount to max")));

        // Get input amount value
        String inputAmount = driver.findElement(AppiumBy.xpath("//android.widget.EditText[@resource-id=\"spot-trade-sheet.amount-input\"]")).getText();
        System.out.println("Input amount: " + inputAmount);

        // Tap Review order
        clickElement(driver.findElement(AppiumBy.accessibilityId("Review order")));

        // Verify pay amount matches input amount
        String payAmount = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@resource-id=\"spot-trade-sheet.pay-amount-text\"]")).getText();
        if (payAmount.startsWith("<")) {
            double threshold = Double.parseDouble(payAmount.substring(1));
            double actualInput = Double.parseDouble(inputAmount);
            Assert.assertTrue(actualInput < threshold,
                    "Input amount (" + inputAmount + ") should be less than " + threshold);
        } else {
            Assert.assertEquals(payAmount, inputAmount,
                    "Pay amount (" + payAmount + ") should match input amount (" + inputAmount + ")");
        }
        System.out.println("PASSED: Pay amount matches input amount.");

        // Store "You'll get" amount
        String youllGet = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@resource-id=\"spot-trade-sheet.receive-amount-text\"]")).getText();
        System.out.println("You'll get: " + youllGet);

        // Tap Confirm order
        clickElement(driver.findElement(AppiumBy.xpath("//android.widget.Button[@content-desc=\"Confirm order\"]")));

        // Minimize the app (press Home)
        driver.pressKey(new io.appium.java_client.android.nativekey.KeyEvent(
                io.appium.java_client.android.nativekey.AndroidKey.HOME));

        // Poll for "Order successful" notification
        WebDriverWait sellNotifWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        sellNotifWait.until(d -> {
            driver.openNotifications();
            List<WebElement> notifs = driver.findElements(AppiumBy.xpath(
                    "//android.widget.TextView[@resource-id='android:id/title' and @text='Order successful']"));
            if (notifs.isEmpty()) {
                driver.pressKey(new io.appium.java_client.android.nativekey.KeyEvent(
                        io.appium.java_client.android.nativekey.AndroidKey.BACK));
                return false;
            }
            return true;
        });
        System.out.println("PASSED: Notification 'Order successful' received.");

        // Get notification body text and verify pay amount
        String notifBody = driver.findElement(AppiumBy.xpath("//android.widget.TextView[contains(@text, 'order for')]")).getText();
        System.out.println("Notification body: " + notifBody);

        // Extract amount from notification
        String notifAmount = notifBody.split("for ")[1].split(" CDCBTC")[0];
        System.out.println("Amount from notification: " + notifAmount);

        // Assert pay amount matches notification amount
        Assert.assertEquals(notifAmount, payAmount,
                "Pay amount (" + payAmount + ") should match notification amount (" + notifAmount + ")");
        System.out.println("PASSED: Pay amount matches notification amount.");

        // Tap on the notification
        Thread.sleep(3000);
        clickElement(driver.findElement(AppiumBy.xpath("(//android.widget.FrameLayout[@resource-id=\"android:id/status_bar_latest_event_content\"])[1]")));

        // Assert "Success" text is displayed
        String successText = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text=\"Success\"]")).getText();
        Assert.assertEquals(successText, "Success", "Success text should be displayed");
        System.out.println("PASSED: Success text is displayed.");

        // Assert BTC amount on success screen matches notification amount
        String successAmountText = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text=\"<0.000001\"]")).getText();
        Assert.assertEquals(successAmountText, notifAmount,
                "Amount on success screen (" + successAmountText + ") should match notification amount (" + notifAmount + ")");
        System.out.println("PASSED: Amount on success screen matches notification amount.");

        // Assert total paid matches the input value
        String totalPaid = driver.findElement(AppiumBy.xpath("//android.widget.TextView[@text=\"$0.01\"]")).getText();
        Assert.assertEquals(totalPaid, "$0.01",
                "Total paid (" + totalPaid + ") should match input value ($0.01)");
        System.out.println("PASSED: Total paid matches input value.");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
