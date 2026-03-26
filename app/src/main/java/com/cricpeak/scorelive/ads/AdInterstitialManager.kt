package com.cricpeak.scorelive.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdInterstitialManager(private val cooldownMs: Long = 30_000L) {

    private val TAG = "AdManager"

    private var interstitialAd: InterstitialAd? = null
    private var lastAdTime = 0L
    private var isLoading = false

    // ✅ Load Ad
    fun loadAd(context: Context, adUnitId: String) {
        if (adUnitId.isEmpty()) return
        if (interstitialAd != null || isLoading) return

        isLoading = true

        val request = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            request,
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    Log.d(TAG, "Ad Loaded")

                    interstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {

                            override fun onAdShowedFullScreenContent() {
                                Log.d(TAG, "Ad Shown")
                                lastAdTime = System.currentTimeMillis()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Ad Dismissed")
                                interstitialAd = null
                                loadAd(context, adUnitId) // preload next
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Log.d(TAG, "Ad Failed: ${adError.message}")
                                interstitialAd = null
                                loadAd(context, adUnitId)
                            }
                        }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    interstitialAd = null
                    Log.d(TAG, "Load Failed: ${error.message}")
                }
            }
        )
    }

    // ✅ Check cooldown
    private fun canShowAd(): Boolean {
        return System.currentTimeMillis() - lastAdTime >= cooldownMs
    }

    // ✅ Show Ad
    fun showAdIfAvailable(activity: Activity, adUnitId: String, onContinue: () -> Unit) {

        if (!canShowAd()) {
            Log.d(TAG, "Cooldown Active → Skip Ad")
            onContinue()
            return
        }

        val ad = interstitialAd

        if (ad != null) {
            Log.d(TAG, "Showing Ad")
            ad.show(activity)
        } else {
            Log.d(TAG, "Ad Not Ready → Continue")
            loadAd(activity, adUnitId)
            onContinue()
        }
    }

    companion object {
        // Global instances for separate timers
        val HomeScreenAdManager = AdInterstitialManager()
        val MatchDetailsAdManager = AdInterstitialManager()
    }
}
