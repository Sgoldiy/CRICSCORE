package com.cricpeak.scorelive.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cricpeak.scorelive.R
import com.cricpeak.scorelive.ads.AdInterstitialManager
import com.cricpeak.scorelive.ads.AdRewardManager
import com.cricpeak.scorelive.ads.AdsState
import com.cricpeak.scorelive.ads.AdsViewModel
import com.cricpeak.scorelive.ads.BannerAdView
import com.cricpeak.scorelive.ads.RewardPrefs
import com.cricpeak.scorelive.ui.apicallfiles.CommentaryEntry
import com.cricpeak.scorelive.ui.apicallfiles.CommentsData
import com.cricpeak.scorelive.ui.apicallfiles.CommentsState
import com.cricpeak.scorelive.ui.apicallfiles.InningsData
import com.cricpeak.scorelive.ui.apicallfiles.MatchDetailsData
import com.cricpeak.scorelive.ui.apicallfiles.MatchDetailsState
import com.cricpeak.scorelive.ui.apicallfiles.MatchDetailsViewModel
import com.cricpeak.scorelive.ui.apicallfiles.MatchInfo
import com.cricpeak.scorelive.ui.apicallfiles.OverSummary
import com.cricpeak.scorelive.ui.apicallfiles.PlayerDetail
import com.cricpeak.scorelive.ui.apicallfiles.ScoreAnimation
import com.cricpeak.scorelive.ui.apicallfiles.getBattingTeam
import com.cricpeak.scorelive.ui.apicallfiles.getFirstInning
import com.cricpeak.scorelive.ui.apicallfiles.getFourthInning
import com.cricpeak.scorelive.ui.apicallfiles.getSampleInningsData
import com.cricpeak.scorelive.ui.apicallfiles.getSecondInning
import com.cricpeak.scorelive.ui.apicallfiles.getThirdInning
import com.cricpeak.scorelive.ui.components.RewardPolicyDialog
import com.cricpeak.scorelive.ui.theme.CardBackground
import com.cricpeak.scorelive.ui.theme.DarkBackground
import com.cricpeak.scorelive.ui.theme.GilroyFontFamily
import com.cricpeak.scorelive.ui.theme.RadialTeal
import com.cricpeak.scorelive.ui.theme.TextGray
import kotlin.math.cos
import kotlin.math.sin

