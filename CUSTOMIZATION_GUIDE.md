# Dialog Customization Guide

## How to Customize the Dialogs

### 1. Internet Connection Dialog

**File:** `DialogComponents.kt` - Lines 25-79

**Customizable Properties:**

```kotlin
// Change dialog width
.width(320.dp)  // Currently 320dp, adjust as needed

// Change border color
color = AccentGreen.copy(alpha = 0.3f)  // Change AccentGreen to any color

// Change button color
containerColor = AccentGreen  // Change to your desired color

// Change title and message text
Text(text = "No Internet Connection")  // Customize title
Text(text = "Internet connection is required...")  // Customize message

// Change button text
Text(text = "Retry")  // Customize button label

// Change dialog background
color = CardBackground  // Change to different background color
```

**Example Customization:**
```kotlin
// Make dialog more prominent
.border(
    width = 2.dp,  // Changed from 1.dp
    color = Color.Red.copy(alpha = 0.5f),  // Red instead of green
    shape = RoundedCornerShape(16.dp)
)
```

---

### 2. Exit Confirmation Dialog

**File:** `DialogComponents.kt` - Lines 82-217

**Customizable Properties:**

```kotlin
// Change dialog width
.width(320.dp)

// Change button row spacing
horizontalArrangement = Arrangement.spacedBy(12.dp)  // Adjust gap between buttons

// Change button colors
// Cancel button
containerColor = CardBackground  // Background color
contentColor = AccentGreen  // Text color

// Exit button
containerColor = AccentGreen  // Background color
contentColor = Color.White  // Text color

// Change title and message
Text(text = "Exit App")  // Customize title
Text(text = "Are you sure you want to exit?")  // Customize message

// Change button text
Text(text = "Cancel")  // Customize cancel label
Text(text = "Exit")  // Customize exit label
```

---

### 3. Network Check Logic

**File:** `NetworkUtils.kt` - Lines 7-22

**Customize Network Detection:**

```kotlin
// Add or remove specific connection types
return when {
    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
    // Add more:
    // capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
    else -> false
}
```

---

### 4. Dialog Behavior in MainActivity

**File:** `MainActivity.kt` - Lines 75-102

**Customize Behavior:**

```kotlin
// Show internet dialog on specific screens only
if (showNoInternetDialog && !isInternetAvailable) {
    // Add condition here to show only on certain routes
    val currentRoute = navController.currentDestination?.route
    if (currentRoute != "splash") {  // Don't show on splash screen
        InternetConnectionDialog(...)
    }
}

// Control which screens show exit dialog
val currentDestination = navController.currentDestination?.route
if (currentDestination == "home" || 
    currentDestination == "splash" || 
    currentDestination == "onboarding") {  // Add more routes if needed
    showExitDialog = true
}
```

---

### 5. Color Customization

**Option A: Use custom colors directly**
```kotlin
// In DialogComponents.kt
.background(
    color = Color(0xFF1A1A1A),  // Custom dark color
    shape = RoundedCornerShape(16.dp)
)
```

**Option B: Add new colors to Color.kt**
```kotlin
// In ui/theme/Color.kt
val DialogBackgroundColor = Color(0xFF1A1A1A)
val DialogBorderColor = Color(0xFF00AA88)

// Then use in dialogs:
.background(color = DialogBackgroundColor, shape = RoundedCornerShape(16.dp))
.border(width = 1.dp, color = DialogBorderColor, shape = RoundedCornerShape(16.dp))
```

---

### 6. Change Dialog Animation

**Add fade-in animation:**
```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

// Wrap the Dialog with animation
AnimatedVisibility(
    visible = showNoInternetDialog,
    enter = fadeIn(),
    exit = fadeOut()
) {
    InternetConnectionDialog(onDismiss = { ... })
}
```

---

### 7. Common Customization Examples

**Example 1: Alert-style Internet Dialog (Red accent)**
```kotlin
// In InternetConnectionDialog, change:
color = Color.Red.copy(alpha = 0.3f)  // Red border
containerColor = Color.Red  // Red button
```

**Example 2: Smaller Dialogs**
```kotlin
// Change width from 320.dp to 280.dp
.width(280.dp)

// Reduce padding
.padding(20.dp)  // From 24.dp
```

**Example 3: Different Dismiss Behavior**
```kotlin
// In InternetConnectionDialog, change DialogProperties:
properties = DialogProperties(
    dismissOnBackPress = true,  // Allow back press to dismiss
    dismissOnClickOutside = true  // Allow outside tap to dismiss
)
```

**Example 4: Add More Network Types**
```kotlin
// In NetworkUtils.kt, add VPN detection:
capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
```

---

### 8. Text Customization

**Change fonts:**
```kotlin
import cricpeak.scorelive.ui.theme.GilroyFontFamily

Text(
    text = "No Internet",
    fontFamily = GilroyFontFamily,  // Your app font
    fontSize = 20.sp,
    fontWeight = FontWeight.Bold
)
```

**Change text styling:**
```kotlin
Text(
    text = "Internet Required",
    fontSize = 22.sp,  // Increase size
    fontWeight = FontWeight.ExtraBold,  // Bolder text
    letterSpacing = 0.5.sp  // Add spacing
)
```

---

### 9. Button Size Customization

**Change button height:**
```kotlin
// Current: 48.dp
Button(
    modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)  // Make taller
)
```

**Change button corner radius:**
```kotlin
// Current: 8.dp
shape = RoundedCornerShape(12.dp)  // More rounded
```

---

### 10. Testing Customizations

After making changes, test by:
1. Rebuilding the app (Ctrl+F9 in Android Studio)
2. Running on emulator/device
3. For internet dialog: Disable WiFi and cellular
4. For exit dialog: Press back from home screen

---

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Dialog not showing | Check `showNoInternetDialog` and `showExitDialog` states |
| Dialog showing wrong colors | Verify color imports from `ui.theme.Color` |
| Button not working | Check `onDismiss` and `onConfirm` callbacks |
| Dialog blocking interaction | Check `dismissOnBackPress` and `dismissOnClickOutside` |
| Text too small | Increase `fontSize` value in `sp` units |
| Button text cut off | Increase button `height` or reduce `fontSize` |


