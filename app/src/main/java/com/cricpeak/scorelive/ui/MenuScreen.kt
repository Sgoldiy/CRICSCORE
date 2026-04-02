package com.cricpeak.scorelive.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricpeak.scorelive.ui.theme.AccentGreen
import com.cricpeak.scorelive.ui.theme.BayonFont
import com.cricpeak.scorelive.ui.theme.CardBackground
import com.cricpeak.scorelive.ui.theme.DarkBackground
import com.cricpeak.scorelive.ui.theme.GilroyFontFamily
import com.cricpeak.scorelive.ui.theme.RadialTeal
import com.cricpeak.scorelive.ui.theme.TextGray
import androidx.core.net.toUri
import com.cricpeak.scorelive.ads.AdsState
import com.cricpeak.scorelive.ads.BannerAdView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onBack: () -> Unit, 
    onPrivacyPolicyClick: () -> Unit,
    onUninstallClick: () -> Unit,
    isPremium: Boolean = false,
    adsState: AdsState? = null
) {
    val context = LocalContext.current
    val radialGradient = Brush.radialGradient(
        colors = listOf(RadialTeal.copy(alpha = 0.5f), Color.Transparent),
        center = androidx.compose.ui.geometry.Offset(540f, -50f),
        radius = 1000f
    )

    val menuItems = listOf(
        MenuItem("Rate Us", Icons.Default.Star) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
            context.startActivity(intent)
        },
        MenuItem("Share App", Icons.Default.Share) {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out CricScore for live cricket scores! https://play.google.com/store/apps/details?id=${context.packageName}")
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, null))
        },
        MenuItem("Notifications", Icons.Default.Notifications) {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${context.packageName}".toUri()
                }
            }
            context.startActivity(intent)
        },
        MenuItem("Uninstall App", Icons.Default.Delete) {
            onUninstallClick()
        },
        MenuItem("Privacy Policy", Icons.Default.Lock) {
            onPrivacyPolicyClick()
        }
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "MENU",
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = if (adsState?.menu_banner?.enabled == true && adsState.menu_banner.ads_unit.isNotEmpty() && !isPremium) 16.dp else 40.dp)
            ) {
                items(menuItems) { item ->
                    MenuListItem(item)
                }
                
                // 2. Menu Banner Ad - At the bottom of menu screen (above navigation bar)
                if (adsState?.menu_banner?.enabled == true && adsState.menu_banner.ads_unit.isNotEmpty() && !isPremium) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .padding(bottom = 32.dp) // Approximate navigation bar height to position above it
                        ) {
                            BannerAdView(adUnitId = adsState.menu_banner.ads_unit, isPremium = isPremium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuListItem(item: MenuItem) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() },
        color = CardBackground,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = GilroyFontFamily,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "›",
                color = TextGray,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

data class MenuItem(val title: String, val icon: ImageVector, val onClick: () -> Unit)
