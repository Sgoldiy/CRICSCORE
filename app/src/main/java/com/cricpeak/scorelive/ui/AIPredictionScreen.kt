package com.cricpeak.scorelive.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cricpeak.scorelive.billing.BillingViewModel
import com.cricpeak.scorelive.ui.apicallfiles.MatchDetailsData
import com.cricpeak.scorelive.ui.apicallfiles.MatchDetailsViewModel
import com.cricpeak.scorelive.ui.apicallfiles.PredictionState
import com.cricpeak.scorelive.ui.theme.AccentGreen
import com.cricpeak.scorelive.ui.theme.DarkBackground
import com.cricpeak.scorelive.ui.theme.GilroyFontFamily
import com.cricpeak.scorelive.ui.theme.GoldPrimary
import com.cricpeak.scorelive.ui.theme.RadialGold
import com.cricpeak.scorelive.ui.theme.RadialTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIPredictionScreen(
    matchId: Int,
    onBackClick: () -> Unit,
    viewModel: MatchDetailsViewModel = viewModel(),
    billingViewModel: BillingViewModel = viewModel(),
    onPurchaseClick: () -> Unit = {},
) {
    val matchDetails = viewModel.matchDetails.collectAsState().value
    val predictionState = viewModel.predictionState.collectAsState().value
    val isPremium by billingViewModel.isPremium.collectAsState()

    LaunchedEffect(matchId) {
        viewModel.loadMatchDetails(matchId)
    }

    val goldGradient = Brush.radialGradient(
        colors = listOf(
            RadialGold.copy(alpha = 0.25f),
            Color.Transparent
        ),
        center = Offset(540f, -50f),
        radius = 1000f
    )

    val radialTealGradient = Brush.verticalGradient(
        colors = listOf(RadialTeal.copy(alpha = 0.2f), Color.Transparent)
    )

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AI Analysis",
                        fontFamily = GilroyFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(if (isPremium) goldGradient else radialTealGradient)
        ) {
            val data = matchDetails
            if (data == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = if (isPremium) GoldPrimary else AccentGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Two Team Logos & Winning Chance
                    item {
                        WinningChanceSection(data, predictionState, isPremium)
                    }
                    // 2. Summary (Analysis)
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        SummarySection(predictionState)
                    }

                    // 3. 5 Players from each side
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            "KEY PLAYERS TO WATCH",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GilroyFontFamily,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                        PlayersGridSimplified(predictionState)
                    }

                    // 4. AI Disclaimer
                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                        Text(
                            text = "Content is generated by AI",
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 12.sp,
                            fontFamily = GilroyFontFamily,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PremiumLockOverlay(onPurchaseClick: () -> Unit) {
    Surface(
        color = Color.White.copy(0.05f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Unlock AI Analysis",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = GilroyFontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Get detailed tactical summaries and key player insights with our premium AI-powered analysis.",
                color = Color.White.copy(0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                fontFamily = GilroyFontFamily
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onPurchaseClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    "Get Premium",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WinningChanceSection(
    data: MatchDetailsData,
    state: PredictionState,
    isPremium: Boolean = false,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // VS Section with Logos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TeamLogoLarge(data.homeTeam.logoUrl)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    data.homeTeam.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                "VS",
                color = if (isPremium) GoldPrimary.copy(alpha = 0.5f) else AccentGreen.copy(alpha = 0.5f),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                fontFamily = GilroyFontFamily
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TeamLogoLarge(data.awayTeam.logoUrl)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    data.awayTeam.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "${data.venue.name}, ${data.venue.city} • ${data.startDateTime}",
            color = Color.White.copy(0.5f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (state is PredictionState.Success) {
            val result = state.prediction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        data.homeTeam.shortName,
                        color = Color.White.copy(0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${result.homeWinProb}%",
                        color = if (isPremium) GoldPrimary else AccentGreen,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = GilroyFontFamily
                    )
                }
                Text(
                    "WIN CHANCE",
                    color = Color.White.copy(0.3f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        data.awayTeam.shortName,
                        color = Color.White.copy(0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${result.awayWinProb}%",
                        color = Color(0xFFFF5252),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = GilroyFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.05f))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(result.homeWinProb.toFloat().coerceAtLeast(1f))
                            .background(if (isPremium) GoldPrimary else AccentGreen)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(result.awayWinProb.toFloat().coerceAtLeast(1f))
                            .background(Color(0xFFFF5252))
                    )
                }
            }
        } else if (state is PredictionState.Loading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = if (isPremium) GoldPrimary else AccentGreen,
                trackColor = Color.White.copy(0.05f)
            )
        }
    }
}

@Composable
fun TeamLogoLarge(url: String) {
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color.White.copy(0.05f))
            .border(2.dp, Color.White.copy(0.1f), CircleShape)
            .padding(12.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun SummarySection(state: PredictionState) {
    Surface(
        color = Color.White.copy(0.05f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "AI TACTICAL SUMMARY",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            when (state) {
                is PredictionState.Success -> {
                    Text(
                        state.prediction.analysis,
                        color = Color.White.copy(0.9f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontFamily = GilroyFontFamily
                    )
                }

                is PredictionState.Loading -> {
                    Text(
                        "AI is generating tactical summary...",
                        color = Color.White.copy(0.4f),
                        fontSize = 14.sp,
                        style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    )
                }

                is PredictionState.Error -> {
                    Text(
                        "AI is not responding right now. Please try again later.",
                        color = Color.Red.copy(0.7f),
                        fontSize = 14.sp,
                        fontFamily = GilroyFontFamily
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
fun PlayersGridSimplified(state: PredictionState) {
    if (state is PredictionState.Success) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Home players
            Column(modifier = Modifier.weight(1f)) {
                state.prediction.homePlayers.take(5).forEach { name ->
                    SimplePlayerRow(name, isRight = false)
                }
            }
            // Away players
            Column(modifier = Modifier.weight(1f)) {
                state.prediction.awayPlayers.take(5).forEach { name ->
                    SimplePlayerRow(name, isRight = true)
                }
            }
        }
    } else if (state is PredictionState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Analyzing key player impacts...",
                color = Color.White.copy(0.3f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SimplePlayerRow(name: String, isRight: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isRight) Arrangement.End else Arrangement.Start
    ) {
        if (!isRight) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.05f))
                    .padding(2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                name,
                color = Color.White.copy(0.8f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                name,
                color = Color.White.copy(0.8f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(0.05f))
                    .padding(2.dp)
            )
        }
    }
}
