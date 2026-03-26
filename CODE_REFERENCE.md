# Quick Code Reference

## Files Overview

### 1. NetworkUtils.kt
**Full Code:**
```kotlin
package cricpeak.scorelive.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtils {
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
```

**Usage:**
```kotlin
// Check if internet is available
val isOnline = NetworkUtils.isInternetAvailable(context)

// Use in condition
if (!NetworkUtils.isInternetAvailable(context)) {
    showInternetDialog = true
}
```

---

### 2. DialogComponents.kt - InternetConnectionDialog

**Full Code:**
```kotlin
@Composable
fun InternetConnectionDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(320.dp)
                .background(
                    color = CardBackground,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = AccentGreen.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "No Internet Connection",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "Internet connection is required to use our app. Please check your connection and try again.",
                    fontSize = 14.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp),
                    lineHeight = 20.sp
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Retry",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
```

**Usage:**
```kotlin
if (showNoInternetDialog && !isInternetAvailable) {
    InternetConnectionDialog(
        onDismiss = {
            isInternetAvailable = NetworkUtils.isInternetAvailable(context)
            if (isInternetAvailable) {
                showNoInternetDialog = false
            }
        }
    )
}
```

---

### 3. DialogComponents.kt - ExitConfirmationDialog

**Full Code:**
```kotlin
@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .width(320.dp)
                .background(
                    color = CardBackground,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = AccentGreen.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "👋",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Exit App",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "Are you sure you want to exit?",
                    fontSize = 14.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 28.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardBackground,
                            contentColor = AccentGreen
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = AccentGreen
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Exit",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
```

**Usage:**
```kotlin
if (showExitDialog) {
    ExitConfirmationDialog(
        onConfirm = {
            activity?.finish()
        },
        onDismiss = {
            showExitDialog = false
        }
    )
}
```

---

### 4. MainActivity.kt - Key Parts

**Imports to add:**
```kotlin
import cricpeak.scorelive.ui.components.ExitConfirmationDialog
import cricpeak.scorelive.ui.components.InternetConnectionDialog
import cricpeak.scorelive.util.NetworkUtils
```

**Modified setContent:**
```kotlin
setContent {
    CRICSCORETheme {
        AppNavigation(activity = this)  // Pass activity reference
    }
}
```

**AppNavigation state setup:**
```kotlin
// Internet connectivity state
var showNoInternetDialog by remember { mutableStateOf(false) }
var isInternetAvailable by remember { mutableStateOf(true) }

// Exit confirmation dialog state
var showExitDialog by remember { mutableStateOf(false) }

// Check internet connectivity on launch
remember {
    isInternetAvailable = NetworkUtils.isInternetAvailable(context)
    showNoInternetDialog = !isInternetAvailable
}
```

**Dialog display logic:**
```kotlin
if (showNoInternetDialog && !isInternetAvailable) {
    InternetConnectionDialog(
        onDismiss = {
            isInternetAvailable = NetworkUtils.isInternetAvailable(context)
            if (isInternetAvailable) {
                showNoInternetDialog = false
            }
        }
    )
}

if (showExitDialog) {
    ExitConfirmationDialog(
        onConfirm = {
            activity?.finish()
        },
        onDismiss = {
            showExitDialog = false
        }
    )
}
```

**Back button handling:**
```kotlin
// Handle back press to show exit dialog
val onBackPressed = {
    val currentDestination = navController.currentDestination?.route
    if (currentDestination == "home" || 
        currentDestination == "splash" || 
        currentDestination == "onboarding") {
        showExitDialog = true
    } else {
        navController.popBackStack()
    }
}
```

---

## Common Modifications

### Change Internet Dialog Colors
```kotlin
// In InternetConnectionDialog
.background(
    color = Color(0xFF1A1A2E),  // Change background
    shape = RoundedCornerShape(16.dp)
)
.border(
    width = 1.dp,
    color = Color.Red.copy(alpha = 0.3f),  // Change to red
    shape = RoundedCornerShape(16.dp)
)
```

### Change Button Appearance
```kotlin
Button(
    onClick = onDismiss,
    modifier = Modifier
        .fillMaxWidth()
        .height(56.dp),  // Increase height
    colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF00AA88),  // Custom color
        contentColor = Color.White
    ),
    shape = RoundedCornerShape(12.dp)  // More rounded
)
```

### Add More Network Types
```kotlin
// In NetworkUtils.kt
return when {
    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true  // Add VPN
    else -> false
}
```

### Change Exit Dialog Text
```kotlin
Text(
    text = "Leave CricScore?",  // Custom title
    fontSize = 20.sp,
    fontWeight = FontWeight.Bold
)

Text(
    text = "You'll lose your current session.",  // Custom message
    fontSize = 14.sp
)

Text(text = "Stay")  // Custom cancel text
Text(text = "Quit")  // Custom exit text
```

### Trigger Exit Dialog Programmatically
```kotlin
// From any composable with access to navController
val currentRoute = navController.currentDestination?.route
if (currentRoute == "home") {
    showExitDialog = true  // Show exit dialog
}
```

### Disable Internet Dialog Temporarily
```kotlin
// Set showNoInternetDialog to false
if (false && showNoInternetDialog && !isInternetAvailable) {  // Temporarily disabled
    InternetConnectionDialog(...)
}
```

### Add Animation to Dialog
```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

AnimatedVisibility(
    visible = showNoInternetDialog,
    enter = fadeIn(),
    exit = fadeOut()
) {
    InternetConnectionDialog(...)
}
```

---

## Import Statements Reference

```kotlin
// For dialogs
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// For components
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

// For network check
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

// For utilities
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
```

---

## State Management Patterns

### Internet Check Pattern
```kotlin
var isInternetAvailable by remember { mutableStateOf(true) }

remember {
    isInternetAvailable = NetworkUtils.isInternetAvailable(context)
}

// Later, recheck
isInternetAvailable = NetworkUtils.isInternetAvailable(context)
if (isInternetAvailable) {
    showNoInternetDialog = false
}
```

### Exit Dialog Pattern
```kotlin
var showExitDialog by remember { mutableStateOf(false) }

// Show dialog
showExitDialog = true

// Handle confirm
Button(onClick = { activity?.finish() })

// Handle dismiss
Button(onClick = { showExitDialog = false })
```

### Combined Pattern
```kotlin
var showNoInternetDialog by remember { mutableStateOf(false) }
var isInternetAvailable by remember { mutableStateOf(true) }
var showExitDialog by remember { mutableStateOf(false) }

// Check on launch
remember {
    val available = NetworkUtils.isInternetAvailable(context)
    isInternetAvailable = available
    showNoInternetDialog = !available
}
```

---

## Troubleshooting Code

### Check if dialogs are showing
```kotlin
// Add logging
remember {
    isInternetAvailable = NetworkUtils.isInternetAvailable(context)
    showNoInternetDialog = !isInternetAvailable
    Log.d("InternetCheck", "Internet Available: $isInternetAvailable")
}
```

### Verify button clicks
```kotlin
Button(
    onClick = {
        Log.d("DialogClick", "Button clicked")
        onDismiss()
    }
)
```

### Check navigation route
```kotlin
val currentDestination = navController.currentDestination?.route
Log.d("Navigation", "Current route: $currentDestination")

if (currentDestination == "home" || 
    currentDestination == "splash" || 
    currentDestination == "onboarding") {
    showExitDialog = true
    Log.d("Dialog", "Exit dialog shown")
} else {
    navController.popBackStack()
    Log.d("Navigation", "Navigating back")
}
```


