package com.cricpeak.scorelive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricpeak.scorelive.R
import com.cricpeak.scorelive.ui.theme.*

@Composable
fun PurchaseScreen(onClose: () -> Unit) {
    var isMonthly by remember { mutableStateOf(true) }

    // Golden gradient similar to homescreen but with gold tones
    val radialGradient = Brush.radialGradient(
        colors = listOf(Color(0xFFFFD700).copy(alpha = 0.25f), Color.Transparent),
        center = androidx.compose.ui.geometry.Offset(540f, -50f),
        radius = 1200f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Radial Background Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(radialGradient)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Get Premium",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GilroyFontFamily
                    )
                    Text(
                        text = "Get Remove All Ads",
                        color = TextGray,
                        fontSize = 14.sp,
                        fontFamily = GilroyFontFamily
                    )
                }

                // Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gold Man Image
            Icon(
                painter = painterResource(id = R.drawable.ic_gold_man),
                contentDescription = null,
                modifier = Modifier
                    .size(320.dp)
                    .padding(horizontal = 24.dp),
                tint = Color.Unspecified
            )

            // Remove All Ads Title
            Text(
                text = "Remove All Ads",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = GilroyFontFamily
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Go Remove All Ads, Score & Point Table\nAll Type Ads Removeos",
                color = TextGray,
                fontSize = 14.sp,
                fontFamily = GilroyFontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Monthly / Yearly Toggle
            Row(
                modifier = Modifier
                    .width(200.dp)
                    .height(44.dp)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(22.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isMonthly) Color(0xFF2E2A12) else Color.Transparent)
                        .border(
                            width = if (isMonthly) 1.dp else 0.dp,
                            color = if (isMonthly) Color(0xFFFFD700).copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { isMonthly = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Monthly",
                        color = if (isMonthly) Color(0xFFFFD700) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (isMonthly) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = GilroyFontFamily
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (!isMonthly) Color(0xFF2E2A12) else Color.Transparent)
                        .border(
                            width = if (!isMonthly) 1.dp else 0.dp,
                            color = if (!isMonthly) Color(0xFFFFD700).copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { isMonthly = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Yearly",
                        color = if (!isMonthly) Color(0xFFFFD700) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (!isMonthly) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = GilroyFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price Text
            Text(
                text = if (isMonthly) "$99.00" else "$499.00",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = GilroyFontFamily
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Go Premium Button
            Button(
                onClick = { /* Handle Purchase */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC317)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Go Premium",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GilroyFontFamily
                )
            }
        }
    }
}
