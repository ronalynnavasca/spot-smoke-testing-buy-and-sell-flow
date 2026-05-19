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
import java.util.List;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
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
            reportEntries.add(new String[]{currentTestName, stepName, base64Screenshot, status, currentTestDescription});

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
                        html.append("<tr><th>#</th><th>Cases</th><th>Screenshot</th><th>Status</th></tr>");
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

                String stepName = reportEntries.get(i)[1];
                String base64 = reportEntries.get(i)[2];
                String status = reportEntries.get(i)[3];

                stepNum++;
                testTotal++;
                if (status.equals("PASSED")) testPassed++;
                else testHasFailure = true;

                String pillClass = status.equals("PASSED") ? "pill-passed" : "pill-failed";
                testRows.append("<tr>");
                testRows.append("<td class='num'>").append(stepNum).append("</td>");
                testRows.append("<td>").append(stepName).append("</td>");
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

        options.setDeviceName("iPhone 16 Pro");
        options.setPlatformName("iOS");
        options.setAutomationName("XCUITest");

        options.setCapability("appium:bundleId", "com.defi.uat.wallet.enterprise");
        options.setCapability("appium:udid", "3B2BBD6D-DE65-4FAC-A2AA-E80D57AB3EEE");
        options.setCapability("appium:noReset", true);

        options.setNewCommandTimeout(Duration.ofSeconds(60));
        driver = new IOSDriver(new URL("http://127.0.0.1:4723"), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test(description = "Verify app launch")
    public void testVerifyAppLaunch() throws InterruptedException {
        // TODO: Implement iOS app launch verification
        Thread.sleep(5000);
    }

    @Test(description = "Verify Buy order flow")
    public void testBuyOrder() throws InterruptedException {
        // TODO: Implement iOS Buy order flow
        Thread.sleep(5000);
    }

    @Test(description = "Verify Sell order flow")
    public void testSellOrder() throws InterruptedException {
        // TODO: Implement iOS Sell order flow
        Thread.sleep(5000);
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
