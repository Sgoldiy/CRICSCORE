package com.cricpeak.scorelive

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cricpeak.scorelive.ui.CricScoreHomeScreen
import com.cricpeak.scorelive.ui.AIPredictionScreen
import com.cricpeak.scorelive.ui.MatchDetailsScreen
import com.cricpeak.scorelive.ui.MenuScreen
import com.cricpeak.scorelive.ui.OnboardingScreen
import com.cricpeak.scorelive.ui.PrivacyPolicyScreen
import com.cricpeak.scorelive.ui.SplashScreen
import com.cricpeak.scorelive.ui.UninstallFeedbackScreen
import com.cricpeak.scorelive.ui.theme.CRICPEAKTheme
import com.cricpeak.scorelive.ui.components.ExitConfirmationDialog
import com.cricpeak.scorelive.ui.components.InternetConnectionDialog
import com.cricpeak.scorelive.util.NetworkUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cricpeak.scorelive.ads.AdsViewModel
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {}

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb())
        )
        setContent {
            CRICPEAKTheme {
                AppNavigation(activity = this)
            }
        }
    }
}


@SuppressLint("RememberReturnType")
@Composable
fun AppNavigation(activity: MainActivity? = null) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("cricscore_prefs", Context.MODE_PRIVATE) }
    val adsViewModel: AdsViewModel = viewModel()
    
    // Check if onboarding is completed
    var showOnboarding by remember { 
        mutableStateOf(!sharedPref.getBoolean("onboarding_completed", false)) 
    }

    // Internet connectivity state
    var showNoInternetDialog by remember { mutableStateOf(false) }
    var isInternetAvailable by remember { mutableStateOf(true) }

    // Exit confirmation dialog state
    var showExitDialog by remember { mutableStateOf(false) }

    // State to persist selected tab in CricScoreHomeScreen
    var selectedHomeTabIndex by remember { mutableIntStateOf(0) }
    
    // Collect ads state
    val adsState by adsViewModel.adsState.collectAsState()

    // Check internet connectivity
    remember {
        isInternetAvailable = NetworkUtils.isInternetAvailable(context)
        showNoInternetDialog = !isInternetAvailable
    }

    // Handle back press to show exit dialog
    BackHandler {
        val currentDestination = navController.currentDestination?.route
        if (currentDestination == "home" || currentDestination == "splash" || currentDestination == "onboarding") {
            showExitDialog = true
        } else {
            navController.popBackStack()
        }
    }

    if (showNoInternetDialog && !isInternetAvailable) {
        InternetConnectionDialog(
            onDismiss = {
                isInternetAvailable = NetworkUtils.isInternetAvailable(context)
                if (isInternetAvailable) {
                    showNoInternetDialog = false
                }
            }
        )
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirm = {
                activity?.finish()
            },
            onDismiss = {
                showExitDialog = false
            },
            adConfig = adsState.exit_banner
        )
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(onTimeout = {
                if (showOnboarding) {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                } else {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            })
        }
        
        composable("onboarding") {
            OnboardingScreen(
                onFinish = {
                    sharedPref.edit { putBoolean("onboarding_completed", true) }
                    showOnboarding = false
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                adsState = adsState
            )
        }

        composable("home") {
            CricScoreHomeScreen(
                selectedTabIndex = selectedHomeTabIndex,
                onTabSelected = { index -> selectedHomeTabIndex = index },
                onMatchClick = { matchId ->
                    navController.navigate("details/$matchId")
                },
                onPredictionClick = { matchId ->
                    navController.navigate("prediction/$matchId")
                },
                onMenuClick = {
                    navController.navigate("menu")
                },
                adsState = adsState
            )
        }
        
        composable("menu") {
            MenuScreen(
                onBack = { 
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onPrivacyPolicyClick = { navController.navigate("privacy_policy") },
                onUninstallClick = { navController.navigate("uninstall_feedback") },
                adsState = adsState
            )
        }

        composable("uninstall_feedback") {
            UninstallFeedbackScreen(
                onBack = { navController.popBackStack() },
                adsState = adsState
            )
        }

        composable("privacy_policy") {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }
        
        composable(
            route = "details/{matchId}",
            arguments = listOf(navArgument("matchId") { type = NavType.IntType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getInt("matchId") ?: 0
            MatchDetailsScreen(
                matchId = matchId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "prediction/{matchId}",
            arguments = listOf(navArgument("matchId") { type = NavType.IntType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getInt("matchId") ?: 0
            AIPredictionScreen(
                matchId = matchId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}


/*package com.cricpeak.scorelive.ui

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cricpeak.scorelive.R
import com.cricpeak.scorelive.ads.AdConfig
import com.cricpeak.scorelive.ads.AdInterstitialManager
import com.cricpeak.scorelive.ads.AdRewardManager
import com.cricpeak.scorelive.ads.AdsState
import com.cricpeak.scorelive.ads.BannerAdView
import com.cricpeak.scorelive.ads.NativeAdViewHorizontal
import com.cricpeak.scorelive.ads.RewardPrefs
import com.cricpeak.scorelive.ui.apicallfiles.CricketViewModel
import com.cricpeak.scorelive.ui.apicallfiles.FixtureContainer
import com.cricpeak.scorelive.ui.apicallfiles.FixturesState
import com.cricpeak.scorelive.ui.components.RewardPolicyDialog
import com.cricpeak.scorelive.ui.theme.AccentGreen
import com.cricpeak.scorelive.ui.theme.BayonFont
import com.cricpeak.scorelive.ui.theme.CardBackground
import com.cricpeak.scorelive.ui.theme.DarkBackground
import com.cricpeak.scorelive.ui.theme.GilroyFontFamily
import com.cricpeak.scorelive.ui.theme.RadialTeal
import com.cricpeak.scorelive.ui.theme.TextGray
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
    interval: Int = 3
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

    // Track previous tab index to detect changes
    var previousTabIndex by remember { mutableStateOf(selectedTabIndex) }

    // State for Reward Policy Dialog
    var showRewardPolicyDialog by remember { mutableStateOf(false) }
    var pendingPredictionMatchId by remember { mutableStateOf<Int?>(null) }

    // Pre-load ads
    LaunchedEffect(adsState) {
        val interConfig = adsState?.inter_home
        if (interConfig?.enabled == true && interConfig.ads_unit.isNotEmpty()) {
            AdInterstitialManager.HomeScreenAdManager.loadAd(context, interConfig.ads_unit)
        }

        val rewardAiConfig = adsState?.reward_ai
        if (rewardAiConfig?.enabled == true && rewardAiConfig.ads_unit.isNotEmpty()) {
            AdRewardManager.loadAd(context, rewardAiConfig.ads_unit)
        }
    }

    // Handle tab changes and show interstitial ad using Global Manager
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex != previousTabIndex && activity != null) {
            val adConfig = adsState?.inter_home
            if (adConfig?.enabled == true && adConfig.ads_unit.isNotEmpty()) {
                AdInterstitialManager.HomeScreenAdManager.showAdIfAvailable(
                    activity = activity,
                    adUnitId = adConfig.ads_unit
                ) {
                    // Continue navigation logic here if needed
                }
            }
        }
        previousTabIndex = selectedTabIndex
    }

    val radialGradient = Brush.radialGradient(
        colors = listOf(RadialTeal.copy(alpha = 0.5f), Color.Transparent),
        center = androidx.compose.ui.geometry.Offset(540f, -50f),
        radius = 1000f
    )

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

        when (val state = fixturesState) {
            is FixturesState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentGreen)
                }
            }

            is FixturesState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Error: ${state.message}", color = Color.Red)
                        Button(
                            onClick = { viewModel.fetchFixtures() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            is FixturesState.Success -> {
                val fixtures = state.response

                // Priority logic: all live matches first, then fill with latest completed/upcoming
                val topFiveMatches = remember(fixtures) {
                    val list = mutableListOf<FixtureContainer>()
                    list.addAll(fixtures.inProgressFixtures)
                    if (list.size < 5) {
                        list.addAll(fixtures.completedFixtures.take(5 - list.size))
                    }
                    if (list.size < 5) {
                        list.addAll(fixtures.upcomingFixtures.take(5 - list.size))
                    }
                    list.take(5)
                }

                // Prepare lists with ads outside LazyColumn
                val liveMatchesWithAds = remember(fixtures.inProgressFixtures, adsState?.live_banner) {
                    getItemsWithAds(fixtures.inProgressFixtures, adsState?.live_banner)
                }

                val upcomingLiveMatchesWithAds = remember(fixtures.upcomingFixtures, adsState?.upcoming_live_banner) {
                    getItemsWithAds(fixtures.upcomingFixtures.take(5), adsState?.upcoming_live_banner)
                }

                val displayFixtures = when (selectedTabIndex) {
                    1 -> fixtures.upcomingFixtures
                    else -> fixtures.completedFixtures
                }

                val tabAdConfig = when (selectedTabIndex) {
                    1 -> adsState?.upcoming_banner
                    else -> adsState?.completed_banner
                }

                val tabItemsWithAds = remember(displayFixtures, tabAdConfig) {
                    getItemsWithAds(displayFixtures, tabAdConfig)
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
                        contentPadding = PaddingValues(bottom = if (adsState?.sticky_banner_bottom?.enabled == true && adsState.sticky_banner_bottom.ads_unit.isNotEmpty()) 60.dp else 24.dp)
                    ) {
                        item {
                            TopBar(onMenuClick = onMenuClick)
                        }

                        // Featured Matches Section
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

                                val totalItems = 7 // 5 matches + 2 native ads
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
                                            page == 3 -> {
                                                if (adsState?.slider_big_native?.enabled == true &&
                                                    adsState.slider_big_native.ads_unit.isNotEmpty()
                                                ) {
                                                    NativeAdViewHorizontal(
                                                        adUnitId = adsState.slider_big_native.ads_unit
                                                    )
                                                } else {
                                                    FeaturedMatchCard(
                                                        topFiveMatches[page],
                                                        onMatchClick,
                                                        onPredictionClick,
                                                        showPrediction = false
                                                    )
                                                }
                                            }
                                            page == 6 -> {
                                                if (adsState?.slider_big_native?.enabled == true &&
                                                    adsState.slider_big_native.ads_unit.isNotEmpty()
                                                ) {
                                                    NativeAdViewHorizontal(
                                                        adUnitId = adsState.slider_big_native.ads_unit
                                                    )
                                                } else {
                                                    FeaturedMatchCard(
                                                        topFiveMatches[page],
                                                        onMatchClick,
                                                        onPredictionClick,
                                                        showPrediction = false
                                                    )
                                                }
                                            }
                                            else -> {
                                                val matchIndex = when {
                                                    page < 3 -> page
                                                    page > 3 -> page - 1
                                                    else -> page
                                                }
                                                if (matchIndex < topFiveMatches.size) {
                                                    FeaturedMatchCard(
                                                        topFiveMatches[matchIndex],
                                                        onMatchClick,
                                                        onPredictionClick,
                                                        showPrediction = false
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        repeat(totalItems) { iteration ->
                                            Box(
                                                modifier = Modifier
                                                    .padding(4.dp)
                                                    .size(if (pagerState.currentPage == iteration) 10.dp else 6.dp)
                                                    .background(
                                                        if (pagerState.currentPage == iteration) Color.White else Color.Gray.copy(
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
                        // Category Tabs
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            CategoryTabs(categories, selectedTabIndex, onTabSelected)
                        }
                        if (selectedTabIndex == 0) {
                            // LIVE tab
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
                                items(liveMatchesWithAds) { item ->
                                    when (item) {
                                        is MatchListItemType.Match -> MatchListItem(item.fixture, onMatchClick, onPredictionClick)
                                        is MatchListItemType.Ad -> {
                                            NativeAdViewHorizontal(
                                                adUnitId = item.adConfig.ads_unit,
                                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                            )
                                        }
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
                            items(upcomingLiveMatchesWithAds) { item ->
                                when (item) {
                                    is MatchListItemType.Match -> {
                                        MatchListItem(
                                            fixture = item.fixture,
                                            onMatchClick = onMatchClick,
                                            onPredictionClick = { matchId ->
                                                val rewardAiConfig = adsState?.reward_ai
                                                if (rewardAiConfig?.enabled == true && rewardAiConfig.ads_unit.isNotEmpty()) {
                                                    if (!RewardPrefs.isPolicyAccepted(context)) {
                                                        pendingPredictionMatchId = matchId
                                                        showRewardPolicyDialog = true
                                                    } else {
                                                        if (activity != null) {
                                                            AdRewardManager.showAd(
                                                                activity = activity,
                                                                adUnitId = rewardAiConfig.ads_unit,
                                                                onRewardEarned = { onPredictionClick(matchId) }
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    onPredictionClick(matchId)
                                                }
                                            }
                                        )
                                    }
                                    is MatchListItemType.Ad -> {
                                        NativeAdViewHorizontal(
                                            adUnitId = item.adConfig.ads_unit,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Other tabs
                            items(tabItemsWithAds) { item ->
                                when (item) {
                                    is MatchListItemType.Match -> {
                                        MatchListItem(
                                            item.fixture,
                                            onMatchClick,
                                            onPredictionClick,
                                            showPrediction = false
                                        )
                                    }
                                    is MatchListItemType.Ad -> {
                                        NativeAdViewHorizontal(
                                            adUnitId = item.adConfig.ads_unit,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            if (displayFixtures.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No matches found",
                                            color = TextGray,
                                            fontFamily = GilroyFontFamily
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Sticky Banner Bottom
        if (adsState?.sticky_banner_bottom?.enabled == true && adsState.sticky_banner_bottom.ads_unit.isNotEmpty()) {
            val navBarHeight = WindowInsets.navigationBars.getBottom(LocalDensity.current)
            val navBarHeightDp = with(LocalDensity.current) { navBarHeight.toDp() }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(0.8f))
                    .padding(bottom = navBarHeightDp)
            ) {
                BannerAdView(adUnitId = adsState.sticky_banner_bottom.ads_unit)
            }
        }

        // Reward Policy Dialog
        if (showRewardPolicyDialog) {
            RewardPolicyDialog(
                onAccept = {
                    RewardPrefs.setPolicyAccepted(context, true)
                    showRewardPolicyDialog = false
                    val rewardAiConfig = adsState?.reward_ai
                    if (activity != null && rewardAiConfig != null && pendingPredictionMatchId != null) {
                        AdRewardManager.showAd(
                            activity = activity,
                            adUnitId = rewardAiConfig.ads_unit,
                            onRewardEarned = {
                                pendingPredictionMatchId?.let { onPredictionClick(it) }
                                pendingPredictionMatchId = null
                            }
                        )
                    }
                },
                onDismiss = {
                    showRewardPolicyDialog = false
                    pendingPredictionMatchId = null
                }
            )
        }
    }
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
            letterSpacing = (36 * 0.07).sp,
            fontWeight = FontWeight.Normal
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
                // Top Row: Separated texts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Text: Competition Name
                    Text(
                        text = fixture.competition.name,
                        color = TextGray,
                        fontSize = 14.sp,
                        fontFamily = GilroyFontFamily,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right Text: Status Pill or Match Type
                    if (fixture.isLive || fixture.isInProgress) {
                        Surface(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(30.dp),
                            border = BorderStroke(0.5.dp, AccentGreen)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AccentGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "LIVE",
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
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Teams and Scores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val homeInning = fixture.innings?.find { it.inningNumber == 1 }
                    val awayInning = fixture.innings?.find { it.inningNumber == 2 }

                    TeamInfoFeatured(
                        name = fixture.homeTeam.shortName,
                        score = if (isUpcoming) "-" else (homeInning?.let { "${it.runsScored}/${it.wickets}" }
                            ?: "0/0"),
                        overs = if (isUpcoming) "" else (homeInning?.let { "(${it.oversBowled})" }
                            ?: "(0)"),
                        logoUrl = fixture.homeTeam.logoUrl,
                        modifier = Modifier.weight(1f)
                    )

                    // Middle vertical line or Date/Time
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
                        score = if (isUpcoming) "-" else (awayInning?.let { "${it.runsScored}/${it.wickets}" }
                            ?: "0/0"),
                        overs = if (isUpcoming) "" else (awayInning?.let { "(${it.oversBowled})" }
                            ?: "(0)"),
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
                                "AI Prediction",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = GilroyFontFamily
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("✨", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Bottom result line / Status
            val footerText = fixture.resultText ?: (if (isUpcoming) "Match starting soon" else "")
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                color = Color(0xFF2C2C2E),
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            ) {
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
    @Suppress("UNUSED_VARIABLE")
    val arrangement = if (isRight) Arrangement.End else Arrangement.Start
    Column(
        modifier = modifier,
        horizontalAlignment = if (isRight) Alignment.End else Alignment.Start
    ) {
        val rowArrangement = if (isRight) Arrangement.End else Arrangement.Start
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = rowArrangement,
            modifier = Modifier.fillMaxWidth()
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
        categories.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isSelected) Color.Black.copy(alpha = 0.4f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (isSelected) AccentGreen else Color.Transparent,
                        RoundedCornerShape(24.dp)
                    )
                    .clickable { onTabSelected(index) }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = fixture.competition.name,
                        color = TextGray,
                        fontSize = 14.sp,
                        fontFamily = GilroyFontFamily,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isUpcoming) {
                        Text(
                            text = formatMatchDate(fixture.startDateTime),
                            color = AccentGreen,
                            fontSize = 12.sp,
                            fontFamily = GilroyFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val homeInning = fixture.innings?.find { it.inningNumber == 1 }
                val awayInning = fixture.innings?.find { it.inningNumber == 2 }

                TeamRow(
                    name = fixture.homeTeam.shortName,
                    score = if (isUpcoming) "-" else (homeInning?.let { "${it.runsScored}/${it.wickets}" }
                        ?: "0/0"),
                    overs = if (isUpcoming) "" else (homeInning?.let { "(${it.oversBowled})" }
                        ?: ""),
                    logoUrl = fixture.homeTeam.logoUrl
                )
                Spacer(modifier = Modifier.height(10.dp))
                TeamRow(
                    name = fixture.awayTeam.shortName,
                    score = if (isUpcoming) "-" else (awayInning?.let { "${it.runsScored}/${it.wickets}" }
                        ?: "0/0"),
                    overs = if (isUpcoming) "" else (awayInning?.let { "(${it.oversBowled})" }
                        ?: ""),
                    logoUrl = fixture.awayTeam.logoUrl
                )

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
                                    "AI Prediction",
                                    color = Color(0xFF00BFA5),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = GilroyFontFamily
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("✨", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Bottom pill
            val footerText = fixture.resultText ?: (if (isUpcoming) "Upcoming" else "")
            if (footerText.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter),
                    color = Color(0xFF2C2C2E),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                ) {
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
}

@Composable
fun TeamRow(name: String, score: String, overs: String, logoUrl: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TeamLogo(logoUrl)
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
*/