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
import com.cricpeak.scorelive.ui.PurchaseScreen
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
                onPurchaseClick = {
                    navController.navigate("purchase")
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

        composable("purchase") {
            PurchaseScreen(onClose = { navController.popBackStack() })
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
