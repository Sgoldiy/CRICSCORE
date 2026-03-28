package com.cricpeak.scorelive.ui

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.cricpeak.scorelive.ads.*
import com.cricpeak.scorelive.ui.apicallfiles.CricketViewModel
import com.cricpeak.scorelive.ui.apicallfiles.FixtureContainer
import com.cricpeak.scorelive.ui.apicallfiles.FixturesState
import com.cricpeak.scorelive.ui.components.RewardPolicyDialog
import com.cricpeak.scorelive.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

fun formatMatchDate(dateTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateTime)

        val outputFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()
        date?.let { outputFormat.format(it) } ?: dateTime
    } catch (e: Exception) {
        dateTime
    }
}

sealed class MatchListItemType {
    data class Match(val fixture: FixtureContainer) : MatchListItemType()
    data class Ad(val adConfig: AdConfig) : MatchListItemType()
}

fun getItemsWithAds(
    fixtures: List<FixtureContainer>,
    adConfig: AdConfig?,
    interval: Int = 3,
): List<MatchListItemType> {
    val list = mutableListOf<MatchListItemType>()
    fixtures.forEachIndexed { index, fixture ->
        list.add(MatchListItemType.Match(fixture))
        if ((index + 1) % interval == 0 && adConfig?.enabled == true && adConfig.ads_unit.isNotEmpty()) {
            list.add(MatchListItemType.Ad(adConfig))
        }
    }
    return list
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CricScoreHomeScreen(
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onMatchClick: (Int) -> Unit,
    onPredictionClick: (Int) -> Unit,
    onMenuClick: () -> Unit,
    adsState: AdsState? = null,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val viewModel: CricketViewModel = viewModel()
    val fixturesState by viewModel.fixturesState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val categories = listOf("LIVE", "UPCOMING", "COMPLETED")

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Policy Dialog State
    var showRewardPolicyDialog by remember { mutableStateOf(false) }
    var pendingPredictionMatchId by remember { mutableStateOf<Int?>(null) }

    // Pre-load ads
    LaunchedEffect(adsState) {
        adsState?.let { state ->
            if (state.inter_home.enabled) AdInterstitialManager.HomeScreenAdManager.loadAd(
                context,
                state.inter_home.ads_unit
            )
            if (state.reward_ai.enabled) AdRewardManager.loadAd(context, state.reward_ai.ads_unit)
        }
    }

    // Tab Change Interstitial
    var previousTabIndex by remember { mutableIntStateOf(selectedTabIndex) }
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex != previousTabIndex && activity != null) {
            adsState?.inter_home?.let { config ->
                if (config.enabled) AdInterstitialManager.HomeScreenAdManager.showAdIfAvailable(
                    activity,
                    config.ads_unit
                ) {}
            }
        }
        previousTabIndex = selectedTabIndex
    }

    val radialGradient = Brush.radialGradient(
        colors = listOf(RadialTeal.copy(alpha = 0.5f), Color.Transparent),
        center = androidx.compose.ui.geometry.Offset(540f, -50f),
        radius = 1000f
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(radialGradient)
            )

            when (val state = fixturesState) {
                is FixturesState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = AccentGreen) }

                is FixturesState.Error -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Error: ${state.message}", color = Color.Red)
                        Button(
                            onClick = { viewModel.fetchFixtures() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) { Text(text = "Retry") }
                    }
                }

                is FixturesState.Success -> {
                    val fixtures = state.response
                    val topFiveMatches = remember(fixtures) {
                        (fixtures.inProgressFixtures + fixtures.completedFixtures.take(5) + fixtures.upcomingFixtures.take(
                            5
                        )).take(5)
                    }

                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.fetchFixtures(isManual = true) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding(),
                            contentPadding = PaddingValues(bottom = if (adsState?.sticky_banner_bottom?.enabled == true) 80.dp else 24.dp)
                        ) {
                            item { TopBar(onMenuClick = onMenuClick) }

                            // Featured Section
                            if (topFiveMatches.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Featured Matches",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontFamily = GilroyFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(
                                            start = 20.dp,
                                            top = 16.dp,
                                            bottom = 12.dp
                                        )
                                    )
                                    val totalItems = 7
                                    val pagerState = rememberPagerState(pageCount = { totalItems })
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        HorizontalPager(
                                            state = pagerState,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(210.dp),
                                            contentPadding = PaddingValues(horizontal = 40.dp),
                                            pageSpacing = 16.dp
                                        ) { page ->
                                            when {
                                                page == 3 || page == 6 -> {
                                                    if (adsState?.slider_big_native?.enabled == true) NativeAdViewHorizontal(
                                                        adUnitId = adsState.slider_big_native.ads_unit
                                                    )
                                                    else if (page == 3 && topFiveMatches.size > 3) FeaturedMatchCard(
                                                        fixture = topFiveMatches[3],
                                                        onMatchClick = onMatchClick,
                                                        onPredictionClick = onPredictionClick,
                                                        showPrediction = false
                                                    )
                                                }

                                                else -> {
                                                    val idx = if (page < 3) page else page - 1
                                                    if (idx < topFiveMatches.size) FeaturedMatchCard(
                                                        fixture = topFiveMatches[idx],
                                                        onMatchClick = onMatchClick,
                                                        onPredictionClick = onPredictionClick,
                                                        showPrediction = false
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            repeat(totalItems) { i ->
                                                Box(
                                                    modifier = Modifier
                                                        .padding(4.dp)
                                                        .size(if (pagerState.currentPage == i) 10.dp else 6.dp)
                                                        .background(
                                                            color = if (pagerState.currentPage == i) Color.White else Color.Gray.copy(
                                                                alpha = 0.5f
                                                            ),
                                                            shape = RoundedCornerShape(50)
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                CategoryTabs(
                                    categories = categories,
                                    selectedIndex = selectedTabIndex,
                                    onTabSelected = onTabSelected
                                )
                            }

                            if (selectedTabIndex == 0) { // LIVE
                                if (fixtures.inProgressFixtures.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Live Now",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontFamily = GilroyFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(
                                                start = 20.dp,
                                                top = 8.dp,
                                                bottom = 8.dp
                                            )
                                        )
                                    }
                                    val items = getItemsWithAds(
                                        fixtures.inProgressFixtures,
                                        adsState?.live_banner
                                    )
                                    items(items) { item ->
                                        when (item) {
                                            is MatchListItemType.Match -> MatchListItem(
                                                fixture = item.fixture,
                                                onMatchClick = onMatchClick,
                                                onPredictionClick = { id ->
                                                    handlePredictionClick(
                                                        matchId = id,
                                                        context = context,
                                                        activity = activity,
                                                        adsState = adsState,
                                                        onShowPolicy = { showRewardPolicyDialog = true },
                                                        onSetPending = { pendingPredictionMatchId = it },
                                                        onNavigate = { onPredictionClick(it) },
                                                        onUnlocked = {
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("AI Predictions Unlocked 🎯")
                                                            }
                                                        })
                                                })

                                            is MatchListItemType.Ad -> NativeAdViewHorizontal(
                                                adUnitId = item.adConfig.ads_unit,
                                                modifier = Modifier.padding(
                                                    horizontal = 20.dp,
                                                    vertical = 8.dp
                                                )
                                            )
                                        }
                                    }
                                }
                                item {
                                    Text(
                                        text = "Upcoming Matches",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontFamily = GilroyFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(
                                            start = 20.dp,
                                            top = 8.dp,
                                            bottom = 8.dp
                                        )
                                    )
                                }
                                val upcomingItems = getItemsWithAds(
                                    fixtures.upcomingFixtures.take(5),
                                    adsState?.upcoming_live_banner
                                )
                                items(upcomingItems) { item ->
                                    when (item) {
                                        is MatchListItemType.Match -> MatchListItem(
                                            fixture = item.fixture,
                                            onMatchClick = onMatchClick,
                                            onPredictionClick = { id ->
                                                handlePredictionClick(
                                                    matchId = id,
                                                    context = context,
                                                    activity = activity,
                                                    adsState = adsState,
                                                    onShowPolicy = { showRewardPolicyDialog = true },
                                                    onSetPending = { pendingPredictionMatchId = it },
                                                    onNavigate = { onPredictionClick(it) },
                                                    onUnlocked = {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("AI Predictions Unlocked 🎯")
                                                        }
                                                    })
                                            })

                                        is MatchListItemType.Ad -> NativeAdViewHorizontal(
                                            adUnitId = item.adConfig.ads_unit,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            } else { // UPCOMING/COMPLETED
                                val display =
                                    if (selectedTabIndex == 1) fixtures.upcomingFixtures else fixtures.completedFixtures
                                val adConfig =
                                    if (selectedTabIndex == 1) adsState?.upcoming_banner else adsState?.completed_banner
                                val items = getItemsWithAds(display, adConfig)
                                items(items) { item ->
                                    when (item) {
                                        is MatchListItemType.Match -> MatchListItem(
                                            fixture = item.fixture,
                                            onMatchClick = onMatchClick,
                                            onPredictionClick = { id ->
                                                handlePredictionClick(
                                                    matchId = id,
                                                    context = context,
                                                    activity = activity,
                                                    adsState = adsState,
                                                    onShowPolicy = { showRewardPolicyDialog = true },
                                                    onSetPending = { pendingPredictionMatchId = it },
                                                    onNavigate = { onPredictionClick(it) },
                                                    onUnlocked = {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("AI Predictions Unlocked 🎯")
                                                        }
                                                    })
                                            },
                                            showPrediction = false
                                        )

                                        is MatchListItemType.Ad -> NativeAdViewHorizontal(
                                            adUnitId = item.adConfig.ads_unit,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (adsState?.sticky_banner_bottom?.enabled == true) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(
                            bottom = WindowInsets.navigationBars.asPaddingValues()
                                .calculateBottomPadding()
                        )
                ) {
                    BannerAdView(adUnitId = adsState.sticky_banner_bottom.ads_unit)
                }
            }

            if (showRewardPolicyDialog) {
                RewardPolicyDialog(
                    onAccept = {
                        RewardPrefs.setPolicyAccepted(context, true)
                        showRewardPolicyDialog = false
                        pendingPredictionMatchId?.let { id ->
                            adsState?.reward_ai?.let { config ->
                                if (activity != null) AdRewardManager.showAd(
                                    activity = activity,
                                    adUnitId = config.ads_unit,
                                    onRewardEarned = {
                                        RewardPrefs.addAiUnlocks(context)
                                        onPredictionClick(id)
                                        scope.launch { snackbarHostState.showSnackbar("AI Predictions Unlocked 🎯") }
                                    })
                            }
                        }
                    },
                    onDismiss = { showRewardPolicyDialog = false }
                )
            }
        }
    }
}

private fun handlePredictionClick(
    matchId: Int,
    context: android.content.Context,
    activity: Activity?,
    adsState: AdsState?,
    onShowPolicy: () -> Unit,
    onSetPending: (Int) -> Unit,
    onNavigate: (Int) -> Unit,
    onUnlocked: () -> Unit,
) {
    if (RewardPrefs.getAiUnlockCount(context) > 0) {
        RewardPrefs.useAiUnlock(context)
        onNavigate(matchId)
        return
    }

    if (!RewardPrefs.isPolicyAccepted(context)) {
        onSetPending(matchId)
        onShowPolicy()
        return
    }

    adsState?.reward_ai?.let { config ->
        if (config.enabled && activity != null) {
            AdRewardManager.showAd(activity, config.ads_unit, {
                RewardPrefs.addAiUnlocks(context)
                onNavigate(matchId)
                onUnlocked()
            })
        } else {
            onNavigate(matchId)
        }
    } ?: onNavigate(matchId)
}

@Composable
fun TopBar(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_menu),
            contentDescription = "Menu",
            modifier = Modifier
                .size(24.dp)
                .clickable { onMenuClick() },
            tint = Color.White
        )
        Text(
            text = "CRICPEAK",
            color = Color.White,
            fontSize = 36.sp,
            fontFamily = BayonFont,
            letterSpacing = (36 * 0.07).sp
        )
        Spacer(modifier = Modifier.size(24.dp))
    }
}

