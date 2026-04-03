// com/iptvplayer/app/util/AdManager.kt
package com.iptvplayer.app.util

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {

    private const val TAG = "AdManager"

    // ── Replace these with your real AdMob IDs for production ───────────────
    // These are Google test ad unit IDs (safe for development)
    const val BANNER_AD_UNIT_ID     = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private var interstitialAd: InterstitialAd? = null
    private var interstitialShownOnce = false

    fun initialize(context: Context) {
        MobileAds.initialize(context) { initStatus ->
            Log.d(TAG, "AdMob initialized: $initStatus")
        }
    }

    fun loadBannerAd(adView: com.google.android.gms.ads.AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adView.visibility = View.VISIBLE
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                adView.visibility = View.GONE
                Log.e(TAG, "Banner ad failed: ${error.message}")
            }
        }
    }

    fun loadInterstitialAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial ad loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.e(TAG, "Interstitial ad failed: ${error.message}")
                }
            }
        )
    }

    /**
     * Shows the interstitial ad only ONCE per session (Play Store compliant).
     * After display, preloads the next ad.
     * [onAdDismissed] is called when ad is closed (proceed to player).
     */
    fun showInterstitialOnce(activity: Activity, onAdDismissed: () -> Unit) {
        val ad = interstitialAd
        if (ad != null && !interstitialShownOnce) {
            interstitialShownOnce = true
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(activity) // preload next
                    onAdDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } else {
            onAdDismissed()
        }
    }

    fun resetInterstitialFlag() {
        interstitialShownOnce = false
    }
}
