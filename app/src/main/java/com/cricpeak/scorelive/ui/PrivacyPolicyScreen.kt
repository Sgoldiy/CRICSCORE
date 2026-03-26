package com.cricpeak.scorelive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricpeak.scorelive.ui.theme.BayonFont
import com.cricpeak.scorelive.ui.theme.DarkBackground
import com.cricpeak.scorelive.ui.theme.GilroyFontFamily
import com.cricpeak.scorelive.ui.theme.RadialTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val radialGradient = Brush.radialGradient(
        colors = listOf(RadialTeal.copy(alpha = 0.5f), Color.Transparent),
        center = androidx.compose.ui.geometry.Offset(540f, -50f),
        radius = 1000f
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "PRIVACY POLICY",
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
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                PolicySection(
                    title = "Information We Collect",
                    content = "CricScore does not collect any personally identifiable information from its users. We may collect non-personal information such as device type and operating system to improve our app's performance."
                )

                PolicySection(
                    title = "Use of Information",
                    content = "Any information collected is used solely for the purpose of providing and improving the CricScore service. We do not sell or share your data with third parties."
                )

                PolicySection(
                    title = "Ads and Analytics",
                    content = "We use Google AdMob to serve advertisements. AdMob may use device identifiers to personalize ads. We also use internal analytics to monitor app stability and usage patterns."
                )

                PolicySection(
                    title = "AI Features",
                    content = "Our AI prediction features use Google Gemini. Data sent to Gemini is limited to match statistics and does not include user data."
                )

                PolicySection(
                    title = "Changes to This Policy",
                    content = "We may update our Privacy Policy from time to time. You are advised to review this page periodically for any changes."
                )

                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    text = "Last updated: October 2023",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontFamily = GilroyFontFamily,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun PolicySection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            color = Color(0xFF00BFA5),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = GilroyFontFamily
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontFamily = GilroyFontFamily
        )
    }
}
