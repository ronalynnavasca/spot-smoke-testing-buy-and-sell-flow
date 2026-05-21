# WebDriverAgent (WDA) Rebuild - Issue Summary & Debugging Guide

## Issue Summary

**Device:** Animals AIR (iOS 26.3.1)
**UDID:** 00008150-0004344A3CF8401C
**WDA Source:** Appium (appium-xcuitest-driver)
**Signing Identity:** Apple Development: ronalyn.navasca@yahoo.com.ph (7HG76WAR7C)
**Team ID:** QW89J892K5
**Bundle ID:** com.ronalyn.WebDriverAgentRunner.xctrunner

### Problems Encountered

1. **Device stuck in "Preparing" state** — Xcode could not communicate with the device initially because it was still preparing developer resources.
2. **Developer App Certificate not trusted** — WDA was built and installed successfully, but the device refused to launch it because the developer certificate has not been explicitly trusted by the user on the device.

### Final Error

```
The application could not be launched because the Developer App Certificate is not trusted.
Verify that the Developer App certificate for your account is trusted on your device.
Open Settings on the device and navigate to General -> VPN & Device Management,
then select your Developer App certificate to trust it.
```

---

## Debugging Steps

### Step 1: Verify Device Connection

```bash
xcrun xctrace list devices
```

- Confirm your physical device appears in the list (not just simulators).
- Note the UDID for later use.

### Step 2: Validate Device Pairing

```bash
idevicepair -u <UDID> validate
```

- Should return `SUCCESS: Validated pairing with device`.
- If it fails, unplug/replug the device and tap "Trust" on the device when prompted.

### Step 3: Check Developer Mode on Device

On the iOS device:
- Go to **Settings > Privacy & Security > Developer Mode**
- Ensure Developer Mode is **ON**
- If toggled on for the first time, the device will restart

### Step 4: Wait for Device Preparation

If Xcode shows "Preparing device":
- Open **Xcode > Window > Devices and Simulators**
- Wait for the progress bar to complete
- If stuck for more than 5 minutes:
  - Unplug and replug the USB cable
  - Restart the device
  - Restart Xcode

### Step 5: Build WDA

```bash
cd ~/.appium/node_modules/appium-xcuitest-driver/node_modules/appium-webdriveragent

xcodebuild build-for-testing \
  -project WebDriverAgent.xcodeproj \
  -scheme WebDriverAgentRunner \
  -destination "generic/platform=iOS" \
  -allowProvisioningUpdates \
  DEVELOPMENT_TEAM=QW89J892K5
```

- Look for `** TEST BUILD SUCCEEDED **` at the end.
- If signing fails, verify your team ID and provisioning profile.

### Step 6: Install & Launch WDA on Device

```bash
xcodebuild test-without-building \
  -project WebDriverAgent.xcodeproj \
  -scheme WebDriverAgentRunner \
  -destination "id=<UDID>"
```

- If successful, WDA will start running on the device.

### Step 7: Trust Developer Certificate (if needed)

If you get the error "Developer App Certificate is not trusted":

1. On the device, go to **Settings > General > VPN & Device Management**
2. Find the developer profile (e.g., `Apple Development: ronalyn.navasca@yahoo.com.ph`)
3. Tap the profile
4. Tap **Trust** and confirm

### Step 8: Re-run WDA After Trusting

After trusting the certificate, run Step 6 again to launch WDA.

### Step 9: Verify WDA is Running

Once launched, WDA should be accessible at:
```
http://<device-ip>:8100/status
```

Or verify through Appium logs when starting a session.

---

## Common Issues & Fixes

| Issue | Fix |
|-------|-----|
| Device not found | Unplug/replug USB, trust computer on device |
| Device busy (Preparing) | Wait, restart device, or restart Xcode |
| Signing error | Check DEVELOPMENT_TEAM, provisioning profile |
| Certificate not trusted | Trust in Settings > General > VPN & Device Management |
| WDA crashes on launch | Clean build folder, rebuild WDA |
| Provisioning profile expired | Renew in Xcode or Apple Developer portal |

---

## Useful Commands

```bash
# List connected devices
xcrun xctrace list devices

# Check device pairing
idevicepair -u <UDID> validate

# Clean WDA build
xcodebuild clean -project WebDriverAgent.xcodeproj -scheme WebDriverAgentRunner

# Full rebuild and test
xcodebuild build-for-testing -project WebDriverAgent.xcodeproj \
  -scheme WebDriverAgentRunner \
  -destination "id=<UDID>" \
  -allowProvisioningUpdates
```

---

*Generated: 2026-05-21*
