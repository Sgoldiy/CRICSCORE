package com.cricpeak.scorelive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricpeak.scorelive.ui.theme.BayonFont
import com.cricpeak.scorelive.ui.theme.DarkBackground
import com.cricpeak.scorelive.ui.theme.GilroyFontFamily
import com.cricpeak.scorelive.ui.theme.RadialGold
import com.cricpeak.scorelive.ui.theme.RadialTeal
import com.cricpeak.scorelive.ui.theme.GoldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit, isPremium: Boolean = false) {
    val radialGradient = Brush.radialGradient(
        colors = listOf(RadialTeal.copy(alpha = 0.5f), Color.Transparent),
        center = Offset(540f, -50f),
        radius = 1000f
    )

    val goldGradient = Brush.radialGradient(
        colors = listOf(
            RadialGold.copy(alpha = 0.25f),
            Color.Transparent
        ),
        center = Offset(540f, -50f),
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
                    .background(if (isPremium) goldGradient else radialGradient)
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
                    content = "CricScore does not collect any personally identifiable information from its users. We may collect non-personal information such as device type and operating system to improve our app's performance.",
                    isPremium = isPremium
                )

                PolicySection(
                    title = "Use of Information",
                    content = "Any information collected is used solely for the purpose of providing and improving the CricScore service. We do not sell or share your data with third parties.",
                    isPremium = isPremium
                )

                PolicySection(
                    title = "Ads and Analytics",
                    content = "We use Google AdMob to serve advertisements. AdMob may use device identifiers to personalize ads. We also use internal analytics to monitor app stability and usage patterns.",
                    isPremium = isPremium
                )

                PolicySection(
                    title = "AI Features",
                    content = "Our AI prediction features use Google Gemini. Data sent to Gemini is limited to match statistics and does not include user data.",
                    isPremium = isPremium
                )

                PolicySection(
                    title = "Changes to This Policy",
                    content = "We may update our Privacy Policy from time to time. You are advised to review this page periodically for any changes.",
                    isPremium = isPremium
                )

                Spacer(modifier = Modifier.height(40.dp))

                val accentColor = if (isPremium) GoldPrimary else Color(0xFF00BFA5)
                Text(
                    text = "Last updated: October 2023",
                    color = accentColor.copy(alpha = 0.5f),
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
fun PolicySection(title: String, content: String, isPremium: Boolean = false) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            color = if (isPremium) GoldPrimary else Color(0xFF00BFA5),
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
