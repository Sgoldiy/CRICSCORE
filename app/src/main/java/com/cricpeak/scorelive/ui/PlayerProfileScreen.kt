package com.cricpeak.scorelive.ui

import com.cricpeak.scorelive.ui.theme.GilroyFontFamily
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cricpeak.scorelive.R
import com.cricpeak.scorelive.ui.theme.GoldPrimary

data class PlayerProfileData(
    val id: Int,
    val name: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val role: String = "Player",
    val imageUrl: String,
    val battingStyle: String = "N/A",
    val bowlingStyle: String = "N/A",
    val dob: String? = null,
    val birthPlace: String? = null,
    val height: String? = null,
    val bio: String? = null,
    val isCaptain: Boolean = false,
    val isWicketKeeper: Boolean = false
)

@Composable
fun PlayerProfileScreen(
    player: PlayerProfileData,
    onBack: () -> Unit,
    isPremium: Boolean = false
) {
    val scrollState = rememberScrollState()
    
    val tealGradient = listOf(
        Color(0xFF0D2526),
        Color(0xFF0F1115)
    )
    
    val goldGradient = listOf(
        Color(0xFF2A2A1A),
        Color(0xFF0F1115)
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .verticalScroll(scrollState)
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isPremium) goldGradient else tealGradient
                    )
                )
        ) {
            // Player Image spanning on the right
            AsyncImage(
                model = player.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .align(Alignment.BottomEnd),
                contentScale = ContentScale.Fit,
                error = painterResource(id = if (player.role.contains("Bowl", ignoreCase = true)) R.drawable.bowler else R.drawable.batsman)
            )
            
            // Fading out the bottom of the image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF0F1115)
                            )
                        )
                    )
            )

            // Content Left Side
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close Button Top Right
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Text("✕", color = Color.White, fontSize = 18.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = player.name,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GilroyFontFamily,
                    lineHeight = 38.sp,
                    modifier = Modifier.width(200.dp)
                )

                if (!player.firstName.isNullOrEmpty() && !player.lastName.isNullOrEmpty()) {
                    Text(
                        text = "${player.firstName} ${player.middleName ?: ""} ${player.lastName}".replace("  ", " "),
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontFamily = GilroyFontFamily,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = Color(0xFF00BFA5).copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = player.role.uppercase(),
                            color = Color(0xFF00BFA5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GilroyFontFamily,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (player.isCaptain) {
                        Surface(
                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "CAPTAIN",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GilroyFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (player.isWicketKeeper) {
                        Surface(
                            color = Color(0xFF42A5F5).copy(alpha = 0.2f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "WK",
                                color = Color(0xFF42A5F5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GilroyFontFamily,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                PlayerStatItem("Batting Style", player.battingStyle)
                Spacer(modifier = Modifier.height(20.dp))
                PlayerStatItem("Bowling Style", player.bowlingStyle)
            }
        }

        // Rest of the data
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    PlayerStatItem("Born", player.dob ?: "N/A")
                }
                Column(modifier = Modifier.weight(1f)) {
                    PlayerStatItem("Birth Place", player.birthPlace ?: "N/A")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            if (!player.height.isNullOrEmpty()) {
                PlayerStatItem("Height", player.height)
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (!player.bio.isNullOrEmpty()) {
                Text(
                    text = "Biography",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    fontFamily = GilroyFontFamily,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = player.bio,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontFamily = GilroyFontFamily
                )
                Spacer(modifier = Modifier.height(40.dp))
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PlayerStatItem(label: String, value: String) {
    Column {
        Text(
            text = label, 
            color = Color.Gray, 
            fontSize = 13.sp, 
            fontFamily = GilroyFontFamily,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (value.isNullOrEmpty() || value == "null") "N/A" else value,
            color = Color.White,
            fontSize = 17.sp, 
            fontWeight = FontWeight.Bold, 
            fontFamily = GilroyFontFamily
        )
    }
}
