# Testing Checklist

## ✅ Quick Testing Guide

### Prerequisites
- Android device or emulator running Android 7.0+
- CRICSCORE app built and ready to test

---

## 🌐 Internet Connection Dialog Testing

### Test 1: No Internet on Launch
**Steps:**
1. Disable WiFi and mobile data on your device
2. Launch the CRICSCORE app
3. **Expected Result:** "No Internet Connection" dialog appears
   - ⚠️ emoji/icon displayed
   - Title: "No Internet Connection"
   - Message: "Internet connection is required..."
   - "Retry" button visible
   - Dialog cannot be dismissed by tapping outside or back button

### Test 2: Retry Connection
**Prerequisites:** Test 1 dialog is showing
**Steps:**
1. Tap the "Retry" button while still offline
2. **Expected Result:** Dialog stays on screen (still offline)

### Test 3: Restore Connection
**Prerequisites:** Test 1 dialog is showing
**Steps:**
1. Enable WiFi or mobile data
2. Tap the "Retry" button
3. **Expected Result:** Dialog closes automatically and app proceeds to home screen

### Test 4: Multiple Reconnections
**Steps:**
1. Turn internet off → Dialog appears
2. Turn internet on → Dialog closes
3. Navigate to a screen
4. Turn internet off → Dialog appears again
5. Turn internet on → Dialog closes
6. **Expected Result:** Dialog behavior consistent across multiple cycles

---

## 👋 Exit Confirmation Dialog Testing

### Test 5: Exit from Home Screen
**Prerequisites:** App is on home screen
**Steps:**
1. Press back button
2. **Expected Result:** Exit confirmation dialog appears
   - 👋 emoji/icon displayed
   - Title: "Exit App"
   - Message: "Are you sure you want to exit?"
   - Two buttons: "Cancel" (outlined) and "Exit" (filled green)

### Test 6: Cancel Exit
**Prerequisites:** Exit dialog is showing
**Steps:**
1. Tap "Cancel" button
2. **Expected Result:** Dialog closes, user returns to home screen

### Test 7: Confirm Exit
**Prerequisites:** Exit dialog is showing
**Steps:**
1. Tap "Exit" button
2. **Expected Result:** App closes completely and returns to device home screen

### Test 8: Dismiss by Outside Tap
**Prerequisites:** Exit dialog is showing
**Steps:**
1. Tap outside the dialog area (on the dim background)
2. **Expected Result:** Dialog closes, user returns to home screen

### Test 9: Dismiss by Back Button
**Prerequisites:** Exit dialog is showing
**Steps:**
1. Press device back button
2. **Expected Result:** Dialog closes, user returns to home screen

### Test 10: Exit from Splash Screen
**Prerequisites:** App just launched, splash screen showing
**Steps:**
1. Wait for splash screen to finish (or it may auto-skip)
2. Press back button
3. **Expected Result:** Exit confirmation dialog appears
4. Tap "Exit"
5. **Expected Result:** App closes

### Test 11: Exit from Onboarding Screen
**Prerequisites:** App on onboarding screen (if not completed before)
**Steps:**
1. Press back button
2. **Expected Result:** Exit confirmation dialog appears

### Test 12: Navigation - No Exit Dialog
**Prerequisites:** App is on Match Details screen
**Steps:**
1. Press back button
2. **Expected Result:** Returns to previous screen, NO exit dialog shows

### Test 13: Navigation - No Exit Dialog (Other Screens)
**Steps:**
1. Go to menu screen
2. Press back button
3. **Expected Result:** Returns to home screen, NO exit dialog
4. Go to prediction screen
5. Press back button
6. **Expected Result:** Returns to home screen, NO exit dialog

---

## 🎨 Design Verification Tests

### Test 14: Dialog Color Scheme
**Steps:**
1. Launch the app (trigger internet dialog if offline)
2. Observe colors:
   - **Expected:** Dark background matching app theme
   - **Expected:** Green accent border around dialog
   - **Expected:** White text on dark background
   - **Expected:** Gray text for secondary information
   - **Expected:** Green buttons matching app accent color

### Test 15: Dialog Layout
**Steps:**
1. Trigger either dialog
2. Check layout:
   - **Expected:** Dialog centered on screen
   - **Expected:** Dialog width appropriate for all screen sizes
   - **Expected:** Proper spacing between elements
   - **Expected:** Icon, title, message, buttons well-aligned
   - **Expected:** Text is readable and not cut off

### Test 16: Button Styling
**Steps:**
1. Trigger exit dialog
2. Observe buttons:
   - **Expected:** "Cancel" button has outline style (hollow)
   - **Expected:** "Exit" button has filled style
   - **Expected:** Both buttons are same height
   - **Expected:** Proper spacing between buttons
   - **Expected:** Text centered in buttons

---

## 📱 Responsive Design Tests

