package com.cricpeak.scorelive.ads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AdConfig(
    val ads_unit: String = "",
    val enabled: Boolean = false
)

data class AdsState(
    val match_details_banner: AdConfig = AdConfig(),
    val menu_banner: AdConfig = AdConfig(),
    val overs_banner: AdConfig = AdConfig(),
    val scorecard_banner: AdConfig = AdConfig(),
    val sticky_banner_bottom: AdConfig = AdConfig(),
    val teams_banner: AdConfig = AdConfig(),
    val uninstall_banner: AdConfig = AdConfig(),
    val slider_big_native: AdConfig = AdConfig(),
    val inter_home: AdConfig = AdConfig(),
    val inter_details: AdConfig = AdConfig(),
    val live_banner: AdConfig = AdConfig(),
    val upcoming_banner: AdConfig = AdConfig(),
    val completed_banner: AdConfig = AdConfig(),
    val upcoming_live_banner: AdConfig = AdConfig(),
    val exit_banner: AdConfig = AdConfig(),
    val native_ob_1: AdConfig = AdConfig(),
    val native_ob_3: AdConfig = AdConfig(),
    val reward_ai: AdConfig = AdConfig(),
    val reward_live: AdConfig = AdConfig(),
    val isLoaded: Boolean = false,
    val show_banner_ads: Boolean = true,
    val show_interstitial_ads: Boolean = true
)

class AdsViewModel(application: Application) : AndroidViewModel(application) {
    private val _adsState = MutableStateFlow(AdsState())
    val adsState: StateFlow<AdsState> = _adsState

    init {
        fetchAdsConfig()
    }

    private fun fetchAdsConfig() {
        try {
            val database = FirebaseDatabase.getInstance().getReference("ads_config")
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val matchDetails = snapshot.child("match_details_banner").getValue(AdConfig::class.java) ?: AdConfig()
                        val menu = snapshot.child("menu_banner").getValue(AdConfig::class.java) ?: AdConfig()
                        val overs = snapshot.child("overs_banner").getValue(AdConfig::class.java) ?: AdConfig()
                        val scorecard = snapshot.child("scorecard_banner").getValue(AdConfig::class.java) ?: AdConfig()
                        val stickyBottom = snapshot.child("sticky_banner_bottom").getValue(AdConfig::class.java) ?: AdConfig()
                        val teams = snapshot.child("teams_banner").getValue(AdConfig::class.java) ?: AdConfig()
                        val uninstall = snapshot.child("uninstall_banner").getValue(AdConfig::class.java) ?: AdConfig()
                        val sliderNative = snapshot.child("slider_big_native").getValue(AdConfig::class.java) ?: AdConfig()
                        val homeInterstitial = snapshot.child("inter_home").getValue(AdConfig::class.java) ?: AdConfig()
                        val matchDetailsInterstitial = snapshot.child("inter_details").getValue(AdConfig::class.java) ?: AdConfig()
                        
                        // New banners
                        val liveBanner = snapshot.child("live_banner").getValue(AdConfig::class.java) ?: AdConfig()
                        val upcomingBanner = snapshot.child("upcoming_banner").getValue(AdConfig::class.java) ?: AdConfig()
                        val completedBanner = snapshot.child("completed_banner").getValue(AdConfig::class.java) ?: AdConfig()
                        val upcomingLiveBanner = snapshot.child("upcoming_live_banner").getValue(AdConfig::class.java) ?: AdConfig()

                        // Exit Dialog and Onboarding Ads
                        val exitBanner = snapshot.child("exit_banner").getValue(AdConfig::class.java) ?: AdConfig()
                        val nativeOb1 = snapshot.child("native_ob_1").getValue(AdConfig::class.java) ?: AdConfig()
                        val nativeOb3 = snapshot.child("native_ob_3").getValue(AdConfig::class.java) ?: AdConfig()

                        // Rewarded Ads
                        val rewardAi = snapshot.child("reward_ai").getValue(AdConfig::class.java) ?: AdConfig()
                        val rewardLive = snapshot.child("reward_live").getValue(AdConfig::class.java) ?: AdConfig()

                        _adsState.value = _adsState.value.copy(
                            match_details_banner = matchDetails,
                            menu_banner = menu,
                            overs_banner = overs,
                            scorecard_banner = scorecard,
                            sticky_banner_bottom = stickyBottom,
                            teams_banner = teams,
                            uninstall_banner = uninstall,
                            slider_big_native = sliderNative,
                            inter_home = homeInterstitial,
                            inter_details = matchDetailsInterstitial,
                            live_banner = liveBanner,
                            upcoming_banner = upcomingBanner,
                            completed_banner = completedBanner,
                            upcoming_live_banner = upcomingLiveBanner,
                            exit_banner = exitBanner,
                            native_ob_1 = nativeOb1,
                            native_ob_3 = nativeOb3,
                            reward_ai = rewardAi,
                            reward_live = rewardLive,
                            isLoaded = true
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
