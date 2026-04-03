package com.cricpeak.scorelive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricpeak.scorelive.ui.theme.BayonFont
import com.cricpeak.scorelive.ui.theme.DarkBackground
import com.cricpeak.scorelive.ui.theme.RadialGold
import com.cricpeak.scorelive.ui.theme.RadialTeal
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit, isPremium: Boolean = false) {

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

    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .align(Alignment.TopCenter)
                .background(if (isPremium) goldGradient else radialGradient)
        )

        Text(
            text = "CRICPEAK",
            color = Color.White,
            fontSize = 48.sp,
            fontFamily = BayonFont,
            letterSpacing = (48 * 0.07).sp,
            fontWeight = FontWeight.Normal
        )
    }
}
