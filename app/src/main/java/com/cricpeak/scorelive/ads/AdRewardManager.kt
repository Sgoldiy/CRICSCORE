package com.cricpeak.scorelive.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdRewardManager {
    private const val TAG = "AdRewardManager"
    
    private val rewardedAds = mutableMapOf<String, RewardedAd?>()
    private val loadingStates = mutableMapOf<String, Boolean>()
    private var isShowing = false

    fun loadAd(context: Context, adUnitId: String, isPremium: Boolean) {
        if (isPremium) return

        if (adUnitId.isEmpty() || loadingStates[adUnitId] == true || rewardedAds[adUnitId] != null) return

        loadingStates[adUnitId] = true
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Ad failed to load ($adUnitId): ${adError.message}")
                rewardedAds[adUnitId] = null
                loadingStates[adUnitId] = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Ad loaded: $adUnitId")
                rewardedAds[adUnitId] = ad
                loadingStates[adUnitId] = false
            }
        })
    }

    fun showAd(
        activity: Activity, 
        adUnitId: String, 
        isPremium: Boolean, // Added isPremium parameter
        onRewardEarned: () -> Unit, 
        onAdDismissed: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        if (isPremium) { // If premium, just call onRewardEarned immediately as if ad was watched
            onRewardEarned()
            return
        }

        if (isShowing) return
        
        val ad = rewardedAds[adUnitId]
        if (ad == null) {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
            Toast.makeText(activity, "Ad is still loading, please wait...", Toast.LENGTH_SHORT).show()
            loadAd(activity, adUnitId, isPremium) // Pass isPremium
            onAdFailed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed.")
                rewardedAds[adUnitId] = null
                isShowing = false
                loadAd(activity, adUnitId, isPremium) // Preload next, pass isPremium
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show: ${adError.message}")
                rewardedAds[adUnitId] = null
                isShowing = false
                loadAd(activity, adUnitId, isPremium) // Pass isPremium
                onAdFailed()
            }

            override fun onAdShowedFullScreenContent() {
                isShowing = true
            }
        }

        ad.show(activity) { _ ->
            Log.d(TAG, "User earned the reward.")
            onRewardEarned()
        }
    }
    
    fun isAdReady(adUnitId: String): Boolean = rewardedAds[adUnitId] != null
}