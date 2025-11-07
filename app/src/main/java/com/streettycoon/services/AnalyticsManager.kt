package com.streettycoon.services

import android.content.Context
import android.util.Log

/**
 * Analytics manager - Firebase temporarily disabled
 * This is a no-op implementation until Firebase is properly configured
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

    private var sessionStartTime: Long = 0

    init {
        Log.d(TAG, "Analytics Manager initialized (Firebase disabled)")
    }

    // ==================== SESSION TRACKING ====================

    fun startSession() {
        sessionStartTime = System.currentTimeMillis()
        Log.d(TAG, "Game session started")
    }

    fun endSession() {
        val sessionDuration = System.currentTimeMillis() - sessionStartTime
        Log.d(TAG, "Game session ended (duration: ${sessionDuration / 1000}s)")
    }

    // ==================== GAME EVENTS ====================

    fun logCustomerServed(stallType: String, earnings: Long) {
        Log.d(TAG, "Customer served: $stallType, earnings: $earnings")
    }

    fun logHelperHired(stallType: String, helperCount: Int, cost: Long) {
        Log.d(TAG, "Helper hired: $stallType, count: $helperCount, cost: $cost")
    }

    fun logStallUpgraded(stallType: String, newLevel: Int, cost: Long) {
        Log.d(TAG, "Stall upgraded: $stallType, level: $newLevel, cost: $cost")
    }

    fun logZoneUnlocked(zoneName: String, zoneIndex: Int) {
        Log.d(TAG, "Zone unlocked: $zoneName, index: $zoneIndex")
    }

    fun logAchievementUnlocked(achievementName: String) {
        Log.d(TAG, "Achievement unlocked: $achievementName")
    }

    fun logMoneyEarned(amount: Long, source: String) {
        Log.d(TAG, "Money earned: $amount from $source")
    }

    fun logCharacterHired(characterType: String, characterName: String, cost: Long) {
        Log.d(TAG, "Character hired: $characterType - $characterName, cost: $cost")
    }

    fun logCharacterUpgraded(characterType: String, newLevel: Int, cost: Long) {
        Log.d(TAG, "Character upgraded: $characterType, level: $newLevel, cost: $cost")
    }

    fun logFamilyExpense(category: String, amount: Long) {
        Log.d(TAG, "Family expense: $category, amount: $amount")
    }

    fun logDailyReward(day: Int, reward: Long) {
        Log.d(TAG, "Daily reward claimed: day $day, reward: $reward")
    }

    fun logTutorialComplete() {
        Log.d(TAG, "Tutorial completed")
    }

    fun logSettingsChanged(setting: String, value: String) {
        Log.d(TAG, "Settings changed: $setting = $value")
    }

    // ==================== MONETIZATION EVENTS ====================

    fun logAdWatched(adType: String, reward: Long? = null) {
        Log.d(TAG, "Ad watched: $adType, reward: $reward")
    }

    fun logPurchase(itemId: String, value: Double, currency: String = "USD") {
        Log.d(TAG, "Purchase: $itemId, value: $value $currency")
    }

    // ==================== USER PROPERTIES ====================

    fun setUserProperty(name: String, value: String) {
        Log.d(TAG, "User property set: $name = $value")
    }

    fun updatePlayerLevel(level: Int) {
        setUserProperty(PROPERTY_PLAYER_LEVEL, level.toString())
    }

    fun updateTotalEarnings(totalEarnings: Long) {
        setUserProperty(PROPERTY_TOTAL_EARNINGS, totalEarnings.toString())
    }

    fun updateZonesUnlocked(zonesCount: Int) {
        setUserProperty(PROPERTY_ZONES_UNLOCKED, zonesCount.toString())
    }

    // ==================== PERFORMANCE MONITORING ====================

    fun startTrace(traceName: String) {
        Log.d(TAG, "Performance trace started: $traceName")
    }

    fun stopTrace(traceName: String) {
        Log.d(TAG, "Performance trace stopped: $traceName")
    }

    fun putTraceMetric(traceName: String, metricName: String, value: Long) {
        Log.d(TAG, "Trace metric: $traceName.$metricName = $value")
    }

    fun putTraceAttribute(traceName: String, attribute: String, value: String) {
        Log.d(TAG, "Trace attribute: $traceName.$attribute = $value")
    }

    // ==================== CRASH REPORTING ====================

    fun logException(exception: Exception, context: String? = null) {
        Log.e(TAG, "Exception logged: ${exception.message}", exception)
    }

    fun setCrashKey(key: String, value: String) {
        Log.d(TAG, "Crash key set: $key = $value")
    }

    fun setCrashKey(key: String, value: Int) {
        Log.d(TAG, "Crash key set: $key = $value")
    }

    fun setCrashKey(key: String, value: Long) {
        Log.d(TAG, "Crash key set: $key = $value")
    }

    fun setCrashKey(key: String, value: Boolean) {
        Log.d(TAG, "Crash key set: $key = $value")
    }

    fun setUserId(userId: String) {
        Log.d(TAG, "User ID set: $userId")
    }

    fun log(message: String) {
        Log.d(TAG, message)
    }

    fun logScreenView(screenName: String, screenClass: String) {
        Log.d(TAG, "Screen view: $screenName ($screenClass)")
    }
}
