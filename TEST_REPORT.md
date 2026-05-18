# Automation Test Report

**Project:** Appium Android Test - Cronos UAT Wallet
**Date:** 2026-05-18
**Tester:** Ronalyn Navasca
**Platform:** Android 16 (API 36)
**Device:** Google Pixel 10 (UDID: 56191FDCR00181)
**App Under Test:** `com.defi.st.wallet` (Cronos UAT Wallet)
**Automation Framework:** Appium + TestNG + Maven
**Driver:** UiAutomator2

---

## Executive Summary

| Metric | Value |
|--------|-------|
| Total Test Classes | 2 |
| Total Test Cases | 5 |
| Passed | 0 |
| Failed | 5 |
| Skipped | 0 |
| Pass Rate | 0% |
| Total Execution Time | ~2 min 10 sec |

---

## Test Classes Overview

### 1. `CronosUATTest.java`

| # | Test Method | Description | Status | Failure Reason |
|---|------------|-------------|--------|----------------|
| 1 | `testVerifyAppLaunch` | Verify app launch, login, navigate to Market, search BTC, place Buy order, verify notification & success screen | FAILED | `accessibilityId("Market")` not found after login |
| 2 | `testSellOrder` | Verify Sell order flow — login, tap Manage, sell max BTC, verify notification & success screen | FAILED | `xpath: //android.widget.TextView[@text="Manage"]` not found |

### 2. `spotBuyAndSellFlow.java`

| # | Test Method | Description | Status | Failure Reason |
|---|------------|-------------|--------|----------------|
| 1 | `testVerifyAppLaunch` | Verify app launch, login/passcode, navigate to Market, search BTC | FAILED | `accessibilityId("Market")` not found (line 79) |
| 2 | `testBuyOrder` | Full Buy order flow — login, navigate to BTC, place $0.01 buy order, verify notification & success | FAILED | Timeout waiting for "Order successful" notification (line 171) |
| 3 | `testSellOrder` | Full Sell order flow — login, tap Manage, sell max amount, verify notification & success | FAILED | Timeout waiting for `TextView[@text="Manage"]` (line 252) |

---

## Test Case Details

### TC-001: Verify App Launch & Navigation to BTC

**Test Method:** `testVerifyAppLaunch`
**Priority:** High
**Preconditions:** App installed, user logged in or passcode set

**Steps:**
1. Launch app
2. Handle login (Google login or passcode entry)
3. Tap "Market" tab
4. Tap search icon
5. Enter "BTC" in search field
6. Tap on "CDCBTC Bitcoin"

**Expected Result:** User navigates to BTC token detail page
**Actual Result:** FAILED at step 3 — "Market" element not found
**Error:** `NoSuchElementException` / `TimeoutException` for `accessibilityId("Market")`

---

### TC-002: Verify Buy Order Flow

**Test Method:** `testBuyOrder`
**Priority:** Critical
**Preconditions:** App launched, user on BTC token page

**Steps:**
1. Launch app & handle login
2. Navigate to Market > Search BTC > Tap CDCBTC
3. Tap "Buy"
4. Enter amount: $0.01
5. Tap "Review order"
6. Verify receive amount and pay amount displayed
7. Tap "Confirm order"
8. Minimize app and wait for "Order successful" notification
9. Verify notification body contains BTC amount
10. Tap notification to open success screen
11. Verify "Success" text, BTC amount, and total paid ($0.01)

**Expected Result:** Buy order completes successfully with matching amounts
**Actual Result:** FAILED at step 8 — Notification did not appear within 30 seconds
**Error:** `TimeoutException` waiting for notification poll

---

### TC-003: Verify Sell Order Flow

**Test Method:** `testSellOrder`
**Priority:** Critical
**Preconditions:** App launched, user has BTC balance

**Steps:**
1. Launch app & handle login
2. Tap "Manage"
3. Tap "Sell"
4. Tap "Set amount to max"
5. Tap "Review order"
6. Verify pay amount matches input
7. Tap "Confirm order"
8. Minimize app and wait for "Order successful" notification
9. Verify notification body contains correct amount
10. Tap notification to open success screen
11. Verify "Success" text and amounts

**Expected Result:** Sell order completes successfully with matching amounts
**Actual Result:** FAILED at step 2 — "Manage" element not found
**Error:** `TimeoutException` for `xpath: //android.widget.TextView[@text="Manage"]`

---

## Failure Analysis

### Root Cause: Stale UI Locators

All failures stem from the same root cause — **the app's UI has been updated** and the element locators in the automation scripts no longer match the current app interface.

| Locator | Type | Status |
|---------|------|--------|
| `accessibilityId("Market")` | Bottom nav tab | NOT FOUND |
| `accessibilityId("Buy")` | Action button | NOT FOUND (in first run) |
| `xpath: //android.widget.TextView[@text="Manage"]` | Home screen button | NOT FOUND |
| `accessibilityId("Login with Google")` | Login button | EXISTS |
| `accessibilityId("CDCBTC Bitcoin")` | Search result | UNKNOWN |

### Contributing Factors

1. **App UI redesign** — Navigation elements (Market tab, Manage button) have likely been renamed or restructured
2. **No explicit waits in `testVerifyAppLaunch`** — Uses `driver.findElement` directly without `WebDriverWait`, causing immediate failure
3. **Notification dependency** — `testBuyOrder` depends on push notification which may be disabled or the order may not have executed
4. **Test independence** — Each test creates a new driver session but some tests (like Buy) depend on navigating through multiple screens

---

## Environment Details

| Component | Version |
|-----------|---------|
| Java | 25.0.2 |
| Selenium | 4.17.0 |
| Appium Java Client | (via Maven) |
| TestNG | (via Maven) |
| UiAutomator2 | Latest |
| Maven Surefire | 3.2.3 |
| OS (Host) | macOS Darwin 25.2.0 (aarch64) |
| Device Screen | 1080x2424 (420dpi) |

---

## Recommendations

1. **Inspect current UI hierarchy** — Use Appium Inspector or `adb shell uiautomator dump` to capture the current element tree and update locators
2. **Update navigation locators** — Priority fix for "Market" tab and "Manage" button identifiers
3. **Add robust waits** — Replace remaining `driver.findElement` calls with `WebDriverWait` + `ExpectedConditions`
4. **Verify notification permissions** — Ensure the app has notification permission enabled on the test device
5. **Add screenshot-on-failure** — Implement a TestNG listener to capture screenshots when tests fail for easier debugging
6. **Consider Page Object Model** — Centralize locators to make maintenance easier when the UI changes

---

## Attachments

- Surefire Reports: `target/surefire-reports/`
- Test Source: `src/test/java/tests/spotBuyAndSellFlow.java`
- Test Source: `src/test/java/tests/CronosUATTest.java`
