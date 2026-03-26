package com.cricpeak.scorelive.ads

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.cricpeak.scorelive.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun BannerAdView(adUnitId: String, modifier: Modifier = Modifier) {
    var isAdLoaded by remember { mutableStateOf(false) }
    var isAdFailed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        if (!isAdLoaded && !isAdFailed) {
            ShimmerBanner()
        }

        if (!isAdFailed) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        setAdUnitId(adUnitId)
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                isAdLoaded = true
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                isAdFailed = true
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}

@Composable
fun ShimmerBanner() {
    val shimmerColors = listOf(
        Color.Gray.copy(alpha = 0.6f),
        Color.Gray.copy(alpha = 0.2f),
        Color.Gray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(brush)
    )
}

@Composable
fun NativeAdViewHorizontal(adUnitId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isAdLoaded by remember { mutableStateOf(false) }
    var isAdFailed by remember { mutableStateOf(false) }
    val TAG = "NativeAdViewHorizontal"
    
    // Check if using problematic test unit
    val isTestUnit = adUnitId == "/21775744923/example/native"

    DisposableEffect(adUnitId) {
        Log.d(TAG, "🎯 Initializing AdLoader for unit: $adUnitId")
        if (isTestUnit) {
            Log.w(TAG, "⚠️ WARNING: Using known problematic test unit /2247696110")
            Log.w(TAG, "💡 RECOMMENDATION: Use your own production native ad unit from AdMob console")
            Log.w(TAG, "💡 This test unit often fails with 'Cannot determine request type' error")
        }
        
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                nativeAd = ad
                isAdLoaded = true
                Log.d(TAG, "✅ Native Ad loaded successfully!")
                Log.d(TAG, "💡 Headline: ${ad.headline}")
                Log.d(TAG, "💡 Body: ${ad.body}")
                Log.d(TAG, "💡 CTA: ${ad.callToAction}")
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isAdFailed = true
                    Log.e(TAG, "❌ Ad failed: ${error.message}, Code: ${error.code}")
                    if (error.code == 1 && isTestUnit) {
                        Log.e(TAG, "🔴 KNOWN ISSUE: Test unit /2247696110 is unreliable")
                        Log.e(TAG, "✅ SOLUTION: Create your own native ad unit in AdMob console")
                        Log.e(TAG, "📝 Steps:")
                        Log.e(TAG, "   1. Go to admob.google.com")
                        Log.e(TAG, "   2. Ads → Your App → Create Ad Unit")
                        Log.e(TAG, "   3. Type: Native Advanced")
                        Log.e(TAG, "   4. Copy the new ad unit ID")
                        Log.e(TAG, "   5. Update Firebase config with your real ad unit")
                    }
                }
                
                override fun onAdClicked() {
                    Log.d(TAG, "🖱️ Native ad clicked")
                }
                
                override fun onAdImpression() {
                    Log.d(TAG, "👁️ Native ad impression")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            Log.d(TAG, "🧹 Cleaning up native ad resources")
            nativeAd?.destroy()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        if (!isAdLoaded && !isAdFailed) {
            ShimmerNativeBanner()
        }

        if (isAdLoaded && nativeAd != null) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    val adView = LayoutInflater.from(ctx).inflate(R.layout.native_ad_layout, null) as NativeAdView
                    populateNativeAdView(nativeAd!!, adView)
                    adView
                },
                update = { adView ->
                    // Re-populate if needed, though usually handled by factory for single ads
                    nativeAd?.let { populateNativeAdView(it, adView) }
                }
            )
        } else if (isAdFailed) {
            Log.w(TAG, "Ad failed to display - using fallback space")
        }
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    val TAG = "NativeAdViewHorizontal"
    
    // Set the media view (full background).
    adView.mediaView = adView.findViewById(R.id.ad_media)

    // Set text elements.
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)

    // The headline is guaranteed to be in every NativeAd.
    (adView.headlineView as TextView).text = nativeAd.headline
    Log.d(TAG, "📰 Headline set: ${nativeAd.headline}")
    
    // Media content - full screen background
    if (nativeAd.mediaContent != null) {
        adView.mediaView?.setMediaContent(nativeAd.mediaContent!!)
        Log.d(TAG, "🖼️ Media content set (210dp full background)")
    } else {
        Log.w(TAG, "⚠️ No media content available")
    }

    // Body text - CRITICAL: Always show, even if short
    if (nativeAd.body == null || nativeAd.body!!.isEmpty()) {
        adView.bodyView?.visibility = View.VISIBLE
        (adView.bodyView as TextView).text = "Click to learn more"
        Log.w(TAG, "⚠️ Body text was empty, using fallback")
    } else {
        adView.bodyView?.visibility = View.VISIBLE
        (adView.bodyView as TextView).text = nativeAd.body
        Log.d(TAG, "📝 Body text set: ${nativeAd.body}")
    }

    // CTA button - CRITICAL: Always show
    if (nativeAd.callToAction == null || nativeAd.callToAction!!.isEmpty()) {
        adView.callToActionView?.visibility = View.VISIBLE
        (adView.callToActionView as Button).text = "Learn More"
        Log.w(TAG, "⚠️ CTA was empty, using fallback")
    } else {
        adView.callToActionView?.visibility = View.VISIBLE
        (adView.callToActionView as Button).text = nativeAd.callToAction
        Log.d(TAG, "🔘 CTA button set: ${nativeAd.callToAction}")
    }

    // Note: Icon and Star Rating removed from new design

    // This method tells the Google Mobile Ads SDK that it has finished populating the
    // native ad view with this native ad.
    adView.setNativeAd(nativeAd)
    Log.d(TAG, "✅ Native ad view fully populated! (Full background design, 210dp)")
}

@Composable
fun ShimmerNativeBanner() {
    val shimmerColors = listOf(
        Color.Gray.copy(alpha = 0.6f),
        Color.Gray.copy(alpha = 0.2f),
        Color.Gray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp) // Exact height to match slider layout
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
    )
}
