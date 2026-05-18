package tests;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterSuite;

public class spotBuyAndSellFlow {

    private AndroidDriver driver;

    private WebDriverWait wait;

    private int screenshotCounter = 0;

    private static List<String[]> reportEntries = new ArrayList<>();
    private String currentTestName = "";

    private void takeScreenshot(String stepName) {
        takeScreenshot(stepName, "PASSED");
    }

    private void takeScreenshot(String stepName, String status) {
        try {
            screenshotCounter++;
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("screenshots/%s_%02d_%s.png", timestamp, screenshotCounter, stepName);
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destFile = new File("target/" + fileName);
            destFile.getParentFile().mkdirs();
            Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved: " + destFile.getAbsolutePath());

            // Store for custom report
            byte[] fileContent = Files.readAllBytes(destFile.toPath());
            String base64Screenshot = Base64.getEncoder().encodeToString(fileContent);
            reportEntries.add(new String[]{currentTestName, stepName, base64Screenshot, status});

            // Also embed in TestNG report
            Reporter.log("<br><b>" + stepName + "</b> [" + status + "]<br>");
            Reporter.log("<img src='data:image/png;base64," + base64Screenshot + "' width='400px'/><br>");
        } catch (IOException e) {
            System.out.println("Failed to take screenshot: " + e.getMessage());
        }
    }

    @AfterSuite
    public void generateCustomReport() {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='utf-8'>");
            html.append("<title>Spot Buy & Sell Flow - Test Report</title>");
            html.append("<style>");
            html.append("body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }");
            html.append(".suite-header { background: #4CAF50; color: white; padding: 15px; border-radius: 5px; margin-bottom: 20px; }");
            html.append(".test-section { background: white; border: 1px solid #ddd; border-radius: 5px; margin-bottom: 20px; padding: 15px; }");
            html.append(".test-name { font-size: 16px; font-weight: bold; color: #333; border-bottom: 2px solid #eee; padding-bottom: 10px; margin-bottom: 15px; }");
            html.append(".messages-table { width: 100%; border-collapse: collapse; }");
            html.append(".messages-table th { background: #f0f0f0; padding: 10px; text-align: left; border: 1px solid #ddd; }");
            html.append(".messages-table td { padding: 10px; border: 1px solid #ddd; vertical-align: top; }");
            html.append(".step-name { font-weight: bold; font-size: 14px; margin-bottom: 5px; }");
            html.append(".screenshot img { max-width: 300px; border: 1px solid #ccc; border-radius: 3px; }");
            html.append(".status-passed { color: white; background: #4CAF50; padding: 5px 15px; border-radius: 3px; font-weight: bold; display: inline-block; }");
            html.append(".status-failed { color: white; background: #f44336; padding: 5px 15px; border-radius: 3px; font-weight: bold; display: inline-block; }");
            html.append("</style></head><body>");
            html.append("<div class='suite-header'><h2>Surefire Test Report</h2><p>Spot Buy & Sell Flow</p></div>");

            String lastTest = "";
            for (String[] entry : reportEntries) {
                String testName = entry[0];
                String stepName = entry[1];
                String base64 = entry[2];
                String status = entry[3];

                if (!testName.equals(lastTest)) {
                    if (!lastTest.isEmpty()) {
                        html.append("</table></div>");
                    }
                    html.append("<div class='test-section'>");
                    html.append("<div class='test-name'>tests.spotBuyAndSellFlow#").append(testName).append("</div>");
                    html.append("<table class='messages-table'>");
                    html.append("<tr><th>Cases</th><th>Screenshot</th><th>Status</th></tr>");
                    lastTest = testName;
                }

                String statusClass = status.equals("PASSED") ? "status-passed" : "status-failed";
                html.append("<tr>");
                html.append("<td><div class='step-name'>").append(stepName).append("</div></td>");
                html.append("<td class='screenshot'><img src='data:image/png;base64,").append(base64).append("'/></td>");
                html.append("<td><span class='").append(statusClass).append("'>").append(status).append("</span></td>");
                html.append("</tr>");
            }
            if (!lastTest.isEmpty()) {
                html.append("</table></div>");
            }
            html.append("</body></html>");

            File reportFile = new File("target/surefire-reports/custom-report.html");
            reportFile.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(reportFile);
            writer.write(html.toString());
            writer.close();
            System.out.println("Custom report generated: " + reportFile.getAbsolutePath());

            // Write checkpoint results file for GitHub Actions job summary
            File checkpointFile = new File("target/surefire-reports/checkpoint-results.txt");
            FileWriter checkpointWriter = new FileWriter(checkpointFile);
            for (String[] entry : reportEntries) {
                String testName = entry[0];
                String stepName = entry[1];
                String status = entry[3];
                checkpointWriter.write(testName + "|" + stepName + "|" + status + "\n");
            }
            checkpointWriter.close();
            System.out.println("Checkpoint results written: " + checkpointFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to generate custom report: " + e.getMessage());
        }
    }