// Data class for Player from API
data class Player(
    val id: Int,
    val name: String,
    val imageUrl: String = "",
    val role: String = "",
    val teamId: Int = 0,
    val battingStyle: String = "N/A",
    val bowlingStyle: String = "N/A",
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(matchId: Int, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Summary", "Scorecard", "Commentary")
    val TEST_AD_ID = "ca-app-pub-3940256099942544/1033173712"
    // Track previous tab index to detect changes
    var previousTabIndex by remember { mutableStateOf(0) }

    // Initialize ViewModels
    val viewModel: MatchDetailsViewModel = viewModel()
    val adsViewModel: AdsViewModel = viewModel()

    val matchDetailsState by viewModel.matchDetailsState.collectAsState()
    val commentsState by viewModel.commentsState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val scoreAnimation by viewModel.scoreAnimationState.collectAsState()
    val adsState: AdsState by adsViewModel.adsState.collectAsState()

    // Reward Ad States
    var showRewardPolicyDialog by remember { mutableStateOf(false) }
    var rewardPendingOver by remember { mutableStateOf<Int?>(null) }

    // Screen Policy Dialog State
    var showMatchDetailsPolicyDialog by remember { mutableStateOf(false) }

    // Free Overs Tracking Logic
    var unlockedUntilOver by remember {
        mutableIntStateOf(RewardPrefs.getOversUnlockedUntil(context, matchId))
    }
    var isFirstMatchLoad by remember { mutableStateOf(true) }

    // Global Ad pre-loading for Match Details
    LaunchedEffect(adsState) {
        val adConfig = adsState.inter_details
        if (adConfig.enabled && adConfig.ads_unit.isNotEmpty()) {
            AdInterstitialManager.MatchDetailsAdManager.loadAd(context, adConfig.ads_unit)
        }

        val rewardLiveConfig = adsState.reward_live
        if (rewardLiveConfig.enabled && rewardLiveConfig.ads_unit.isNotEmpty()) {
            AdRewardManager.loadAd(context, rewardLiveConfig.ads_unit)
        }
    }

// ✅ Tab Click Logic
    LaunchedEffect(selectedTabIndex) {

        if (selectedTabIndex != previousTabIndex && activity != null) {
            val adConfig = adsState.inter_details

            if (adConfig.enabled && adConfig.ads_unit.isNotEmpty()) {
                AdInterstitialManager.MatchDetailsAdManager.showAdIfAvailable(
                    activity = activity,
                    adUnitId = adConfig.ads_unit
                ) {
                    // Continue navigation if needed
                }
            }
        }
        previousTabIndex = selectedTabIndex
    }

    // State for UI data (to be populated from API)
    val matchInfoState = remember { mutableStateOf<MatchInfo?>(null) }
    var innings1 by remember { mutableStateOf(getSampleInningsData().first) }
    var innings2 by remember { mutableStateOf(getSampleInningsData().second) }
    var innings3 by remember { mutableStateOf<InningsData?>(null) }
    var innings4 by remember { mutableStateOf<InningsData?>(null) }

    // Squad states
    var homeTeamPlayers by remember { mutableStateOf<List<Player>>(emptyList()) }
    var awayTeamPlayers by remember { mutableStateOf<List<Player>>(emptyList()) }

    // Player Profile State
    var selectedPlayerProfile by remember { mutableStateOf<PlayerProfileData?>(null) }

    // Handle back press to close player profile if open
    BackHandler(enabled = selectedPlayerProfile != null) {
        selectedPlayerProfile = null
    }

    // Load match details when screen loads
    LaunchedEffect(matchId) {
        viewModel.loadMatchDetails(matchId)
    }

    // Update UI when API data arrives
    LaunchedEffect(matchDetailsState) {
        when (val state = matchDetailsState) {
            is MatchDetailsState.Success -> {
                val matchData = state.matchData

                // Map API data to MatchInfo format for existing UI
                matchInfoState.value = MatchInfo(
                    team1Short = matchData.homeTeam.shortName,
                    team1Full = matchData.homeTeam.name,
                    team2Short = matchData.awayTeam.shortName,
                    team2Full = matchData.awayTeam.name,
                    date = matchData.startDateTime,
                    venue = "${matchData.venue.name}, ${matchData.venue.city}",
                    toss = matchData.tossResult,
                    umpire = "",
                    tvUmpire = "",
                    matchReferee = ""
                )

                // Map first innings data
                val firstInning = matchData.getFirstInning()
                if (firstInning != null) {
                    innings1 = firstInning
                }

                // Map second innings data
                val secondInning = matchData.getSecondInning()
                if (secondInning != null) {
                    innings2 = secondInning
                }

                // Map third innings data (for Test Matches)
                val thirdInning = matchData.getThirdInning()
                if (thirdInning != null) {
                    innings3 = thirdInning
                }

                // Map fourth innings data (for Test Matches)
                val fourthInning = matchData.getFourthInning()
                if (fourthInning != null) {
                    innings4 = fourthInning
                }

                // Extract squads using the Players list from API
                homeTeamPlayers = matchData.players
                    .filter { it.teamId == matchData.homeTeam.id }
                    .map {
                        Player(
                            it.id,
                            it.displayName,
                            it.imageUrl ?: "",
                            it.type ?: "",
                            it.teamId,
                            it.battingStyle ?: it.battingHand ?: "N/A",
                            it.bowlingStyle ?: it.bowlingType ?: "N/A"
                        )
                    }

                awayTeamPlayers = matchData.players
                    .filter { it.teamId == matchData.awayTeam.id }
                    .map {
                        Player(
                            it.id,
                            it.displayName,
                            it.imageUrl ?: "",
                            it.type ?: "",
                            it.teamId,
                            it.battingStyle ?: it.battingHand ?: "N/A",
                            it.bowlingStyle ?: it.bowlingType ?: "N/A"
                        )
                    }

                // Set initial "current" over as authorized if it's the first load
                if (matchData.isLive) {
                    if (unlockedUntilOver == -1) {
                        val currentOverVal = matchData.innings.lastOrNull()?.oversBowled?.toDoubleOrNull()?.toInt() ?: 0
                        // Automatically unlock current over + next 2 overs
                        unlockedUntilOver = currentOverVal + 3
                        RewardPrefs.setOversUnlockedUntil(context, matchId, unlockedUntilOver)
                    }

                    if (!RewardPrefs.isMatchDetailsPolicyAccepted(context)) {
                        showMatchDetailsPolicyDialog = true
                    }
                }
            }

            is MatchDetailsState.Loading -> {}
            is MatchDetailsState.Error -> {}
        }
    }

    // ✅ Automatic Reward Ad Trigger Logic
    // Only triggers when the match is live and the current over exceeds the unlocked limit
    LaunchedEffect(matchDetailsState) {
        val state = matchDetailsState
        if (state is MatchDetailsState.Success) {
            val matchData = state.matchData
            if (matchData.isLive) {
                val currentOverInt = matchData.innings.lastOrNull()?.oversBowled?.toDoubleOrNull()?.toInt() ?: 0
                
                // USER REQUEST: Don't show ad immediately on resume/first load
                if (isFirstMatchLoad) {
                    if (currentOverInt > unlockedUntilOver) {
                        // User came back and it's already past the limit -> Give 2 free overs from here
                        unlockedUntilOver = currentOverInt + 2
                        RewardPrefs.setOversUnlockedUntil(context, matchId, unlockedUntilOver)
                    }
                    isFirstMatchLoad = false
                    return@LaunchedEffect
                }

                // If the user has progressed beyond the unlocked limit, show ad automatically
                if (currentOverInt > unlockedUntilOver) {
                    val rewardConfig = adsState.reward_live
                    if (rewardConfig.enabled && rewardConfig.ads_unit.isNotEmpty() && activity != null) {
                        if (RewardPrefs.isPolicyAccepted(context)) {
                            AdRewardManager.showAd(
                                activity = activity,
                                adUnitId = rewardConfig.ads_unit,
                                onRewardEarned = {
                                    unlockedUntilOver = currentOverInt + 2 // Unlock current and next 2 overs
                                    RewardPrefs.setOversUnlockedUntil(context, matchId, unlockedUntilOver)
                                }
                            )
                        } else {
                            rewardPendingOver = currentOverInt
                            showRewardPolicyDialog = true
                        }
                    }
                }
            }
        }
    }

    // Get real commentary data from API
    val commentsData = when (commentsState) {
        is CommentsState.Success -> (commentsState as CommentsState.Success).commentsData
        else -> CommentsData()
    }

    val radialGradient = Brush.radialGradient(
        colors = listOf(RadialTeal.copy(alpha = 0.5f), Color.Transparent),
        center = Offset(540f, -50f),
        radius = 1000f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Background Radial Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(radialGradient)
        )

        // Handle loading and error states
        when (matchDetailsState) {
            is MatchDetailsState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00FF00))
                }
            }

            is MatchDetailsState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${(matchDetailsState as MatchDetailsState.Error).message}",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.retry(matchId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00))
                        ) {
                            Text("Retry", color = Color.Black)
                        }
                    }
                }
            }

            is MatchDetailsState.Success -> {
                val matchData = (matchDetailsState as MatchDetailsState.Success).matchData

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.loadMatchDetails(matchId, isRefresh = true) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .pointerInput(Unit) {
                                var totalDrag = 0f
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { _, dragAmount ->
                                        totalDrag += dragAmount
                                    },
                                    onDragEnd = {
                                        if (totalDrag > 200) {
                                            if (selectedTabIndex > 0) selectedTabIndex--
                                        } else if (totalDrag < -200) {
                                            if (selectedTabIndex < tabs.size - 1) selectedTabIndex++
                                        }
                                        totalDrag = 0f
                                    }
                                )
                            },
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        // Header Section
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = matchData.matchName,
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontFamily = GilroyFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Live from ${matchData.venue.name}",
                                        color = TextGray,
                                        fontSize = 13.sp,
                                        fontFamily = GilroyFontFamily
                                    )
                                }

                                IconButton(
                                    onClick = onBackClick,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(end = 16.dp)
                                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                        .size(36.dp)
                                ) {
                                    Text("✕", color = Color.White, fontSize = 16.sp)
                                }
                            }
                        }

                        // Scorecard Detail Card
                        item {
                            DetailScoreCard(matchData, scoreAnimation)
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }

                        // Dynamic Player Performance Card
                        item {
                            PlayerPerformanceCard(matchData, onPlayerClick = { id ->
                                matchData.players.find { it.id == id }?.let { p ->
                                    selectedPlayerProfile = PlayerProfileData(
                                        id = p.id,
                                        name = p.displayName,
                                        firstName = p.firstName,
                                        lastName = p.lastName,
                                        middleName = p.middleName,
                                        role = p.type ?: "Player",
                                        imageUrl = p.imageUrl ?: "",
                                        battingStyle = p.battingStyle ?: p.battingHand ?: "N/A",
                                        bowlingStyle = p.bowlingStyle ?: p.bowlingType ?: "N/A",
                                        dob = p.dob,
                                        birthPlace = p.birthPlace,
                                        height = p.height,
                                        bio = p.bio,
                                        isCaptain = p.isCaptain,
                                        isWicketKeeper = p.isWicketKeeper
                                    )
                                }
                            })
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }

                        // 1. Match Details Banner Ad
                        if (adsState.match_details_banner.enabled && adsState.match_details_banner.ads_unit.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    BannerAdView(adUnitId = adsState.match_details_banner.ads_unit)
                                }
                            }
                        }

                        // Recent Over Stats
                        item {
                            RecentOverStats(commentsData.recentOvers)
                        }

                        // Tabs below the over screen
                        item {
                            DetailTabs(tabs, selectedTabIndex) { selectedTabIndex = it }
                        }

                        // Conditional Content based on selected tab with animation
                        item {
                            var isTabRendered by remember { mutableStateOf(false) }
                            LaunchedEffect(selectedTabIndex) {
                                isTabRendered = false
                                kotlinx.coroutines.delay(50) // Tiny delay to let the UI breathe
                                isTabRendered = true
                            }

                            if (!isTabRendered) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF00BFA5))
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    when (selectedTabIndex) {
                                        0 -> { // Summary
                                            SquadView(
                                                "${matchData.homeTeam.name} Squad",
                                                homeTeamPlayers,
                                                onPlayerClick = { id ->
                                                    matchData.players.find { it.id == id }
                                                        ?.let { p ->
                                                            selectedPlayerProfile =
                                                                PlayerProfileData(
                                                                    id = p.id,
                                                                    name = p.displayName,
                                                                    firstName = p.firstName,
                                                                    lastName = p.lastName,
                                                                    middleName = p.middleName,
                                                                    role = p.type ?: "Player",
                                                                    imageUrl = p.imageUrl ?: "",
                                                                    battingStyle = p.battingStyle
                                                                        ?: p.battingHand
                                                                        ?: "N/A",
                                                                    bowlingStyle = p.bowlingStyle
                                                                        ?: p.bowlingType
                                                                        ?: "N/A",
                                                                    dob = p.dob,
                                                                    birthPlace = p.birthPlace,
                                                                    height = p.height,
                                                                    bio = p.bio,
                                                                    isCaptain = p.isCaptain,
                                                                    isWicketKeeper = p.isWicketKeeper
                                                                )
                                                        }
                                                }
                                            )

                                            // 6. Teams Banner Ad - Between two teams
                                            if (adsState.teams_banner.enabled && adsState.teams_banner.ads_unit.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 16.dp)
                                                ) {
                                                    BannerAdView(adUnitId = adsState.teams_banner.ads_unit)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(24.dp))
                                            SquadView(
                                                "${matchData.awayTeam.name} Squad",
                                                awayTeamPlayers,
                                                onPlayerClick = { id ->
                                                    matchData.players.find { it.id == id }
                                                        ?.let { p ->
                                                            selectedPlayerProfile =
                                                                PlayerProfileData(
                                                                    id = p.id,
                                                                    name = p.displayName,
                                                                    firstName = p.firstName,
                                                                    lastName = p.lastName,
                                                                    middleName = p.middleName,
                                                                    role = p.type ?: "Player",
                                                                    imageUrl = p.imageUrl ?: "",
                                                                    battingStyle = p.battingStyle
                                                                        ?: p.battingHand
                                                                        ?: "N/A",
                                                                    bowlingStyle = p.bowlingStyle
                                                                        ?: p.bowlingType
                                                                        ?: "N/A",
                                                                    dob = p.dob,
                                                                    birthPlace = p.birthPlace,
                                                                    height = p.height,
                                                                    bio = p.bio,
                                                                    isCaptain = p.isCaptain,
                                                                    isWicketKeeper = p.isWicketKeeper
                                                                )
                                                        }
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(24.dp))
                                            MatchInfoTable(matchInfoState.value)
                                            Spacer(modifier = Modifier.height(24.dp))
                                        }

                                        1 -> { // Scorecard
                                            val fullInningsList =
                                                mutableListOf(innings1, innings2)
                                            // Always add extra innings if they are available, regardless of gameType check
                                            innings3?.let { fullInningsList.add(it) }
                                            innings4?.let { fullInningsList.add(it) }

                                            ScorecardScreen(
                                                inningsList = fullInningsList,
                                                onPlayerClick = { player ->
                                                    selectedPlayerProfile = player
                                                },
                                                adsState = adsState,
                                                gameType = matchData.gameType
                                            )
                                        }

                                        2 -> { // Commentary
                                            when (commentsState) {
                                                is CommentsState.Loading -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(32.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        CircularProgressIndicator(
                                                            color = Color(
                                                                0xFF00BFA5
                                                            )
                                                        )
                                                    }
                                                }

                                                is CommentsState.Error -> {
                                                    Text(
                                                        text = "Failed to load commentary",
                                                        color = TextGray,
                                                        fontSize = 14.sp,
                                                        fontFamily = GilroyFontFamily,
                                                        modifier = Modifier.padding(24.dp)
                                                    )
                                                }

                                                is CommentsState.Success -> {
                                                    // Innings Selection
                                                    if (commentsData.totalInnings > 0) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(
                                                                    horizontal = 16.dp,
                                                                    vertical = 16.dp
                                                                ),
                                                            horizontalArrangement = Arrangement.Center,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            for (i in 1..commentsData.totalInnings) {
                                                                val isSelected =
                                                                    commentsData.currentInning == i
                                                                val inningData =
                                                                    matchData.innings.getOrNull(
                                                                        i - 1
                                                                    )
                                                                val teamShortName =
                                                                    if (inningData != null) {
                                                                        matchData.getBattingTeam(
                                                                            inningData
                                                                        )?.shortName ?: "T$i"
                                                                    } else "T$i"

                                                                val displayLabel =
                                                                    if (matchData.gameType.uppercase()
                                                                            .contains("TEST") || commentsData.totalInnings > 2
                                                                    ) {
                                                                        "$teamShortName (${(i + 1) / 2})"
                                                                    } else {
                                                                        teamShortName
                                                                    }

                                                                val currentMatchId = matchId
                                                                Card(
                                                                    modifier = Modifier
                                                                        .padding(horizontal = 2.dp)
                                                                        .weight(1f)
                                                                        .clickable {
                                                                            viewModel.loadComments(
                                                                                currentMatchId,
                                                                                i
                                                                            )
                                                                        },
                                                                    colors = CardDefaults.cardColors(
                                                                        containerColor = if (isSelected) Color(
                                                                            0xFF00BFA5
                                                                        ) else CardBackground
                                                                    ),
                                                                    shape = RoundedCornerShape(
                                                                        12.dp
                                                                    )
                                                                ) {
                                                                    Column(
                                                                        modifier = Modifier
                                                                            .padding(
                                                                                vertical = 8.dp
                                                                            )
                                                                            .fillMaxWidth(),
                                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                                        verticalArrangement = Arrangement.Center
                                                                    ) {
                                                                        Text(
                                                                            text = displayLabel,
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

                                                    if (commentsData.commentary.isEmpty()) {
                                                        Text(
                                                            text = "No commentary available yet",
                                                            color = TextGray,
                                                            fontSize = 14.sp,
                                                            fontFamily = GilroyFontFamily,
                                                            modifier = Modifier.padding(24.dp)
                                                        )
                                                    } else {
                                                        val commentaryList =
                                                            commentsData.commentary
                                                        val isPaginating by viewModel.isPaginatingComments.collectAsState()

                                                        commentaryList.forEachIndexed { index, entry ->
                                                            // Trigger pagination as user scrolls down
                                                            if (index == commentaryList.size - 2) {
                                                                LaunchedEffect(index) {
                                                                    viewModel.loadMoreComments()
                                                                }
                                                            }

                                                            when (entry) {
                                                                is CommentaryEntry.BallCommentary -> {
                                                                    CommentaryItemFromApi(entry, matchData.players)
                                                                }

                                                                is CommentaryEntry.OverScorecard -> {
                                                                    // REWARDED ADS LOGIC FOR OVERS
                                                                    val isLocked =
                                                                        matchData.isLive && entry.overNumber > unlockedUntilOver

                                                                    // 3. Overs Banner Ad - Before every 2 overs
                                                                    if (entry.overNumber % 2 == 0 && adsState.overs_banner.enabled && adsState.overs_banner.ads_unit.isNotEmpty()) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .fillMaxWidth()
                                                                                .padding(
                                                                                    vertical = 16.dp
                                                                                )
                                                                        ) {
                                                                            BannerAdView(
                                                                                adUnitId = adsState.overs_banner.ads_unit
                                                                            )
                                                                        }
                                                                    }

                                                                    if (isLocked) {
                                                                        LockedOverItem(
                                                                            overNumber = entry.overNumber,
                                                                            onUnlockClick = {
                                                                                val rewardConfig =
                                                                                    adsState.reward_live
                                                                                if (rewardConfig.enabled && rewardConfig.ads_unit.isNotEmpty()) {
                                                                                    if (!RewardPrefs.isPolicyAccepted(
                                                                                            context
                                                                                        )
                                                                                    ) {
                                                                                        rewardPendingOver =
                                                                                            entry.overNumber
                                                                                        showRewardPolicyDialog =
                                                                                            true
                                                                                    } else {
                                                                                        if (activity != null) {
                                                                                            AdRewardManager.showAd(
                                                                                                activity = activity,
                                                                                                adUnitId = rewardConfig.ads_unit,
                                                                                                onRewardEarned = {
                                                                                                    unlockedUntilOver =
                                                                                                        entry.overNumber + 2 // Unlock current and next 2 overs
                                                                                                    RewardPrefs.setOversUnlockedUntil(
                                                                                                        context,
                                                                                                        matchId,
                                                                                                        unlockedUntilOver
                                                                                                    )
                                                                                                }
                                                                                            )
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    unlockedUntilOver =
                                                                                        entry.overNumber + 2
                                                                                    RewardPrefs.setOversUnlockedUntil(
                                                                                        context,
                                                                                        matchId,
                                                                                        unlockedUntilOver
                                                                                    )
                                                                                }
                                                                            }
                                                                        )
                                                                    } else {
                                                                        OverScorecardItem(entry)
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        // Loading indicator when fetching older overs
                                                        if (isPaginating) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(16.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                CircularProgressIndicator(
                                                                    color = Color(0xFF00BFA5),
                                                                    modifier = Modifier.size(32.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


// Show Player Profile as an overlay
    selectedPlayerProfile?.let { player ->
        Box(modifier = Modifier.fillMaxSize()) {
            PlayerProfileScreen(
                player = player,
                onBack = { selectedPlayerProfile = null }
            )
        }
    }

// General Reward Policy Dialog (for Ads)
    if (showRewardPolicyDialog) {
        RewardPolicyDialog(
            onAccept = {
                RewardPrefs.setPolicyAccepted(context, true)
                showRewardPolicyDialog = false
                val rewardConfig =
                    if (rewardPendingOver != null) adsState.reward_live else adsState.reward_ai
                if (activity != null && rewardConfig.enabled && rewardConfig.ads_unit.isNotEmpty()) {
                    AdRewardManager.showAd(
                        activity = activity,
                        adUnitId = rewardConfig.ads_unit,
                        onRewardEarned = {
                            if (rewardPendingOver != null) {
                                unlockedUntilOver = rewardPendingOver!! + 2
                                RewardPrefs.setOversUnlockedUntil(
                                    context,
                                    matchId,
                                    unlockedUntilOver
                                )
                                rewardPendingOver = null
                            }
                        }
                    )
                }
            },
            onDismiss = {
                showRewardPolicyDialog = false
                rewardPendingOver = null
            }
        )
    }

// Match Details Specific Policy Dialog
    if (showMatchDetailsPolicyDialog) {
        RewardPolicyDialog(
            onAccept = {
                RewardPrefs.setMatchDetailsPolicyAccepted(context, true)
                showMatchDetailsPolicyDialog = false
            },
            onDismiss = {
                showMatchDetailsPolicyDialog = false
                onBackClick() // User did not accept, go back
            }
        )
    }
}

@Composable
fun LockedOverItem(overNumber: Int, onUnlockClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onUnlockClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔒", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Over $overNumber is locked",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Watch a short ad to unlock next 2 overs",
                color = TextGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onUnlockClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    "Unlock Now",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun DetailScoreCard(matchData: MatchDetailsData, scoreAnimation: ScoreAnimation) {
    val homeInning = matchData.innings.findLast { it.battingTeamId == matchData.homeTeam.id }
    val awayInning = matchData.innings.findLast { it.battingTeamId == matchData.awayTeam.id }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF2C2C2E), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(CardBackground, Color.Black.copy(alpha = 0.5f))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.padding(
                        top = 20.dp,
                        bottom = 44.dp,
                        start = 20.dp,
                        end = 20.dp
                    )
                ) {
                    Text(
                        matchData.competition.name,
                        color = TextGray,
                        fontSize = 13.sp,
                        fontFamily = GilroyFontFamily,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    ScoreRowDetail(
                        matchData.homeTeam.name,
                        if (homeInning != null) "${homeInning.total}/${homeInning.wickets}" else "0/0",
                        if (homeInning != null) "(${homeInning.oversBowled})" else "(0)",
                        matchData.homeTeam.logoUrl
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ScoreRowDetail(
                        matchData.awayTeam.name,
                        if (awayInning != null) "${awayInning.total}/${awayInning.wickets}" else "0/0",
                        if (awayInning != null) "(${awayInning.oversBowled})" else "(0)",
                        matchData.awayTeam.logoUrl
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Second Horizontal Divider before CRR
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.1f),
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val crr = matchData.innings.lastOrNull()?.currentRunRate ?: 0.0
                        Text(
                            "CRR : $crr",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontFamily = GilroyFontFamily
                        )

                        val rrr = matchData.innings.lastOrNull()?.requiredRunRate
                        if (rrr != null) {
                            Text(
                                "RRR : $rrr",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                fontFamily = GilroyFontFamily
                            )
                        }
                    }
                }
                // Status Badge inside card - aligned like Home Screen
                val statusText: String = if (matchData.isLive) {
                    val inning1 = matchData.innings.getOrNull(0)
                    val inning2 = matchData.innings.getOrNull(1)
                    if (inning2 != null && !matchData.gameType.uppercase().contains("TEST")) {
                        val target = inning1?.total?.plus(1) ?: 0
                        val needs = target - inning2.total
                        val totalBalls = if (matchData.gameType == "ODI") 300 else 120
                        val usedBalls = inning2.oversBowled.toDoubleOrNull()
                            ?.let { (it.toInt() * 6) + ((it % 1) * 10).toInt() } ?: 0
                        val ballsRem = totalBalls - usedBalls
                        if (needs > 0) {
                            val battingTeam = matchData.getBattingTeam(inning2)?.shortName ?: "Team"
                            "$battingTeam need $needs runs in $ballsRem balls"
                        } else {
                            matchData.resultText ?: "Match in progress"
                        }
                    } else if (inning1 != null && inning2 != null) {
                        val diff = inning1.total - inning2.total
                        if (diff > 0) "Lead by $diff runs" else "Trail by ${-diff} runs"
                    } else {
                        matchData.resultText ?: "Live"
                    }
                } else {
                    matchData.resultText ?: ""
                }

                if (statusText.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        color = Color(0xFF2C2C2E),
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    ) {
                        Text(
                            statusText,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontFamily = GilroyFontFamily,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreRowDetail(name: String, score: String, overs: String, logoUrl: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = logoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_launcher_foreground)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = name,
                color = Color.White,
                fontSize = 17.sp,
                fontFamily = GilroyFontFamily,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = score,
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = GilroyFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (overs == "0.0" || overs.isEmpty()) "(0)" else overs,
                color = TextGray,
                fontFamily = GilroyFontFamily,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

@Composable
fun PlayerPerformanceCard(matchData: MatchDetailsData, onPlayerClick: (Int) -> Unit) {
    val currentInning = matchData.innings.lastOrNull()

    // Get currently batting players
    val battingPlayers = currentInning?.batsmen?.filter { it.isBatting } ?: emptyList()
    val batsman1 = battingPlayers.getOrNull(0)
    val batsman2 = battingPlayers.getOrNull(1)

    // Correct active bowler: from the latest ball bowled in recent overs
    val activeBowlerId = currentInning?.recentOvers?.lastOrNull()?.balls?.lastOrNull()?.bowlerId
    val bowler = currentInning?.bowlers?.find { it.playerId == activeBowlerId }
        ?: currentInning?.bowlers?.lastOrNull()

    val playersMap = matchData.players.associateBy { it.id }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF2C2C2E), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CardBackground,
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 48.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val b1 = batsman1?.let { playersMap[it.playerId] }
                    PlayerInfo(
                        name = b1?.displayName
                            ?: b1?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }
                                .takeIf { it?.isNotEmpty() == true }
                            ?: batsman1?.fullName?.split(" ")?.lastOrNull()
                            ?: "Batsman",
                        score = "${batsman1?.runs ?: 0}",
                        detail = "(${batsman1?.balls ?: 0})",
                        isOnStrike = batsman1?.isOnStrike ?: false,
                        imageUrl = b1?.imageUrl ?: "",
                        role = b1?.type ?: "",
                        teamLogoUrl = if (b1?.teamId == matchData.homeTeam.id) matchData.homeTeam.logoUrl else matchData.awayTeam.logoUrl,
                        onPlayerClick = { b1?.let { onPlayerClick(it.id) } }
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(50.dp)
                            .background(Color.White.copy(0.05f))
                    )

                    val b2 = batsman2?.let { playersMap[it.playerId] }
                    PlayerInfo(
                        name = b2?.displayName
                            ?: b2?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }
                                .takeIf { it?.isNotEmpty() == true }
                            ?: batsman2?.fullName?.split(" ")?.lastOrNull()
                            ?: "Batsman",
                        score = "${batsman2?.runs ?: 0}",
                        detail = "(${batsman2?.balls ?: 0})",
                        isOnStrike = batsman2?.isOnStrike ?: false,
                        imageUrl = b2?.imageUrl ?: "",
                        role = b2?.type ?: "",
                        teamLogoUrl = if (b2?.teamId == matchData.homeTeam.id) matchData.homeTeam.logoUrl else matchData.awayTeam.logoUrl,
                        onPlayerClick = { b2?.let { onPlayerClick(it.id) } }
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(50.dp)
                            .background(Color.White.copy(0.05f))
                    )

                    val bw = bowler?.let { playersMap[it.playerId] }
                    PlayerInfo(
                        name = bw?.displayName
                            ?: bw?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }
                                .takeIf { it?.isNotEmpty() == true }
                            ?: bowler?.fullName?.split(" ")?.lastOrNull()
                            ?: "Bowler",
                        score = "${bowler?.wickets ?: 0}-${bowler?.runs ?: 0}",
                        detail = "(${bowler?.overs ?: "0"})",
                        isOnStrike = false,
                        imageUrl = bw?.imageUrl ?: "",
                        role = bw?.type ?: "",
                        teamLogoUrl = if (bw?.teamId == matchData.homeTeam.id) matchData.homeTeam.logoUrl else matchData.awayTeam.logoUrl,
                        onPlayerClick = { bw?.let { onPlayerClick(it.id) } }
                    )
                }

                // Partnership & Last Wicket badge inside the card - aligned like Home Screen
                val partnership = currentInning?.partnership
                val lastWkt = currentInning?.fallOfWickets?.lastOrNull()
                val badgeText =
                    "P'ship : ${partnership?.totalRuns ?: 0}(${partnership?.totalBalls ?: 0}) | Last Wkt : ${lastWkt?.playerName ?: "N/A"} ${lastWkt?.runsAtFall ?: 0}(${lastWkt?.over ?: ""})"

                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = Color(0xFF2C2C2E),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                ) {
                    Text(
                        badgeText,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontFamily = GilroyFontFamily,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerInfo(
    name: String,
    score: String,
    detail: String,
    isOnStrike: Boolean,
    imageUrl: String,
    role: String = "",
    teamLogoUrl: String = "",
    onPlayerClick: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clickable { onPlayerClick() }
    ) {
        Box {
            Box(modifier = Modifier.size(65.dp)) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    error = painterResource(
                        id = if (role.contains(
                                "Bowl",
                                ignoreCase = true
                            )
                        ) R.drawable.bowler else R.drawable.batsman
                    )
                )
                // Scorecard-style vertical gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.6f to Color.Black.copy(alpha = 0.1f),
                                1.0f to Color.Black.copy(alpha = 0.5f)
                            )
                        )
                )
            }

            // Team Flag / Nationality Overlay
            if (teamLogoUrl.isNotEmpty()) {
                AsyncImage(
                    model = teamLogoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp),
                    contentScale = ContentScale.Crop
                )
            }
            if (isOnStrike) {
                Text(
                    text = "🏏",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-2).dp),
                    fontSize = 16.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = GilroyFontFamily,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
            Text(
                score,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = GilroyFontFamily
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                detail,
                color = TextGray,
                fontSize = 10.sp,
                fontFamily = GilroyFontFamily,
                modifier = Modifier.padding(bottom = 1.dp)
            )
        }
    }
}

@Composable
fun RecentOverStats(recentOvers: List<OverSummary>) {
    val scrollState = rememberScrollState()

    // Auto-scroll to the end (latest overs on the right)
    LaunchedEffect(recentOvers) {
        if (recentOvers.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = "Recent Balls",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontFamily = GilroyFontFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (recentOvers.isEmpty()) {
                Text("--", color = TextGray, fontSize = 12.sp, fontFamily = GilroyFontFamily, modifier = Modifier.padding(start = 4.dp))
            } else {
                recentOvers.forEachIndexed { index, over ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(Color.White.copy(0.05f), RoundedCornerShape(30.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = "Ov ${over.overNumber}",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontFamily = GilroyFontFamily
                            )
                            Text(
                                text = "${over.totalRuns}-${over.totalWickets}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GilroyFontFamily
                            )
                        }
                        over.balls.forEach { ball ->
                            BallStat(ball.displayText, ball.displayColor)
                        }
                    }
                    
                    // Divider between overs if not the last one
                    if (index < recentOvers.size - 1) {
                         Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(1.5.dp)
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BallStat(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DetailTabs(tabs: List<String>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = title,
                    color = if (isSelected) Color(0xFF00BFA5) else Color.White,
                    fontSize = 16.sp,
                    fontFamily = GilroyFontFamily,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(2.5.dp)
                            .background(Color(0xFF00BFA5), RoundedCornerShape(2.dp))
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.5.dp))
                }
            }
        }
    }
}

@Composable
fun FieldPlayerItem(player: Player, onPlayerClick: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clickable { onPlayerClick(player.id) }
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Box {
                AsyncImage(
                    model = player.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(65.dp),
                    contentScale = ContentScale.Fit,
                    error = painterResource(
                        id = if (player.role.contains(
                                "Bowl",
                                ignoreCase = true
                            )
                        ) R.drawable.bowler else R.drawable.batsman
                    )
                )
            }
            Surface(
                color = Color.Black,
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.offset(y = 2.dp)
            ) {
                Text(
                    text = player.name,
                    color = Color.White,
                    fontSize = 8.sp,
                    fontFamily = GilroyFontFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun SquadView(teamName: String, players: List<Player>, onPlayerClick: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dark title box attached to top of field
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            modifier = Modifier.width(240.dp) // Narrower title box like in screenshot
        ) {
            Text(
                text = teamName,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GilroyFontFamily,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        // Field Background
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.team_bg),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp), // More vertical padding for spacing
                verticalArrangement = Arrangement.spacedBy(20.dp), // Increased gap between rows
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (players.isEmpty()) {
                    Text(
                        "No players available",
                        color = Color.White.copy(0.5f),
                        modifier = Modifier.padding(40.dp)
                    )
                } else {
                    // formation: 3, 3, 3, 2 (Dream11 style)
                    val rows = listOf(3, 3, 3, 2)
                    var index = 0
                    rows.forEach { count ->
                        val rowPlayers = players.drop(index).take(count)
                        if (rowPlayers.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowPlayers.forEach { FieldPlayerItem(it, onPlayerClick) }
                            }
                        }
                        index += count
                    }
                }
            }
        }
    }
}

@Composable
fun MatchInfoTable(info: MatchInfo?) {
    if (info == null) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Match Info",
            color = Color.White,
            fontSize = 18.sp,
            fontFamily = GilroyFontFamily,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        InfoRow("Match", "${info.team1Short} vs ${info.team2Short}, ${info.date}")
        InfoRow("Series", "International Tour")
        InfoRow("Date", info.date)
        InfoRow("Toss", info.toss ?: "TBA")
        InfoRow("Venue", info.venue)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TextGray,
            fontSize = 13.sp,
            fontFamily = GilroyFontFamily,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontFamily = GilroyFontFamily,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CommentaryItemFromApi(ball: CommentaryEntry.BallCommentary, players: List<PlayerDetail> = emptyList()) {
    var isExpanded by remember { mutableStateOf(false) }

    val batter = players.find { it.id == ball.batterId }
    val isLeftHanded = batter?.battingHand?.contains("Left", ignoreCase = true) == true ||
                       batter?.battingStyle?.contains("Left", ignoreCase = true) == true

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(enabled = ball.shotAngle != null) {
                isExpanded = !isExpanded
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ball.color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ball.ballResult,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ball.over,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = GilroyFontFamily
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ball.title,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        fontFamily = GilroyFontFamily
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ball.description,
                    color = TextGray,
                    fontSize = 13.sp,
                    fontFamily = GilroyFontFamily,
                    lineHeight = 18.sp
                )

                val validBadges = listOfNotNull(
                    ball.length?.takeIf { it != "Null" && it.isNotEmpty() }
                        ?.let { "Length" to it.formatIdentifier() },
                    ball.line?.takeIf { it != "Null" && it.isNotEmpty() }
                        ?.let { "Line" to it.formatIdentifier() },
                    ball.shotType?.takeIf { it != "Null" && it.isNotEmpty() }
                        ?.let { "Shot" to it.formatIdentifier() },
                    ball.fieldingPosition?.takeIf { it != "Null" && it.isNotEmpty() }
                        ?.let { "To" to it.formatIdentifier() }
                )

                if (validBadges.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        validBadges.forEach { (label, value) ->
                            BadgeItem(label, value)
                        }
                    }
                }

                if (ball.shotAngle != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isExpanded) "👇 Hide Shot View" else "👉 Shot View (360°)",
                        color = Color(0xFF00BFA5),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GilroyFontFamily
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    if (ball.shotAngle != null) {
                        ShotViewCanvas(
                            angle = ball.shotAngle,
                            magnitude = ball.shotMagnitude ?: 100,
                            runs = ball.runs,
                            isWicket = ball.isWicket,
                            dismissalType = ball.dismissalType,
                            fieldingPosition = ball.fieldingPosition,
                            isAirControlled = ball.isAirControlled,
                            isAirNotControlled = ball.isAirNotControlled,
                            isWide = ball.isWide,
                            isNoBall = ball.isNoBall,
                            isByes = ball.isByes,
                            isLegByes = ball.isLegByes,
                            battingConnectionId = ball.battingConnectionId,
                            battingFeetId = ball.battingFeetId,
                            catchDifficulty = ball.catchDifficulty,
                            isLeftHanded = isLeftHanded
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShotViewCanvas(
    angle: Int,
    magnitude: Int,
    runs: Int,
    isWicket: Boolean,
    dismissalType: String? = null,
    fieldingPosition: String? = null,
    isAirControlled: Boolean = false,
    isAirNotControlled: Boolean = false,
    isWide: Boolean = false,
    isNoBall: Boolean = false,
    isByes: Boolean = false,
    isLegByes: Boolean = false,
    battingConnectionId: String? = null,
    battingFeetId: String? = null,
    catchDifficulty: Int? = null,
    isLeftHanded: Boolean = false
) {
    val progress = remember { Animatable(0f) }
    val pulse = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
        if (runs >= 4 || isWicket) {
            pulse.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
            pulse.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
        }
    }

    // Professional field map (Standard RHB)
    val positions = remember {
        listOf(
            Triple("Wicketkeeper", 0f, true),
            Triple("Slip", 25f, true),
            Triple("Gully", 45f, true),
            Triple("Point", 90f, true),
            Triple("Deep Point", 100f, false),
            Triple("Cover", 125f, true),
            Triple("Extra Cover", 145f, true),
            Triple("Deep Cover", 135f, false),
            Triple("Mid Off", 165f, true),
            Triple("Long Off", 175f, false),
            Triple("Long On", 185f, false),
            Triple("Mid On", 195f, true),
            Triple("Midwicket", 235f, true),
            Triple("Deep Midwicket", 225f, false),
            Triple("Square Leg", 270f, true),
            Triple("Deep Square Leg", 280f, false),
            Triple("Fine Leg", 315f, true),
            Triple("Deep Fine Leg", 325f, false),
            Triple("Third Man", 60f, false)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2.3f
            val innerRadius = radius * 0.55f

            // 1. Professional Ground
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color(0xFF2E7D32),
                    1.0f to Color(0xFF1B5E20),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Boundary Flare/Glow
            if (runs >= 4) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f + pulse.value * 0.4f),
                    radius = radius + (pulse.value * 12f),
                    center = center,
                    style = Stroke(6f)
                )
            }
            drawCircle(Color.White.copy(0.7f), radius, center, style = Stroke(4f))

            // 30-Yard Circle
            drawCircle(
                Color.White.copy(0.3f),
                innerRadius,
                center,
                style = Stroke(2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
            )

            // Pitch
            val pitchW = radius * 0.12f
            val pitchH = radius * 0.48f
            drawRoundRect(
                color = Color(0xFFD7B899),
                topLeft = Offset(center.x - pitchW / 2, center.y - pitchH / 2),
                size = Size(pitchW, pitchH),
                cornerRadius = CornerRadius(4f)
            )

            // 2. Fielding Setup (Horizontal Mirror for LHB)
            positions.forEach { (name, baseAngle, isInside) ->
                val mappedAngle = if (isLeftHanded) (360f - baseAngle) % 360f else baseAngle
                val rad = Math.toRadians(mappedAngle.toDouble())

                val dist = if (isInside) innerRadius * 0.85f else radius * 0.92f
                val lx = center.x + (dist * sin(rad).toFloat())
                val ly = center.y - (dist * cos(rad).toFloat())

                val isRelevant = fieldingPosition?.contains(name, ignoreCase = true) == true

                if (isRelevant) {
                    drawCircle(Color(0xFFFFA726), 10f * progress.value, Offset(lx, ly))
                    drawCircle(Color.White.copy(0.3f), 18f * progress.value, Offset(lx, ly))

                    // Label Pill Backdrop
                    val rectW = name.length * 12f + 30f
                    drawRoundRect(
                        color = Color(0xFF00BFA5).copy(alpha = 0.9f),
                        topLeft = Offset(lx - rectW/2, ly - (if (ly > center.y) -20f else 55f)),
                        size = Size(rectW, 36f),
                        cornerRadius = CornerRadius(18f)
                    )
                }

                drawContext.canvas.nativeCanvas.drawText(
                    name.uppercase(),
                    lx,
                    ly + (if (ly > center.y) 46f else -30f),
                    android.graphics.Paint().apply {
                        color = (if (isRelevant) android.graphics.Color.WHITE else 0x55FFFFFF.toInt())
                        textSize = 20f
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.create("Gilroy", android.graphics.Typeface.BOLD)
                    }
                )
            }

            // 3. Trajectory Physics (Snap to Fielder if provided)
            val matchedPos = positions.find { fieldingPosition?.contains(it.first, ignoreCase = true) == true }
            val rawAngle = matchedPos?.second ?: angle.toFloat()
            val visualAngle = if (isLeftHanded) (360f - rawAngle) % 360f else rawAngle
            val shotRad = Math.toRadians(visualAngle.toDouble())

            val baseDist = when {
                runs >= 6 -> radius * 1.35f
                runs >= 4 -> radius * 1.08f
                runs >= 1 -> radius * (0.35f + (runs * 0.15f))
                isWicket && dismissalType == "Caught" -> radius * 0.75f
                else -> radius * 0.3f
            }
            val magFactor = (magnitude / 220f).coerceIn(0.5f, 1.4f)
            val finalDist = (baseDist * magFactor).coerceAtMost(radius * 1.55f)

            if (isWicket && dismissalType == "Bowled") {
                drawCircle(Color.Red.copy(0.4f * progress.value), 40f * progress.value, center)
            } else {
                val tx = center.x + (finalDist * sin(shotRad).toFloat())
                val ty = center.y - (finalDist * cos(shotRad).toFloat())

                val currX = center.x + (tx - center.x) * progress.value
                val currY = center.y - (center.y - ty) * progress.value

                val isAir = isAirNotControlled || isAirControlled || runs >= 4
                val isMisTimed = battingConnectionId == "MisTimed"

                // Shadow
                if (isAir) {
                    drawLine(
                        Color.Black.copy(0.3f * progress.value),
                        center,
                        Offset(currX, currY),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }

                val wobble = if (isMisTimed) (sin(progress.value * 25f) * 6f) else 0f
                val elev = if (isAir) (sin(progress.value * Math.PI.toFloat()) * -45f) else 0f

                val shotColor = when {
                    isWicket -> Color(0xFFFF5252)
                    runs == 6 -> Color(0xFF9C27B0)
                    runs == 4 -> Color(0xFF2196F3)
                    runs > 0 -> Color(0xFF4CAF50)
                    else -> Color.White
                }

                // Glowing Trail
                if (runs >= 4 && progress.value > 0.3f) {
                    val trailScale = (progress.value - 0.3f) * 1.4f
                    drawLine(
                        brush = Brush.linearGradient(listOf(shotColor.copy(0f), shotColor.copy(0.4f))),
                        start = center,
                        end = Offset(center.x + (tx - center.x) * trailScale, center.y + (ty - center.y) * trailScale + elev),
                        strokeWidth = 18f,
                        cap = StrokeCap.Round
                    )
                }

                // Main Line
                drawLine(
                    shotColor,
                    center,
                    Offset(currX + wobble, currY + elev),
                    strokeWidth = if (runs >= 4) 14f else 8f,
                    cap = StrokeCap.Round
                )

                // Ball & Glow
                drawCircle(Color.White, 8f, Offset(currX + wobble, currY + elev))
                drawCircle(shotColor.copy(0.4f), 15f, Offset(currX + wobble, currY + elev))

                // Outcome Banner
                if (progress.value > 0.98f) {
                    val label = when {
                        isWicket -> "OUT!"
                        runs == 6 -> "SIX! 🚀"
                        runs == 4 -> "FOUR! ⚡"
                        runs > 0 -> "$runs RUNS"
                        else -> "DOT"
                    }
                    val rectW = label.length * 16f + 60f
                    val labelYOffset = if (ty > center.y) 65f else -105f

                    drawRoundRect(
                        color = shotColor.copy(0.95f),
                        topLeft = Offset(tx - rectW/2, ty + labelYOffset),
                        size = Size(rectW, 46f),
                        cornerRadius = CornerRadius(23f)
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        label.uppercase(),
                        tx,
                        ty + labelYOffset + 34f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 28f
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun OverScorecardItem(entry: CommentaryEntry.OverScorecard) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "End of Over ${entry.overNumber}",
                    color = Color(0xFF00BFA5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = GilroyFontFamily
                )
                Text(
                    text = "Score: ${entry.totalScore}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = GilroyFontFamily
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color.White.copy(0.1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Runs in Over",
                        color = TextGray,
                        fontSize = 12.sp,
                        fontFamily = GilroyFontFamily
                    )
                    Text(
                        text = "${entry.runsInOver}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GilroyFontFamily
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Wickets",
                        color = TextGray,
                        fontSize = 12.sp,
                        fontFamily = GilroyFontFamily
                    )
                    Text(
                        text = "${entry.wicketsInOver}",
                        color = if (entry.wicketsInOver > 0) Color(0xFFE74C3C) else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GilroyFontFamily
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Run Rate",
                        color = TextGray,
                        fontSize = 12.sp,
                        fontFamily = GilroyFontFamily
                    )
                    Text(
                        text = String.format("%.1f", entry.runRate),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GilroyFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeItem(label: String, value: String) {
    Surface(
        color = Color(0xFF2C2C2E),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontFamily = GilroyFontFamily
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GilroyFontFamily
            )
        }
    }
}

fun String.formatIdentifier(): String {
    return this.replace(Regex("([a-z])([A-Z]+)"), "$1 $2")
}

