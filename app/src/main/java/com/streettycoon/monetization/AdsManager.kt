package com.streettycoon.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Manager for Google AdMob integration
 * Handles rewarded ads for game boosters
 */
class AdsManager(private val context: Context) {

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    init {
        // Initialize Mobile Ads SDK
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob initialized: ${initializationStatus.adapterStatusMap}")
        }
        loadRewardedAd()
    }

    /**
     * Load a rewarded ad
     */
    fun loadRewardedAd() {
        if (isLoading || rewardedAd != null) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed to load: ${adError.message}")
                    rewardedAd = null
                    isLoading = false
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    rewardedAd = ad
                    isLoading = false

                    // Set ad callbacks
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Rewarded ad dismissed")
                            rewardedAd = null
                            loadRewardedAd() // Preload next ad
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                            rewardedAd = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Rewarded ad shown")
                        }
                    }
                }
            }
        )
    }

    /**
     * Show rewarded ad if available
     * @param activity The activity to show the ad on
     * @param onRewarded Callback when user earns reward
     * @param onAdNotReady Callback when ad is not ready
     */
    fun showRewardedAd(
        activity: Activity,
        onRewarded: (rewardAmount: Int) -> Unit,
        onAdNotReady: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.show(activity) { rewardItem ->
                val amount = rewardItem.amount
                Log.d(TAG, "User earned reward: $amount")
                onRewarded(amount)
            }
        } else {
            Log.w(TAG, "Rewarded ad not ready")
            onAdNotReady()
            loadRewardedAd() // Try to load
        }
    }

    /**
     * Check if rewarded ad is ready to show
     */
    fun isRewardedAdReady(): Boolean = rewardedAd != null

    companion object {
        private const val TAG = "AdsManager"

        // Test ad unit ID - replace with your actual ad unit ID for production
        private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }
}