    @BeforeMethod
    public void setUp(ITestResult result) throws MalformedURLException {
        currentTestName = result.getMethod().getMethodName();
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
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test(description = "Verify app launch")
    public void testVerifyAppLaunch() throws InterruptedException {
        // Wait for either "Login with Google" or passcode screen to appear
        Thread.sleep(5000);

        // If "Login with Google" exists, tap it; otherwise enter passcode
        List<WebElement> loginWithGoogle = driver.findElements(AppiumBy.accessibilityId("Login with Google"));
        if (!loginWithGoogle.isEmpty()) {
            loginWithGoogle.get(0).click();

            // Tap on account that contains @cronoslabs.org
            driver.findElement(AppiumBy.xpath("//*[contains(@content-desc, '@cronoslabs.org')]")).click();

            // Tap Continue
            driver.findElement(AppiumBy.xpath("//android.widget.Button[@text=\"Continue\"]")).click();
            Thread.sleep(5000);
        } else {
            // Enter passcode only if passcode screen is visible
            List<WebElement> passcodeDigits = driver.findElements(AppiumBy.xpath("//android.widget.TextView[@text='2']"));
            if (!passcodeDigits.isEmpty()) {
                String passcode = "258036";
                for (char digit : passcode.toCharArray()) {
                    driver.findElement(
                            AppiumBy.xpath("//android.widget.TextView[@text='" + digit + "']")
                    ).click();
                }
                Thread.sleep(3000);
            }
        }
    }

    @Test(description = "Verify Buy order flow")
    public void testBuyOrder() throws InterruptedException {
        // Wait for app to load
        Thread.sleep(5000);

        // Handle login/passcode
        List<WebElement> loginWithGoogle = driver.findElements(AppiumBy.accessibilityId("Login with Google"));
        if (!loginWithGoogle.isEmpty()) {
            loginWithGoogle.get(0).click();
            driver.findElement(AppiumBy.xpath("//*[contains(@content-desc, '@cronoslabs.org')]")).click();
            driver.findElement(AppiumBy.xpath("//android.widget.Button[@text=\"Continue\"]")).click();
            Thread.sleep(5000);
        } else {
            List<WebElement> passcodeDigits = driver.findElements(AppiumBy.xpath("//android.widget.TextView[@text='2']"));
            if (!passcodeDigits.isEmpty()) {
                String passcode = "258036";
                for (char digit : passcode.toCharArray()) {
                    driver.findElement(
                            AppiumBy.xpath("//android.widget.TextView[@text='" + digit + "']")
                    ).click();
                }
                Thread.sleep(3000);
            }
        }

        // Navigate to Market
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("Market"))).click();

        // Tap on Search
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//android.widget.Button[@content-desc=\"Open search\"]/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup[1]"))).click();

