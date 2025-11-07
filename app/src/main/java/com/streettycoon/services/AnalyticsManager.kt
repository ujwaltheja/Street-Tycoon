package com.streettycoon.services

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

/**
 * Comprehensive analytics and crash reporting manager using Firebase
 */
class AnalyticsManager(private val context: Context) {

    companion object {
        private const val TAG = "AnalyticsManager"

        // Custom event names
        const val EVENT_GAME_START = "game_start"
        const val EVENT_GAME_SESSION_END = "game_session_end"
        const val EVENT_CUSTOMER_SERVED = "customer_served"
        const val EVENT_HELPER_HIRED = "helper_hired"
        const val EVENT_STALL_UPGRADED = "stall_upgraded"
        const val EVENT_ZONE_UNLOCKED = "zone_unlocked"
        const val EVENT_ACHIEVEMENT_UNLOCKED = "achievement_unlocked"
        const val EVENT_MONEY_EARNED = "money_earned"
        const val EVENT_AD_WATCHED = "ad_watched"
        const val EVENT_IAP_PURCHASED = "iap_purchased"
        const val EVENT_CHARACTER_HIRED = "character_hired"
        const val EVENT_CHARACTER_UPGRADED = "character_upgraded"
        const val EVENT_FAMILY_EXPENSE = "family_expense"
        const val EVENT_DAILY_REWARD = "daily_reward_claimed"
        const val EVENT_TUTORIAL_COMPLETE = "tutorial_complete"
        const val EVENT_SETTINGS_CHANGED = "settings_changed"

        // User properties
        const val PROPERTY_PLAYER_LEVEL = "player_level"
        const val PROPERTY_TOTAL_EARNINGS = "total_earnings"
        const val PROPERTY_ZONES_UNLOCKED = "zones_unlocked"
        const val PROPERTY_PLAY_TIME = "total_play_time"
        const val PROPERTY_SESSION_COUNT = "session_count"
    }

    private val analytics: FirebaseAnalytics = Firebase.analytics
    private val crashlytics: FirebaseCrashlytics = Firebase.crashlytics
    private val performance: FirebasePerformance = FirebasePerformance.getInstance()

    private var sessionStartTime: Long = 0
    private var activeTraces = mutableMapOf<String, Trace>()

    init {
        // Enable analytics collection
        analytics.setAnalyticsCollectionEnabled(true)

        // Enable crashlytics collection
        crashlytics.setCrashlyticsCollectionEnabled(true)

        Log.d(TAG, "Analytics Manager initialized")
    }

    // ==================== SESSION TRACKING ====================

    /**
     * Start a game session
     */
    fun startSession() {
        sessionStartTime = System.currentTimeMillis()
        logEvent(EVENT_GAME_START) {
            param("timestamp", sessionStartTime)
        }
        Log.d(TAG, "Game session started")
    }

    /**
     * End a game session
     */
    fun endSession() {
        val sessionDuration = System.currentTimeMillis() - sessionStartTime
        logEvent(EVENT_GAME_SESSION_END) {
            param("duration_seconds", sessionDuration / 1000)
        }
        Log.d(TAG, "Game session ended (duration: ${sessionDuration / 1000}s)")
    }

    // ==================== GAME EVENTS ====================

    /**
     * Track customer served
     */
    fun logCustomerServed(stallType: String, earnings: Long) {
        logEvent(EVENT_CUSTOMER_SERVED) {
            param("stall_type", stallType)
            param("earnings", earnings)
        }
    }

    /**
     * Track helper hired
     */
    fun logHelperHired(stallType: String, helperCount: Int, cost: Long) {
        logEvent(EVENT_HELPER_HIRED) {
            param("stall_type", stallType)
            param("helper_count", helperCount.toLong())
            param("cost", cost)
        }
    }

    /**
     * Track stall upgrade
     */
    fun logStallUpgraded(stallType: String, newLevel: Int, cost: Long) {
        logEvent(EVENT_STALL_UPGRADED) {
            param("stall_type", stallType)
            param("new_level", newLevel.toLong())
            param("cost", cost)
        }
    }

    /**
     * Track zone unlocked
     */
    fun logZoneUnlocked(zoneName: String, zoneIndex: Int) {
        logEvent(EVENT_ZONE_UNLOCKED) {
            param("zone_name", zoneName)
            param("zone_index", zoneIndex.toLong())
        }
    }

    /**
     * Track achievement unlocked
     */
    fun logAchievementUnlocked(achievementName: String) {
        logEvent(EVENT_ACHIEVEMENT_UNLOCKED) {
            param(FirebaseAnalytics.Param.ACHIEVEMENT_ID, achievementName)
        }
    }

    /**
     * Track money earned
     */
    fun logMoneyEarned(amount: Long, source: String) {
        logEvent(EVENT_MONEY_EARNED) {
            param("amount", amount)
            param("source", source)
        }
    }

    /**
     * Track character hired
     */
    fun logCharacterHired(characterType: String, characterName: String, cost: Long) {
        logEvent(EVENT_CHARACTER_HIRED) {
            param("character_type", characterType)
            param("character_name", characterName)
            param("cost", cost)
        }
    }

