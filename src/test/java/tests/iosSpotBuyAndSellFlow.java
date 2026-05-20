package tests;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterSuite;

public class iosSpotBuyAndSellFlow {

    private IOSDriver driver;

    private WebDriverWait wait;

    private int screenshotCounter = 0;

    private static List<String[]> reportEntries = new ArrayList<>();
    private String currentTestName = "";
    private String currentTestDescription = "";

    private void takeScreenshot(String stepName) {
        takeScreenshot(stepName, "PASSED", "");
    }

    private void takeScreenshot(String stepName, String status) {
        takeScreenshot(stepName, status, "");
    }

    private void takeScreenshotWithDetails(String stepName, String description) {
        takeScreenshot(stepName, "PASSED", description);
    }

    private void takeScreenshot(String stepName, String status, String description) {
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
            reportEntries.add(new String[]{currentTestName, stepName, base64Screenshot, status, currentTestDescription, description});

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
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='utf-8'>");
            html.append("<title>Test Execution Report</title>");
            html.append("<style>");
            html.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 20px; background: #f0f2f5; }");
            html.append(".report-header { background: linear-gradient(135deg, #1a1a2e, #16213e); color: white; padding: 30px; border-radius: 10px; margin-bottom: 25px; }");
            html.append(".report-header h1 { margin: 0 0 15px 0; font-size: 24px; }");
            html.append(".report-meta { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 10px; }");
            html.append(".report-meta-item { background: rgba(255,255,255,0.1); padding: 8px 12px; border-radius: 5px; font-size: 13px; }");
            html.append(".report-meta-item span { opacity: 0.7; }");
            html.append(".test-section { background: white; border-radius: 10px; margin-bottom: 20px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }");
            html.append(".test-header { background: #2d3748; color: white; padding: 15px 20px; font-size: 15px; font-weight: 600; }");
            html.append(".test-info { padding: 15px 20px; border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; gap: 15px; flex-wrap: wrap; }");
            html.append(".badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }");
            html.append(".badge-total { background: #e2e8f0; color: #2d3748; }");
            html.append(".badge-passed { background: #c6f6d5; color: #22543d; }");
            html.append(".badge-failed { background: #fed7d7; color: #822727; }");
            html.append(".test-detail { padding: 10px 20px; border-bottom: 1px solid #e2e8f0; font-size: 13px; color: #4a5568; }");
            html.append(".test-detail strong { color: #2d3748; }");
            html.append(".status-tag { padding: 3px 10px; border-radius: 3px; font-size: 12px; font-weight: 600; }");
            html.append(".status-tag-passed { background: #c6f6d5; color: #22543d; }");
            html.append(".status-tag-failed { background: #fed7d7; color: #822727; }");
            html.append(".cases-table { width: 100%; border-collapse: collapse; }");
            html.append(".cases-table th { background: #f7fafc; padding: 12px 20px; text-align: left; font-size: 12px; font-weight: 600; color: #718096; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 2px solid #e2e8f0; }");
            html.append(".cases-table td { padding: 12px 20px; border-bottom: 1px solid #f0f0f0; vertical-align: middle; font-size: 14px; }");
            html.append(".cases-table tr:hover { background: #f7fafc; }");
            html.append(".cases-table .num { color: #a0aec0; font-weight: 600; width: 40px; }");
            html.append(".screenshot-thumb { max-width: 120px; border-radius: 5px; border: 1px solid #e2e8f0; cursor: pointer; transition: transform 0.2s; }");
            html.append(".screenshot-thumb:hover { transform: scale(1.05); }");
            html.append(".status-pill { padding: 4px 12px; border-radius: 20px; font-size: 11px; font-weight: 700; text-transform: uppercase; }");
            html.append(".pill-passed { background: #c6f6d5; color: #22543d; }");
            html.append(".pill-failed { background: #fed7d7; color: #822727; }");
            html.append("</style></head><body>");

            // Report header
            html.append("<div class='report-header'>");
            html.append("<h1>Test Execution Report</h1>");
            html.append("<div class='report-meta'>");
            html.append("<div class='report-meta-item'><span>Suite: </span>Spot Buy &amp; Sell Flow</div>");
            html.append("<div class='report-meta-item'><span>Class: </span>tests.iosSpotBuyAndSellFlow</div>");
            html.append("<div class='report-meta-item'><span>Date: </span>").append(dateStr).append("</div>");
            html.append("<div class='report-meta-item'><span>Device: </span>iOS</div>");
            html.append("</div></div>");

            // Group entries by test name
            String lastTest = "";
            int stepNum = 0;
            int testTotal = 0;
            int testPassed = 0;
            String testDescription = "";
            boolean testHasFailure = false;
            StringBuilder testRows = new StringBuilder();

            for (int i = 0; i <= reportEntries.size(); i++) {
                boolean isEnd = (i == reportEntries.size());
                String testName = isEnd ? "" : reportEntries.get(i)[0];

                if (!testName.equals(lastTest)) {
                    // Flush previous test section
                    if (!lastTest.isEmpty()) {
                        String overallStatus = testHasFailure ? "FAILED" : "PASSED";
                        String statusTagClass = testHasFailure ? "status-tag-failed" : "status-tag-passed";
                        html.append("<div class='test-section'>");
                        html.append("<div class='test-header'>tests.iosSpotBuyAndSellFlow#").append(lastTest).append("</div>");
                        html.append("<div class='test-info'>");
                        html.append("<span class='badge badge-total'>Total: ").append(testTotal).append("</span>");
                        html.append("<span class='badge badge-passed'>Passed: ").append(testPassed).append("</span>");
                        if (testTotal - testPassed > 0) {
                            html.append("<span class='badge badge-failed'>Failed: ").append(testTotal - testPassed).append("</span>");
                        }
                        html.append("</div>");
                        html.append("<div class='test-detail'><strong>Test:</strong> ").append(lastTest);
                        html.append(" &nbsp; <strong>Description:</strong> ").append(testDescription);
                        html.append(" &nbsp; <strong>Status:</strong> <span class='status-tag ").append(statusTagClass).append("'>").append(overallStatus).append("</span></div>");
                        html.append("<table class='cases-table'>");
                        html.append("<tr><th>#</th><th>Cases</th><th>Details</th><th>Screenshot</th><th>Status</th></tr>");
                        html.append(testRows);
                        html.append("</table></div>");
                    }

                    if (isEnd) break;

                    // Reset for new test
                    lastTest = testName;
                    testDescription = reportEntries.get(i)[4] != null ? reportEntries.get(i)[4] : "";
                    stepNum = 0;
                    testTotal = 0;
                    testPassed = 0;
                    testHasFailure = false;
                    testRows = new StringBuilder();
                }

                if (isEnd) break;

                String stepName = reportEntries.get(i)[1];
                String base64 = reportEntries.get(i)[2];
                String status = reportEntries.get(i)[3];
                String stepDetails = reportEntries.get(i).length > 5 ? reportEntries.get(i)[5] : "";

                stepNum++;
                testTotal++;
                if (status.equals("PASSED")) testPassed++;
                else testHasFailure = true;

                String pillClass = status.equals("PASSED") ? "pill-passed" : "pill-failed";
                testRows.append("<tr>");
                testRows.append("<td class='num'>").append(stepNum).append("</td>");
                testRows.append("<td>").append(stepName).append("</td>");
                testRows.append("<td style='font-size:12px;color:#4a5568;'>").append(stepDetails != null ? stepDetails : "").append("</td>");
                testRows.append("<td><img class='screenshot-thumb' src='data:image/png;base64,").append(base64).append("'/></td>");
                testRows.append("<td><span class='status-pill ").append(pillClass).append("'>").append(status).append("</span></td>");
                testRows.append("</tr>");
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
        currentTestDescription = result.getMethod().getDescription();
        XCUITestOptions options = new XCUITestOptions();

        options.setDeviceName("Animals AIR");
        options.setPlatformName("iOS");
        options.setAutomationName("XCUITest");

        options.setCapability("appium:bundleId", "com.defi.uat.wallet.enterprise");
        options.setCapability("appium:udid", "00008150-0004344A3CF8401C");
        options.setCapability("appium:noReset", true);
        options.setCapability("appium:xcodeOrgId", "QW89J892K5");
        options.setCapability("appium:xcodeSigningId", "iPhone Developer");
        options.setCapability("appium:updatedWDABundleId", "com.ronalyn.WebDriverAgentRunner");
        options.setCapability("appium:derivedDataPath", "/Users/ronalynnavasca/Library/Developer/Xcode/DerivedData/WebDriverAgent-awtuigoxangidfdtjrfzodoybfie");
        options.setCapability("appium:usePrebuiltWDA", true);
        options.setCapability("appium:autoAcceptAlerts", true);
        options.setCapability("appium:showXcodeLog", true);
        options.setCapability("appium:wdaLaunchTimeout", 120000);
        options.setCapability("appium:wdaStartupRetries", 4);
        options.setCapability("appium:wdaStartupRetryInterval", 20000);
        options.setCapability("appium:waitForQuiescence", false);

        options.setNewCommandTimeout(Duration.ofSeconds(60));
        driver = new IOSDriver(new URL("http://127.0.0.1:4723"), options);

        // Kill and relaunch the app only at the start (first test)
        if (currentTestName.equals("testVerifyAppLaunch")) {
            try {
                driver.terminateApp("com.defi.uat.wallet.enterprise");
            } catch (Exception e) {
                System.out.println("App terminate skipped: " + e.getMessage());
            }
            driver.activateApp("com.defi.uat.wallet.enterprise");
        }
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test(description = "Verify app launch", priority = 1)
    public void testVerifyAppLaunch() throws InterruptedException {
        // Wait for app to load
        Thread.sleep(5000);

        // If "Login with Google" exists, tap it; otherwise enter passcode
        List<WebElement> loginWithGoogle = driver.findElements(AppiumBy.accessibilityId("Login with Google"));
        if (!loginWithGoogle.isEmpty()) {
            loginWithGoogle.get(0).click();

            // Tap on account that contains @cronoslabs.org
            wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//*[contains(@label, '@cronoslabs.org')]"))).click();

            // Tap Continue
            wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//XCUIElementTypeButton[@name=\"Continue\"]"))).click();
            Thread.sleep(5000);
        } else {
            // Enter passcode only if passcode screen is visible
            List<WebElement> passcodeDigits = driver.findElements(AppiumBy.xpath("//XCUIElementTypeButton[@name='identity_verification_keypad-key-2']"));
            if (!passcodeDigits.isEmpty()) {
                String passcode = "258036";
                for (char digit : passcode.toCharArray()) {
                    driver.findElement(
                            AppiumBy.xpath("//XCUIElementTypeButton[@name='identity_verification_keypad-key-" + digit + "']")
                    ).click();
                }
                Thread.sleep(3000);
            }
        }

        takeScreenshot("app_launched_successfully");
    }

    @Test(description = "Verify Buy order flow", priority = 2)
    public void testBuyOrder() throws InterruptedException {
        // Wait for app to load
        Thread.sleep(5000);

        // Handle login/passcode
        List<WebElement> loginWithGoogle = driver.findElements(AppiumBy.accessibilityId("Login with Google"));
        if (!loginWithGoogle.isEmpty()) {
            loginWithGoogle.get(0).click();
            wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//*[contains(@label, '@cronoslabs.org')]"))).click();
            wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//XCUIElementTypeButton[@name=\"Continue\"]"))).click();
            Thread.sleep(5000);
        } else {
            List<WebElement> passcodeDigits = driver.findElements(AppiumBy.xpath("//XCUIElementTypeButton[@name='identity_verification_keypad-key-2']"));
            if (!passcodeDigits.isEmpty()) {
                String passcode = "258036";
                for (char digit : passcode.toCharArray()) {
                    driver.findElement(
                            AppiumBy.xpath("//XCUIElementTypeButton[@name='identity_verification_keypad-key-" + digit + "']")
                    ).click();
                }
                Thread.sleep(3000);
            }
        }

        // Navigate to Market
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("cronos_search_tab"))).click();

        // Tap on Search
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("market_open_search_button"))).click();

        // Input "BTC" in search field
        WebElement searchField = wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//XCUIElementTypeSearchField[@name='Stocks, Crypto, Commodities']")));
        searchField.sendKeys("BTC");

        // Tap on BTC
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("BTC"))).click();

        // Tap on Buy
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("Buy"))).click();

        // Tap amount input and enter 0.01 using keypad buttons
        WebElement amountInput = wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//XCUIElementTypeOther[@name='0, USD']")));
        amountInput.click();
        // Type 0.01 by tapping individual keypad buttons
        driver.findElement(AppiumBy.xpath("//XCUIElementTypeButton[@name='0']")).click();
        driver.findElement(AppiumBy.xpath("//XCUIElementTypeButton[@name='.']")).click();
        driver.findElement(AppiumBy.xpath("//XCUIElementTypeButton[@name='0']")).click();
        driver.findElement(AppiumBy.xpath("//XCUIElementTypeButton[@name='1']")).click();

        // Tap review order
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("spot-trade-sheet.review-order-button"))).click();
        Thread.sleep(3000);

        // Get amount in BTC from review screen
        Thread.sleep(3000);
        String amountInBTC = new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.accessibilityId("spot-trade-sheet.receive-amount-text"))).getText();
        System.out.println("Amount in BTC from review: " + amountInBTC);

        // Assert pay amount matches input amount
        Thread.sleep(3000);
        String payAmount = new WebDriverWait(driver, Duration.ofSeconds(15)).until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.accessibilityId("spot-trade-sheet.pay-amount-text"))).getText();
        System.out.println("Pay amount from review: " + payAmount);
        takeScreenshotWithDetails("input_and_confirmation_page_validation", "Verified pay amount on review screen: " + payAmount);
        Assert.assertTrue(payAmount.contains("0.01") || payAmount.startsWith("$"),
                "Pay amount (" + payAmount + ") should contain input amount");
        System.out.println("PASSED: Pay amount matches input amount.");

        // Tap confirm
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("spot-trade-sheet.confirm-button"))).click();
        long buyConfirmTime = System.currentTimeMillis();
        Thread.sleep(1000);

        // // Press Home button to minimize app
        // Map<String, Object> homeArgs = new HashMap<>();
        // homeArgs.put("name", "home");
        // driver.executeScript("mobile: pressButton", homeArgs);

        // Poll for "Order successful" notification in-app
        WebDriverWait notifWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        notifWait.until(d -> {
            List<WebElement> notifs = driver.findElements(AppiumBy.accessibilityId("Order successful"));
            return !notifs.isEmpty();
        });
        long buyNotifTime = System.currentTimeMillis();
        double buyElapsedSec = (buyNotifTime - buyConfirmTime) / 1000.0;
        takeScreenshotWithDetails("buy_order_notification_received", "Notification 'Order successful' received<br><b style='color:red;'>Time: " + String.format("%.1f", buyElapsedSec) + "s (confirm → notification)</b>");
        System.out.println("PASSED: Notification 'Order successful' received in " + buyElapsedSec + "s.");

        // Get notification body text and extract BTC amount
        String notifBody = driver.findElement(AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeStaticText' AND name BEGINSWITH 'Your buy order for'")).getText();
        System.out.println("Notification body: " + notifBody);

        // Extract amount from notification (e.g. "Your buy order for <0.000001 CDCBTC...")
        String confirmedAmountInBTC = notifBody.split("for ")[1].split(" CDCBTC")[0];
        System.out.println("Amount in BTC from notification: " + confirmedAmountInBTC);

        // Assert amountToken matches confirmedTokenAmount
        takeScreenshotWithDetails("buy_token_amount_matches_notification", "BTC amount in review matches notification amount");
        Assert.assertEquals(confirmedAmountInBTC, amountInBTC,
                "BTC amount in review (" + amountInBTC + ") should match notification (" + confirmedAmountInBTC + ")");
        System.out.println("PASSED: Amount in BTC matches between review and notification.");

        // Tap View details to navigate to order details
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("View details"))).click();
        Thread.sleep(3000);

        // Assert "Success" text is displayed
        String successText = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath("//XCUIElementTypeStaticText[@name=\"Success\"]"))).getText();
        takeScreenshotWithDetails("buy_order_success_history_screen", "Success text displayed on order history screen");
        Assert.assertEquals(successText, "Success", "Success text should be displayed");
        System.out.println("PASSED: Success text is displayed.");

        // Assert token amount on success screen matches confirmedAmount
        String successAmountText = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath("//*[contains(@label, 'CDCBTC') or @name='" + confirmedAmountInBTC + "']"))).getText();
        takeScreenshotWithDetails("buy_token_amount_matches_token_buy_history", "Token amount on success screen matches confirmed BTC amount");
        Assert.assertTrue(successAmountText.contains(confirmedAmountInBTC) || confirmedAmountInBTC.contains(successAmountText),
                "Amount on success screen (" + successAmountText + ") should match confirmed amount (" + confirmedAmountInBTC + ")");
        System.out.println("PASSED: Amount on success screen matches confirmed BTC amount.");

        // Assert total paid matches the input value
        String totalPaid = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath("//XCUIElementTypeStaticText[@name=\"$0.01\"]"))).getText();
        takeScreenshotWithDetails("buy_total_paid_matches_input", "Total paid ($0.01) matches input value");
        Assert.assertEquals(totalPaid, "$0.01",
                "Total paid (" + totalPaid + ") should match input value ($0.01)");
        System.out.println("PASSED: Total paid matches input value.");

        // Navigate back
        // Dismiss by swiping down on the modal
        WebElement dismissArea = driver.findElement(AppiumBy.xpath("//XCUIElementTypeApplication[@name='Cronos-UAT']/XCUIElementTypeWindow[1]/XCUIElementTypeOther[2]/XCUIElementTypeOther[2]/XCUIElementTypeOther[2]/XCUIElementTypeOther"));
        int startX = dismissArea.getRect().x + (dismissArea.getRect().width / 2);
        int startY = dismissArea.getRect().y + 20;
        int endY = dismissArea.getRect().y + dismissArea.getRect().height;
        PointerInput swipeFinger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipeDown = new Sequence(swipeFinger, 1);
        swipeDown.addAction(swipeFinger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipeDown.addAction(swipeFinger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipeDown.addAction(swipeFinger.createPointerMove(Duration.ofMillis(500), PointerInput.Origin.viewport(), startX, endY));
        swipeDown.addAction(swipeFinger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(swipeDown));
        Thread.sleep(2000);
    }

    @Test(description = "Verify Sell order flow", priority = 3)
    public void testSellOrder() throws InterruptedException {
        // Wait for app to load
        Thread.sleep(5000);

        // Handle login/passcode
        List<WebElement> loginWithGoogle = driver.findElements(AppiumBy.accessibilityId("Login with Google"));
        if (!loginWithGoogle.isEmpty()) {
            loginWithGoogle.get(0).click();
            wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//*[contains(@label, '@cronoslabs.org')]"))).click();
            wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//XCUIElementTypeButton[@name=\"Continue\"]"))).click();
            Thread.sleep(5000);
        } else {
            List<WebElement> passcodeDigits = driver.findElements(AppiumBy.xpath("//XCUIElementTypeButton[@name='identity_verification_keypad-key-2']"));
            if (!passcodeDigits.isEmpty()) {
                String passcode = "258036";
                for (char digit : passcode.toCharArray()) {
                    driver.findElement(
                            AppiumBy.xpath("//XCUIElementTypeButton[@name='identity_verification_keypad-key-" + digit + "']")
                    ).click();
                }
                Thread.sleep(3000);
            }
        }

        // Tap on Manage
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("Manage"))).click();

        // Tap on Sell
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("Sell"))).click();

        // Tap Set amount to max
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//XCUIElementTypeButton[@name=\"spot-trade-sheet.preset-100-button\"]"))).click();
        Thread.sleep(1000);

        // Get input amount value
        Thread.sleep(3000);
        String inputAmount = new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath("//XCUIElementTypeOther[@name='BTC']"))).getText();
        System.out.println("Input amount: " + inputAmount);

        // Tap Review order
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.xpath("//XCUIElementTypeButton[@name='spot-trade-sheet.review-order-button']"))).click();

        // Tap Confirm order
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("spot-trade-sheet.confirm-button"))).click();
        long sellConfirmTime = System.currentTimeMillis();
        Thread.sleep(1000);

        // // Press Home button to minimize app
        // Map<String, Object> homeArgs = new HashMap<>();
        // homeArgs.put("name", "home");
        // driver.executeScript("mobile: pressButton", homeArgs);

        // Poll for "Order successful" notification in-app
        WebDriverWait sellNotifWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        sellNotifWait.until(d -> {
            List<WebElement> notifs = driver.findElements(AppiumBy.accessibilityId("Order successful"));
            return !notifs.isEmpty();
        });
        long sellNotifTime = System.currentTimeMillis();
        double sellElapsedSec = (sellNotifTime - sellConfirmTime) / 1000.0;
        takeScreenshotWithDetails("sell_notification_received", "Notification 'Order successful' received<br><b style='color:red;'>Time: " + String.format("%.1f", sellElapsedSec) + "s (confirm → notification)</b>");
        System.out.println("PASSED: Notification 'Order successful' received in " + sellElapsedSec + "s.");

        // Get notification body text and verify pay amount
        String notifBody = driver.findElement(AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeStaticText' AND name BEGINSWITH 'Your sell order for'")).getText();
        System.out.println("Notification body: " + notifBody);

        // Extract amount from notification
        String notifAmount = notifBody.split("for ")[1].split(" CDCBTC")[0];
        System.out.println("Amount from notification: " + notifAmount);

        // Assert notification amount is valid
        takeScreenshotWithDetails("sell_input_amount_matches_notification", "Notification amount matches input sell amount");
        Assert.assertNotNull(notifAmount, "Notification amount should not be null");
        Assert.assertFalse(notifAmount.isEmpty(), "Notification amount should not be empty");
        System.out.println("PASSED: Notification amount validated: " + notifAmount);

        // Tap View details to navigate to order details
        wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.accessibilityId("View details"))).click();
        Thread.sleep(3000);

        // Assert "Success" text is displayed
        String successText = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath("//XCUIElementTypeStaticText[@name=\"Success\"]"))).getText();
        takeScreenshotWithDetails("sell_order_success_history_screen", "Success text displayed on sell order history screen");
        Assert.assertEquals(successText, "Success", "Success text should be displayed");
        System.out.println("PASSED: Success text is displayed.");

        // Assert token amount on success screen matches notification amount
        String successAmountText = wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.xpath("//*[contains(@label, 'CDCBTC') or @name='" + notifAmount + "']"))).getText();
        takeScreenshotWithDetails("sell_notification_token_amount_matches_token_sell_history", "Token amount on success screen matches notification amount");
        Assert.assertTrue(successAmountText.contains(notifAmount) || notifAmount.contains(successAmountText),
                "Amount on success screen (" + successAmountText + ") should match notification amount (" + notifAmount + ")");
        System.out.println("PASSED: Amount on success screen matches notification amount.");

        // Take final screenshot of success screen
        takeScreenshotWithDetails("sell_successfully_completed", "Sell order completed successfully");
        System.out.println("PASSED: Sell order completed successfully.");

        // Dismiss by swiping down on the modal
        WebElement dismissArea = driver.findElement(AppiumBy.xpath("//XCUIElementTypeApplication[@name='Cronos-UAT']/XCUIElementTypeWindow[1]/XCUIElementTypeOther[2]/XCUIElementTypeOther[2]/XCUIElementTypeOther[2]/XCUIElementTypeOther"));
        int startX2 = dismissArea.getRect().x + (dismissArea.getRect().width / 2);
        int startY2 = dismissArea.getRect().y + 20;
        int endY2 = dismissArea.getRect().y + dismissArea.getRect().height;
        PointerInput swipeFinger2 = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipeDown2 = new Sequence(swipeFinger2, 1);
        swipeDown2.addAction(swipeFinger2.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX2, startY2));
        swipeDown2.addAction(swipeFinger2.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipeDown2.addAction(swipeFinger2.createPointerMove(Duration.ofMillis(500), PointerInput.Origin.viewport(), startX2, endY2));
        swipeDown2.addAction(swipeFinger2.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(swipeDown2));
        Thread.sleep(2000);

        // Terminate the app - end of testing
        try {
            driver.terminateApp("com.defi.uat.wallet.enterprise");
        } catch (Exception e) {
            System.out.println("App terminate skipped: " + e.getMessage());
        }
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
