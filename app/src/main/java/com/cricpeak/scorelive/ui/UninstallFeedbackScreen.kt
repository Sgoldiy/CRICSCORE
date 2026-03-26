package com.cricpeak.scorelive.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricpeak.scorelive.ui.theme.AccentGreen
import com.cricpeak.scorelive.ui.theme.BayonFont
import com.cricpeak.scorelive.ui.theme.CardBackground
import com.cricpeak.scorelive.ui.theme.DarkBackground
import com.cricpeak.scorelive.ui.theme.GilroyFontFamily
import com.cricpeak.scorelive.ui.theme.RadialTeal
import com.cricpeak.scorelive.ui.theme.TextGray
import com.cricpeak.scorelive.ads.AdsState
import com.cricpeak.scorelive.ads.BannerAdView
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UninstallFeedbackScreen(onBack: () -> Unit, adsState: AdsState? = null) {
    val context = LocalContext.current
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    val radialGradient = Brush.radialGradient(
        colors = listOf(RadialTeal.copy(alpha = 0.5f), Color.Transparent),
        center = androidx.compose.ui.geometry.Offset(540f, -50f),
        radius = 1000f
    )

    val feedbackOptions = listOf(
        "UI is not better",
        "Information is not up to mark",
        "App is okay but needs improvement",
        "Frequent crashes",
        "Too many ads",
        "Other reasons"
    )

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "UNINSTALL",
                        color = Color.White,
                        fontFamily = BayonFont,
                        fontSize = 24.sp,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(radialGradient)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "We're sorry to see you go!",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = GilroyFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Please tell us why you want to uninstall the app so we can improve it.",
                    color = TextGray,
                    fontSize = 14.sp,
                    fontFamily = GilroyFontFamily,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(feedbackOptions) { option ->
                        FeedbackOptionItem(
                            option = option,
                            isSelected = selectedReason == option,
                            onSelect = { selectedReason = option }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Uninstall Banner Ad - At the bottom (above navigation bar)
                if (adsState?.uninstall_banner?.enabled == true && adsState.uninstall_banner.ads_unit.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .padding(bottom = 32.dp) // Approximate navigation bar height to position above it
                    ) {
                        BannerAdView(adUnitId = adsState.uninstall_banner.ads_unit)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSubmitting
                    ) {
                        Text(
                            text = "KEEP USING",
                            color = Color.White,
                            fontFamily = GilroyFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            if (selectedReason != null && !isSubmitting) {
                                isSubmitting = true
                                // Firebase Submission
                                val database = FirebaseDatabase.getInstance()
                                val myRef = database.getReference("uninstall_feedback")
                                
                                val feedbackData = mapOf(
                                    "reason" to selectedReason,
                                    "timestamp" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                                    "device_model" to android.os.Build.MODEL
                                )

                                myRef.push().setValue(feedbackData)
                                    .addOnSuccessListener {
                                        isSubmitting = false
                                        // Navigate to App Settings for Uninstallation
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        val uri = Uri.fromParts("package", context.packageName, null)
                                        intent.data = uri
                                        context.startActivity(intent)
                                    }
                                    .addOnFailureListener { e ->
                                        isSubmitting = false
                                        Toast.makeText(context, "Error: ${e.message}. Please try again.", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedReason != null) Color.Red.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.5f)
                        ),
                        enabled = selectedReason != null && !isSubmitting,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "UNINSTALL",
                                color = Color.White,
                                fontFamily = GilroyFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackOptionItem(
    option: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        color = if (isSelected) AccentGreen.copy(alpha = 0.1f) else CardBackground,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isSelected) AccentGreen else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = AccentGreen,
                    unselectedColor = TextGray
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = option,
                color = Color.White,
                fontSize = 15.sp,
                fontFamily = GilroyFontFamily,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
