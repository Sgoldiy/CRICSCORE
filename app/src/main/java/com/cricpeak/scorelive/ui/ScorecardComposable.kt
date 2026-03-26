package com.cricpeak.scorelive.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cricpeak.scorelive.R
import com.cricpeak.scorelive.ads.AdsState
import com.cricpeak.scorelive.ads.BannerAdView
import com.cricpeak.scorelive.ui.apicallfiles.BatsmanStats
import com.cricpeak.scorelive.ui.apicallfiles.BowlerStats
import com.cricpeak.scorelive.ui.apicallfiles.InningsData
import com.cricpeak.scorelive.ui.theme.CardBackground
import com.cricpeak.scorelive.ui.theme.GilroyFontFamily
import com.cricpeak.scorelive.ui.theme.TextGray
import java.util.Locale

@Composable
fun ScorecardScreen(inningsList: List<InningsData>, onPlayerClick: (PlayerProfileData) -> Unit = {}, adsState: AdsState? = null) {
    if (inningsList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "No scorecard data available", 
                color = TextGray, 
                fontFamily = GilroyFontFamily,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    var selectedInningsTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    },
                    onDragEnd = {
                        if (totalDrag > 100) { // Swipe Right
                            if (selectedInningsTab > 0) selectedInningsTab--
                        } else if (totalDrag < -100) { // Swipe Left
                            if (selectedInningsTab < inningsList.size - 1) selectedInningsTab++
                        }
                    }
                )
            }
    ) {
        // Innings Tabs
        InningsTabs(inningsList, selectedInningsTab) { selectedInningsTab = it }

        Spacer(modifier = Modifier.height(16.dp))

        // Display selected innings with animation
        AnimatedContent(
            targetState = selectedInningsTab,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "ScorecardTabTransition"
        ) { targetTabIndex ->
            InningsScorecard(inningsList[targetTabIndex], onPlayerClick, adsState)
        }
    }
}

@Composable
fun InningsTabs(inningsList: List<InningsData>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        inningsList.forEachIndexed { index, innings ->
            val isSelected = selectedIndex == index
            Card(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(130.dp)
                    .clickable { onTabSelected(index) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF00BFA5) else CardBackground
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (!isSelected) BorderStroke(1.dp, Color.White.copy(0.1f)) else null
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = innings.teamShort,
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = GilroyFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun InningsScorecard(innings: InningsData, onPlayerClick: (PlayerProfileData) -> Unit = {}, adsState: AdsState? = null) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Batting Section
        BattingSection(innings, onPlayerClick)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 4. Scorecard Banner Ad - Between batting and bowling sections
        if (adsState?.scorecard_banner?.enabled == true && adsState.scorecard_banner.ads_unit.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                BannerAdView(adUnitId = adsState.scorecard_banner.ads_unit)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Bowling Section
        BowlingSection(innings.bowlers, onPlayerClick)
    }
}

@Composable
fun BattingSection(innings: InningsData, onPlayerClick: (PlayerProfileData) -> Unit = {}) {
    val batsmen = innings.batsmen
    if (batsmen.isEmpty()) return

    Text(
        text = "Batting Summary",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        fontFamily = GilroyFontFamily,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        textAlign = TextAlign.Center
    )

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        val chunkedBatsmen = batsmen.chunked(2)
        chunkedBatsmen.forEach { rowBatsmen ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowBatsmen.forEach { batsman ->
                    Box(modifier = Modifier.weight(1f)) {
                        BatsmanCard(batsman, onPlayerClick)
                    }
                }
                if (rowBatsmen.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Total and Extras at the bottom of Batting Summary
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E262C).copy(0.4f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(0.05f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("EXTRAS", color = TextGray, fontSize = 11.sp, fontFamily = GilroyFontFamily, fontWeight = FontWeight.Bold)
                    Text("${innings.extras}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = GilroyFontFamily)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TOTAL", color = TextGray, fontSize = 11.sp, fontFamily = GilroyFontFamily, fontWeight = FontWeight.Bold)
                    Text("${innings.total}/${innings.wickets}", color = Color(0xFF00BFA5), fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = GilroyFontFamily)
                }
            }
        }
    }
}

@Composable
fun BatsmanCard(batsman: BatsmanStats, onPlayerClick: (PlayerProfileData) -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(244.dp)
            .border(1.dp, Color(0xFF1E262C), RoundedCornerShape(20.dp))
            .clickable { 
                onPlayerClick(
                    PlayerProfileData(
                        id = batsman.playerId,
                        name = batsman.name,
                        role = "Batsman",
                        imageUrl = batsman.imageUrl,
                        battingStyle = batsman.battingStyle,
                        bowlingStyle = batsman.bowlingStyle,
                        dob = batsman.dob,
                        birthPlace = batsman.birthPlace,
                        height = batsman.height,
                        bio = batsman.bio,
                        isCaptain = batsman.isCaptain,
                        isWicketKeeper = batsman.isWicketKeeper
                    )
                )
            },
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(115.dp)) {
                val isOut = batsman.outStatus != "Not Out"
                
                AsyncImage(
                    model = batsman.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.width(88.dp).height(115.dp).align(Alignment.TopCenter),
                    contentScale = ContentScale.Fit,
                    error = painterResource(id = R.drawable.batsman)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.5f to Color.Black.copy(alpha = 0.2f),
                                1.0f to Color.Black
                            )
                        )
                )
                
            Box(modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                    when {
                        batsman.balls == 0 && batsman.runs == 0 && !isOut -> Color(0xFFF39C12) // Orange for Did not bat
                        !isOut -> Color(0xFF34C759) // Green for Not out
                        else -> Color(0xFFFF3B30) // Red for Out
                    }
                )
                .align(Alignment.TopStart)
            )
        }
        
        Text(
            text = batsman.name, 
            color = Color.White, 
            fontSize = 14.sp, 
            fontWeight = FontWeight.Bold, 
            fontFamily = GilroyFontFamily, 
            maxLines = 1,
            textAlign = TextAlign.Center
        )
            
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(top = 4.dp)) {
                Text(text = batsman.runs.toString(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = GilroyFontFamily)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "(${batsman.balls})", color = Color.LightGray, fontSize = 12.sp, fontFamily = GilroyFontFamily)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            GradientDivider()
            Spacer(modifier = Modifier.weight(1f))
            
            val sr = String.format(Locale.US, "%.2f", batsman.strikeRate)
            Text(
                text = "4s: ${batsman.fours} | 6s: ${batsman.sixes} | SR: $sr",
                color = Color.LightGray, fontSize = 11.sp, fontFamily = GilroyFontFamily, textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.weight(1f))
            GradientDivider()
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = when {
                    batsman.balls == 0 && batsman.runs == 0 && batsman.outStatus == "Not Out" -> "Did not bat"
                    batsman.outStatus == "Not Out" -> "Not Out"
                    else -> batsman.dismissalDetail
                },
                color = Color.LightGray, fontSize = 11.sp, fontFamily = GilroyFontFamily, textAlign = TextAlign.Center,
                minLines = 1, maxLines = 1, modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
            )
        }
    }
}