        // Input "BTC" in search field
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//android.view.ViewGroup[@content-desc=\"search icon\"]/android.view.ViewGroup[1]"))).click();
        WebElement searchField = wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.className("android.widget.EditText")));
        searchField.sendKeys("BTC");

        // Tap on BTC
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("CDCBTC Bitcoin"))).click();

        // Tap on Buy
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("Buy"))).click();

        // Tap amount input and enter 0.01
        WebElement amountInput = wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//android.widget.EditText[@resource-id=\"spot-trade-sheet.amount-input\"]")));
        amountInput.click();
        amountInput.sendKeys("0.01");

        // Tap review order
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("Review order"))).click();
        Thread.sleep(3000);

        // Get amount in BTC from review screen
        String amountInBTC = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath("//android.widget.TextView[@resource-id=\"spot-trade-sheet.receive-amount-text\"]"))).getText();
        System.out.println("Amount in BTC from review: " + amountInBTC);

        // Assert pay amount matches input amount
        String payAmount = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath("//android.widget.TextView[@resource-id=\"spot-trade-sheet.pay-amount-text\"]"))).getText();
        takeScreenshot("input_and_confirmation_page_validation");
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
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("Confirm order"))).click();
        Thread.sleep(5000);

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
        takeScreenshot("buy_order_notification_received");
        System.out.println("PASSED: Notification 'Order successful' received.");

        // Get notification body text and extract BTC amount
        String notifBody = driver.findElement(AppiumBy.xpath("//android.widget.TextView[contains(@text, 'order for')]")).getText();
        System.out.println("Notification body: " + notifBody);

        // Extract amount from notification (e.g. "Your buy order for <0.000001 CDCBTC...")
        String confirmedAmountInBTC = notifBody.split("for ")[1].split(" CDCBTC")[0];
        System.out.println("Amount in BTC from notification: " + confirmedAmountInBTC);

        // Assert amountInBTC matches confirmedAmountInBTC
        takeScreenshot("buy_btc_amount_matches_notification");
        Assert.assertEquals(confirmedAmountInBTC, amountInBTC,
                "BTC amount in review (" + amountInBTC + ") should match notification (" + confirmedAmountInBTC + ")");
        System.out.println("PASSED: Amount in BTC matches between review and notification.");

        // Tap on the notification
        Thread.sleep(3000);
        driver.findElement(AppiumBy.xpath("(//android.widget.FrameLayout[@resource-id=\"android:id/status_bar_latest_event_content\"])[1]")).click();

        // Assert "Success" text is displayed
        String successText = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath("//android.widget.TextView[@text=\"Success\"]"))).getText();
        takeScreenshot("buy_order_success_history_screen");
        Assert.assertEquals(successText, "Success", "Success text should be displayed");
        System.out.println("PASSED: Success text is displayed.");

        // Assert token amount on success screen matches confirmedAmount
        String successAmountText = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath("//android.widget.TextView[contains(@text, 'CDCBTC') or @text='" + confirmedAmountInBTC + "']"))).getText();
        takeScreenshot("buy_token_amount_matches_token_buy_history");
        Assert.assertTrue(successAmountText.contains(confirmedAmountInBTC) || confirmedAmountInBTC.contains(successAmountText),
                "Amount on success screen (" + successAmountText + ") should match confirmed amount (" + confirmedAmountInBTC + ")");
        System.out.println("PASSED: Amount on success screen matches confirmed BTC amount.");

        // Assert total paid matches the input value
        String totalPaid = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath("//android.widget.TextView[@text=\"$0.01\"]"))).getText();
        takeScreenshot("buy_total_paid_matches_input");
        Assert.assertEquals(totalPaid, "$0.01",
                "Total paid (" + totalPaid + ") should match input value ($0.01)");
        System.out.println("PASSED: Total paid matches input value.");

        // Dismiss the success screen by pressing back
        driver.pressKey(new io.appium.java_client.android.nativekey.KeyEvent(
                io.appium.java_client.android.nativekey.AndroidKey.BACK));
        Thread.sleep(2000);
    }


    @Test(description = "Verify Sell order flow")
    public void testSellOrder() throws InterruptedException {
        // Wait for app to load
        Thread.sleep(10000);

        // If "Login with Google" exists, tap it; otherwise enter passcode
        List<WebElement> loginWithGoogle = driver.findElements(AppiumBy.accessibilityId("Login with Google"));
        if (!loginWithGoogle.isEmpty()) {
            loginWithGoogle.get(0).click();
            driver.findElement(AppiumBy.xpath("//*[contains(@content-desc, '@cronoslabs.org')]")).click();
            driver.findElement(AppiumBy.xpath("//android.widget.Button[@text=\"Continue\"]")).click();
            Thread.sleep(5000);
        } else {
            // Enter passcode only if passcode screen is visible
            List<WebElement> passcodeDigits = driver.findElements(AppiumBy.xpath("//android.widget.TextView[@text='2']"));
            if (!passcodeDigits.isEmpty()) {
                String passcode = "258036";
                for (char digit : passcode.toCharArray()) {
                    driver.findElement(
                            AppiumBy.xpath("//android.widget.TextView[@text='" + digit + "']")
                    ).click();
                }
                Thread.sleep(3000);
            }
        }

        // Tap on Manage
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//android.widget.TextView[@text=\"Manage\"]"))).click();

        // Tap on Sell
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("Sell"))).click();

        // Tap Set amount to max
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("Set amount to max"))).click();

        // Get input amount value
        String inputAmount = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath("//android.widget.EditText[@resource-id=\"spot-trade-sheet.amount-input\"]"))).getText();
        System.out.println("Input amount: " + inputAmount);

        // Tap Review order
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("Review order"))).click();

       

        // Tap Confirm order
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//android.widget.Button[@content-desc=\"Confirm order\"]"))).click();

        // Wait for confirmation overlay to be dismissed
        wait.until(ExpectedConditions.invisibilityOfElementLocated(AppiumBy.xpath("//androidx.compose.ui.viewinterop.ViewFactoryHolder/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.view.ViewGroup/android.view.ViewGroup[2]/android.view.ViewGroup[2]")));

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
        takeScreenshot("sell_notification_received");
        System.out.println("PASSED: Notification 'Order successful' received.");

        // Get notification body text and verify pay amount
        String notifBody = driver.findElement(AppiumBy.xpath("//android.widget.TextView[contains(@text, 'order for')]")).getText();
        System.out.println("Notification body: " + notifBody);

        // Extract amount from notification
        String notifAmount = notifBody.split("for ")[1].split(" CDCBTC")[0];
        System.out.println("Amount from notification: " + notifAmount);

        // Assert notification amount matches input amount (notification may truncate/round or show "<" prefix)
        takeScreenshot("sell_input_amount_matches_notification");
        if (notifAmount.startsWith("<")) {
            double threshold = Double.parseDouble(notifAmount.substring(1));
            double inputAmountValue = Double.parseDouble(inputAmount);
            Assert.assertTrue(inputAmountValue <= threshold,
                    "Input amount (" + inputAmount + ") should be <= threshold (" + notifAmount + ")");
        } else {
            double notifAmountValue = Double.parseDouble(notifAmount);
            double inputAmountValue = Double.parseDouble(inputAmount);
            Assert.assertTrue(inputAmountValue >= notifAmountValue,
                    "Input amount (" + inputAmount + ") should be >= notification amount (" + notifAmount + ")");
        }
        System.out.println("PASSED: Notification amount matches input amount.");

        // Tap on the notification
        Thread.sleep(3000);
        driver.findElement(AppiumBy.xpath("(//android.widget.FrameLayout[@resource-id=\"android:id/status_bar_latest_event_content\"])[1]")).click();

        // Assert "Success" text is displayed
        String successText = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath("//android.widget.TextView[@text=\"Success\"]"))).getText();
        takeScreenshot("sell_order_success_history_screen");
        Assert.assertEquals(successText, "Success", "Success text should be displayed");
        System.out.println("PASSED: Success text is displayed.");

        // Assert token amount on success screen matches notification amount
        String successAmountText = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath("//android.widget.TextView[contains(@text, 'CDCBTC') or @text='" + notifAmount + "']"))).getText();
        takeScreenshot("sell_notification_token_amount_matches_token_sell_history");
        Assert.assertTrue(successAmountText.contains(notifAmount) || notifAmount.contains(successAmountText),
                "Amount on success screen (" + successAmountText + ") should match notification amount (" + notifAmount + ")");
        System.out.println("PASSED: Amount on success screen matches notification amount.");

        // Take final screenshot of success screen
        takeScreenshot("sell_successully_completed");
        System.out.println("PASSED: Sell order completed successfully.");

         // Dismiss the success screen by pressing back
        driver.pressKey(new io.appium.java_client.android.nativekey.KeyEvent(
                io.appium.java_client.android.nativekey.AndroidKey.BACK));
        Thread.sleep(2000);

    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (driver != null) {
            if (result.getStatus() == ITestResult.FAILURE) {
                Reporter.setCurrentTestResult(result);
                takeScreenshot("FAILED_" + result.getMethod().getMethodName(), "FAILED");
            }
            driver.quit();
        }
    }
}