### Test 17: Small Phone (5-inch)
**Steps:**
1. Test on 5-inch emulator
2. Trigger both dialogs
3. **Expected:** Dialog fits on screen, readable

### Test 18: Large Phone (6.5-inch)
**Steps:**
1. Test on 6.5-inch emulator
2. Trigger both dialogs
3. **Expected:** Dialog properly centered, not too small

### Test 19: Tablet (10-inch)
**Steps:**
1. Test on tablet emulator
2. Trigger both dialogs
3. **Expected:** Dialog maintains reasonable size (not huge)

---

## 🚀 Edge Case Tests

### Test 20: Rapid Internet Toggle
**Steps:**
1. Turn internet off/on rapidly (5-10 times)
2. **Expected:** Dialog shows/hides appropriately without crashes

### Test 21: Multiple Back Presses
**Prerequisites:** Exit dialog showing
**Steps:**
1. Press back button multiple times
2. **Expected:** Dialog only closes, app doesn't crash

### Test 22: Rotate Screen
**Prerequisites:** Dialog is showing
**Steps:**
1. Rotate device between portrait/landscape
2. **Expected:** Dialog remains visible and properly positioned
3. **Expected:** No content cut off

### Test 23: Low Memory Simulation
**Steps:**
1. Clear device memory (open many apps)
2. Launch CRICSCORE
3. Trigger dialogs
4. **Expected:** Dialogs still appear and function properly

### Test 24: App Backgrounding
**Prerequisites:** Exit dialog showing
**Steps:**
1. Press home button (background app)
2. Return to app
3. **Expected:** Exit dialog still shows

---

## ✨ User Experience Tests

### Test 25: Dialog Responsiveness
**Steps:**
1. Trigger internet dialog
2. Tap "Retry" button quickly multiple times
3. **Expected:** Smooth response, no lag or ANR (Application Not Responding)

### Test 26: Button Touch Targets
**Steps:**
1. Trigger exit dialog
2. Tap buttons at edges
3. **Expected:** Buttons register tap correctly

### Test 27: Text Visibility
**Steps:**
1. Enable battery saver mode (reduces colors)
2. Trigger dialogs
3. **Expected:** Text still clearly visible and readable

### Test 28: Dark Theme Consistency
**Steps:**
1. Verify device is in dark mode
2. Trigger dialogs
3. **Expected:** Dialogs match app's dark theme

---

## 📊 Test Results Template

```
TEST DATE: _______________
DEVICE: ___________________
ANDROID VERSION: __________
APP VERSION: ______________

✓ = PASS
✗ = FAIL

Test 1 (No Internet Launch):     ___
Test 2 (Retry Offline):         ___
Test 3 (Restore Connection):    ___
Test 4 (Multiple Reconnect):    ___
Test 5 (Exit from Home):        ___
Test 6 (Cancel Exit):           ___
Test 7 (Confirm Exit):          ___
Test 8 (Dismiss Outside):       ___
Test 9 (Dismiss Back Button):   ___
Test 10 (Exit Splash):          ___
Test 11 (Exit Onboarding):      ___
Test 12 (Nav - No Exit):        ___
Test 13 (Other Screens):        ___
Test 14 (Color Scheme):         ___
Test 15 (Dialog Layout):        ___
Test 16 (Button Styling):       ___
Test 17 (Small Phone):          ___
Test 18 (Large Phone):          ___
Test 19 (Tablet):               ___
Test 20 (Rapid Toggle):         ___
Test 21 (Multiple Back):        ___
Test 22 (Rotate Screen):        ___
Test 23 (Low Memory):           ___
Test 24 (Background App):       ___
Test 25 (Responsiveness):       ___
Test 26 (Touch Targets):        ___
Test 27 (Text Visibility):      ___
Test 28 (Dark Theme):           ___

NOTES:
_________________________________
_________________________________
_________________________________

OVERALL STATUS: ___________
```

---

## 🐛 Bug Report Template

If you find an issue:

```
BUG: [Brief title]
SEVERITY: Critical / High / Medium / Low
DEVICE: [Device model and Android version]
STEPS TO REPRODUCE:
1. ...
2. ...
3. ...

EXPECTED RESULT:
[What should happen]

ACTUAL RESULT:
[What actually happens]

SCREENSHOTS/VIDEO:
[Attach if possible]

ADDITIONAL INFO:
[Any other relevant details]
```

---

## ✅ Final Checklist

- [ ] All 28 tests passed
- [ ] No crashes or ANR errors
- [ ] Dialogs display correctly on all screen sizes
- [ ] Text is readable and properly formatted
- [ ] Colors match app design
- [ ] Buttons are responsive
- [ ] Internet check works reliably
- [ ] Exit dialog works as expected
- [ ] App ready for production

---

**Happy Testing! 🎉**