    /**
     * Track character upgraded
     */
    fun logCharacterUpgraded(characterType: String, newLevel: Int, cost: Long) {
        logEvent(EVENT_CHARACTER_UPGRADED) {
            param("character_type", characterType)
            param("new_level", newLevel.toLong())
            param("cost", cost)
        }
    }

    /**
     * Track family expense
     */
    fun logFamilyExpense(category: String, amount: Long) {
        logEvent(EVENT_FAMILY_EXPENSE) {
            param("category", category)
            param("amount", amount)
        }
    }

    /**
     * Track daily reward claimed
     */
    fun logDailyReward(day: Int, reward: Long) {
        logEvent(EVENT_DAILY_REWARD) {
            param("day", day.toLong())
            param("reward", reward)
        }
    }

    /**
     * Track tutorial completion
     */
    fun logTutorialComplete() {
        logEvent(EVENT_TUTORIAL_COMPLETE)
    }

    /**
     * Track settings changed
     */
    fun logSettingsChanged(setting: String, value: String) {
        logEvent(EVENT_SETTINGS_CHANGED) {
            param("setting", setting)
            param("value", value)
        }
    }

    // ==================== MONETIZATION EVENTS ====================

    /**
     * Track ad watched
     */
    fun logAdWatched(adType: String, reward: Long? = null) {
        logEvent(EVENT_AD_WATCHED) {
            param(FirebaseAnalytics.Param.AD_FORMAT, adType)
            reward?.let { param("reward", it) }
        }
    }

    /**
     * Track in-app purchase
     */
    fun logPurchase(itemId: String, value: Double, currency: String = "USD") {
        logEvent(FirebaseAnalytics.Event.PURCHASE) {
            param(FirebaseAnalytics.Param.ITEM_ID, itemId)
            param(FirebaseAnalytics.Param.VALUE, value)
            param(FirebaseAnalytics.Param.CURRENCY, currency)
        }
    }

    // ==================== USER PROPERTIES ====================

    /**
     * Set user properties for segmentation
     */
    fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }

    /**
     * Update player level property
     */
    fun updatePlayerLevel(level: Int) {
        setUserProperty(PROPERTY_PLAYER_LEVEL, level.toString())
    }

    /**
     * Update total earnings property
     */
    fun updateTotalEarnings(totalEarnings: Long) {
        setUserProperty(PROPERTY_TOTAL_EARNINGS, totalEarnings.toString())
    }

    /**
     * Update zones unlocked property
     */
    fun updateZonesUnlocked(zonesCount: Int) {
        setUserProperty(PROPERTY_ZONES_UNLOCKED, zonesCount.toString())
    }

    // ==================== PERFORMANCE MONITORING ====================

    /**
     * Start a performance trace
     */
    fun startTrace(traceName: String) {
        val trace = performance.newTrace(traceName)
        trace.start()
        activeTraces[traceName] = trace
        Log.d(TAG, "Performance trace started: $traceName")
    }

    /**
     * Stop a performance trace
     */
    fun stopTrace(traceName: String) {
        activeTraces[traceName]?.let { trace ->
            trace.stop()
            activeTraces.remove(traceName)
            Log.d(TAG, "Performance trace stopped: $traceName")
        }
    }

    /**
     * Add custom metric to trace
     */
    fun putTraceMetric(traceName: String, metricName: String, value: Long) {
        activeTraces[traceName]?.putMetric(metricName, value)
    }

    /**
     * Add custom attribute to trace
     */
    fun putTraceAttribute(traceName: String, attribute: String, value: String) {
        activeTraces[traceName]?.putAttribute(attribute, value)
    }

    // ==================== CRASH REPORTING ====================

    /**
     * Log non-fatal exception
     */
    fun logException(exception: Exception, context: String? = null) {
        context?.let { crashlytics.log(it) }
        crashlytics.recordException(exception)
        Log.e(TAG, "Exception logged: ${exception.message}", exception)
    }

    /**
     * Set custom crash keys
     */
    fun setCrashKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCrashKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCrashKey(key: String, value: Long) {
        crashlytics.setCustomKey(key, value)
    }

    fun setCrashKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    /**
     * Set user identifier for crash reports
     */
    fun setUserId(userId: String) {
        analytics.setUserId(userId)
        crashlytics.setUserId(userId)
    }

    /**
     * Log custom message for debugging
     */
    fun log(message: String) {
        crashlytics.log(message)
        Log.d(TAG, message)
    }

    // ==================== HELPER FUNCTIONS ====================

    /**
     * Generic event logging with builder pattern
     */
    private fun logEvent(eventName: String, block: Bundle.() -> Unit = {}) {
        analytics.logEvent(eventName) {
            block()
        }
    }

    /**
     * Extension function for cleaner parameter setting
     */
    private fun Bundle.param(key: String, value: String) {
        putString(key, value)
    }

    private fun Bundle.param(key: String, value: Long) {
        putLong(key, value)
    }

    private fun Bundle.param(key: String, value: Double) {
        putDouble(key, value)
    }

    private fun Bundle.param(key: String, value: Bundle) {
        putBundle(key, value)
    }

    /**
     * Track screen view
     */
    fun logScreenView(screenName: String, screenClass: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
    }
}