@Composable
fun BowlingSection(bowlers: List<BowlerStats>, onPlayerClick: (PlayerProfileData) -> Unit = {}) {
    if (bowlers.isEmpty()) return

    Text(
        text = "Bowling Summary",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        fontFamily = GilroyFontFamily,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp),
        textAlign = TextAlign.Center
    )

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        val chunkedBowlers = bowlers.chunked(2)
        chunkedBowlers.forEach { rowBowlers ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowBowlers.forEach { bowler ->
                    Box(modifier = Modifier.weight(1f)) {
                        BowlerCard(bowler, onPlayerClick)
                    }
                }
                if (rowBowlers.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun BowlerCard(bowler: BowlerStats, onPlayerClick: (PlayerProfileData) -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(244.dp)
            .border(1.dp, Color(0xFF1E262C), RoundedCornerShape(20.dp))
            .clickable {
                onPlayerClick(
                    PlayerProfileData(
                        id = bowler.playerId,
                        name = bowler.name,
                        role = "Bowler",
                        imageUrl = bowler.imageUrl,
                        battingStyle = bowler.battingStyle,
                        bowlingStyle = bowler.bowlingStyle,
                        dob = bowler.dob,
                        birthPlace = bowler.birthPlace,
                        height = bowler.height,
                        bio = bowler.bio,
                        isCaptain = bowler.isCaptain,
                        isWicketKeeper = bowler.isWicketKeeper
                    )
                )
            },
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(115.dp)) {
                
                AsyncImage(
                    model = bowler.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.width(88.dp).height(115.dp).align(Alignment.TopCenter),
                    contentScale = ContentScale.Fit,
                    error = painterResource(id = R.drawable.bowler)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.5f to Color.Black.copy(alpha = 0.2f),
                                1.0f to Color.Black
                            )
                        )
                )
        }
        
        Text(
            text = bowler.name, 
            color = Color.White, 
            fontSize = 14.sp, 
            fontWeight = FontWeight.Bold, 
            fontFamily = GilroyFontFamily, 
            maxLines = 1,
            textAlign = TextAlign.Center
        )
            
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(top = 4.dp)) {
                Text(text = "${bowler.wickets}-${bowler.runs}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = GilroyFontFamily)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "(${bowler.overs})", color = Color.LightGray, fontSize = 12.sp, fontFamily = GilroyFontFamily)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            GradientDivider()
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Mdns: ${bowler.maidens} | Dots: ${bowler.dots} | Wds: ${bowler.wides}",
                color = Color.LightGray, fontSize = 11.sp, fontFamily = GilroyFontFamily, textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.weight(1f))
            GradientDivider()
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Economy: ${String.format(Locale.US, "%.2f", bowler.economy)}",
                color = Color.LightGray, fontSize = 11.sp, fontFamily = GilroyFontFamily, textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GradientDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                )
            )
    )
}