@Composable
fun FeaturedMatchCard(
    fixture: FixtureContainer,
    onMatchClick: (Int) -> Unit,
    onPredictionClick: (Int) -> Unit,
    showPrediction: Boolean = true,
) {
    val isUpcoming = !fixture.isLive && !fixture.isInProgress && !fixture.isCompleted
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clickable { onMatchClick(fixture.id) }
            .border(2.dp, Color(0xFF2C2C2E), RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fixture.competition.name,
                        color = TextGray,
                        fontSize = 14.sp,
                        fontFamily = GilroyFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (fixture.isLive || fixture.isInProgress) {
                        Surface(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(30.dp),
                            border = BorderStroke(0.5.dp, AccentGreen)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AccentGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE",
                                    color = AccentGreen,
                                    fontSize = 12.sp,
                                    fontFamily = GilroyFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (isUpcoming) "UPCOMING" else fixture.gameType,
                            color = AccentGreen,
                            fontSize = 12.sp,
                            fontFamily = GilroyFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hIn = fixture.innings?.find { it.inningNumber == 1 }
                    val aIn = fixture.innings?.find { it.inningNumber == 2 }
                    TeamInfoFeatured(
                        name = fixture.homeTeam.shortName,
                        score = if (isUpcoming) "-" else (hIn?.let { "${it.runsScored}/${it.wickets}" }
                            ?: "0/0"),
                        overs = if (isUpcoming) "" else (hIn?.let { "(${it.oversBowled})" } ?: "(0)"),
                        logoUrl = fixture.homeTeam.logoUrl,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .height(80.dp)
                            .width(1.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.2f),
                                        Color.White.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    TeamInfoFeatured(
                        name = fixture.awayTeam.shortName,
                        score = if (isUpcoming) "-" else (aIn?.let { "${it.runsScored}/${it.wickets}" }
                            ?: "0/0"),
                        overs = if (isUpcoming) "" else (aIn?.let { "(${it.oversBowled})" } ?: "(0)"),
                        logoUrl = fixture.awayTeam.logoUrl,
                        modifier = Modifier.weight(1f),
                        isRight = true
                    )
                }
                if (isUpcoming && showPrediction) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onPredictionClick(fixture.id) },
                        modifier = Modifier
                            .height(36.dp)
                            .padding(horizontal = 24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5)),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI Prediction",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GilroyFontFamily
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "✨", fontSize = 12.sp)
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = Color(0xFF2C2C2E),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            ) {
                val footerText = fixture.resultText ?: (if (isUpcoming) "Match starting soon" else "")
                Text(
                    text = footerText,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontFamily = GilroyFontFamily,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun TeamInfoFeatured(
    name: String,
    score: String,
    overs: String,
    logoUrl: String,
    modifier: Modifier = Modifier,
    isRight: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (isRight) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isRight) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isRight) TeamLogo(logoUrl)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = GilroyFontFamily,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    textAlign = if (isRight) TextAlign.End else TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (isRight) TeamLogo(logoUrl)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = score,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = GilroyFontFamily
        )
        if (overs.isNotEmpty()) {
            Text(
                text = overs,
                color = TextGray,
                fontSize = 14.sp,
                fontFamily = GilroyFontFamily,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun TeamLogo(url: String) {
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
        contentScale = ContentScale.Crop,
        error = painterResource(id = R.drawable.ic_launcher_foreground)
    )
}

@Composable
fun CategoryTabs(categories: List<String>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEachIndexed { i, title ->
            val isSelected = selectedIndex == i
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSelected) Color.Black.copy(alpha = 0.4f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (isSelected) AccentGreen else Color.Transparent,
                        RoundedCornerShape(24.dp)
                    )
                    .clickable { onTabSelected(i) }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Thin,
                    fontFamily = GilroyFontFamily,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun MatchListItem(
    fixture: FixtureContainer,
    onMatchClick: (Int) -> Unit,
    onPredictionClick: (Int) -> Unit,
    showPrediction: Boolean = true,
) {
    val isUpcoming = !fixture.isLive && !fixture.isInProgress && !fixture.isCompleted
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onMatchClick(fixture.id) }
            .border(2.dp, Color(0xFF2C2C2E), RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = fixture.competition.name,
                        color = TextGray,
                        fontSize = 14.sp,
                        fontFamily = GilroyFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isUpcoming) Text(
                        text = formatMatchDate(fixture.startDateTime),
                        color = AccentGreen,
                        fontSize = 12.sp,
                        fontFamily = GilroyFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                val hIn = fixture.innings?.find { it.inningNumber == 1 }
                val aIn = fixture.innings?.find { it.inningNumber == 2 }
                TeamRow(
                    name = fixture.homeTeam.shortName,
                    score = if (isUpcoming) "-" else (hIn?.let { "${it.runsScored}/${it.wickets}" }
                        ?: "0/0"),
                    overs = if (isUpcoming) "" else (hIn?.let { "(${it.oversBowled})" } ?: ""),
                    logoUrl = fixture.homeTeam.logoUrl)
                Spacer(modifier = Modifier.height(10.dp))
                TeamRow(
                    name = fixture.awayTeam.shortName,
                    score = if (isUpcoming) "-" else (aIn?.let { "${it.runsScored}/${it.wickets}" }
                        ?: "0/0"),
                    overs = if (isUpcoming) "" else (aIn?.let { "(${it.oversBowled})" } ?: ""),
                    logoUrl = fixture.awayTeam.logoUrl)
                if (isUpcoming && showPrediction) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onPredictionClick(fixture.id) }
                                .border(1.dp, Color(0xFF00BFA5), RoundedCornerShape(14.dp)),
                            color = Color(0xFF00BFA5).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI Prediction",
                                    color = Color(0xFF00BFA5),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = GilroyFontFamily
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "✨", fontSize = 11.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            val footer = fixture.resultText ?: (if (isUpcoming) "Upcoming" else "")
            if (footer.isNotEmpty()) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = Color(0xFF2C2C2E),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ) {
                    Text(
                        text = footer,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontFamily = GilroyFontFamily,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun TeamRow(name: String, score: String, overs: String, logoUrl: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TeamLogo(url = logoUrl)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = GilroyFontFamily,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = score,
                color = Color.White,
                fontSize = 20.sp,
                fontFamily = GilroyFontFamily,
                fontWeight = FontWeight.Bold
            )
            if (overs.isNotEmpty()) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = overs,
                    color = TextGray,
                    fontFamily = GilroyFontFamily,
                    fontSize = 14.sp
                )
            }
        }
    }
}
