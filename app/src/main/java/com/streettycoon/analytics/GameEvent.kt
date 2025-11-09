package com.streettycoon.analytics

import com.google.gson.Gson
import com.streettycoon.data.GameEventEntity

/**
 * Sealed class hierarchy for all game analytics events
 * Each event type has specific properties relevant to tracking
 */
sealed class GameEvent(
    val eventName: String,
    val properties: Map<String, Any>
) {
    fun toEntity(): GameEventEntity {
        val gson = Gson()
        return GameEventEntity(
            eventName = eventName,
            eventData = gson.toJson(properties),
            timestamp = System.currentTimeMillis()
        )
    }

    // Session Events
    data class AppLaunch(
        val installDate: Long,
        val lastPlayDate: Long
    ) : GameEvent(
        "app_launch",
        mapOf(
            "install_date" to installDate,
            "last_play_date" to lastPlayDate
        )
    )

    data class SessionStart(
        val sessionId: String,
        val cashAtStart: Double,
        val totalPlayTimeMinutes: Int
    ) : GameEvent(
        "session_start",
        mapOf(
            "session_id" to sessionId,
            "cash_at_start" to cashAtStart,
            "total_play_time" to totalPlayTimeMinutes
        )
    )

    data class SessionEnd(
        val sessionId: String,
        val durationSeconds: Long,
        val cashEarned: Double
    ) : GameEvent(
        "session_end",
        mapOf(
            "session_id" to sessionId,
            "duration_seconds" to durationSeconds,
            "cash_earned" to cashEarned
        )
    )

    // Progression Events
    data class ZoneUnlocked(
        val zoneId: Int,
        val zoneName: String,
        val cashSpent: Double,
        val daysSinceInstall: Int
    ) : GameEvent(
        "zone_unlocked",
        mapOf(
            "zone_id" to zoneId,
            "zone_name" to zoneName,
            "cash_spent" to cashSpent,
            "days_since_install" to daysSinceInstall
        )
    )

    data class HelperHired(
        val stallId: Int,
        val helperCount: Int,
        val totalCashSpent: Double,
        val cashAvailable: Double
    ) : GameEvent(
        "helper_hired",
        mapOf(
            "stall_id" to stallId,
            "helper_count" to helperCount,
            "total_cash_spent" to totalCashSpent,
            "cash_available" to cashAvailable
        )
    )

    data class UpgradeCompleted(
        val stallId: Int,
        val upgradeLevel: Int,
        val incomeBoost: Double,
        val cashBeforeUpgrade: Double
    ) : GameEvent(
        "upgrade_completed",
        mapOf(
            "stall_id" to stallId,
            "upgrade_level" to upgradeLevel,
            "income_boost" to incomeBoost,
            "cash_before_upgrade" to cashBeforeUpgrade
        )
    )

    // Engagement Events
    data class OfflineEarningsClaimed(
        val offlineMinutes: Long,
        val earningsGenerated: Double
    ) : GameEvent(
        "offline_earnings_claimed",
        mapOf(
            "offline_minutes" to offlineMinutes,
            "earnings_generated" to earningsGenerated
        )
    )

    data class FamilyMilestone(
        val milestone: String, // "married", "child_born", etc.
        val totalCashEarned: Double
    ) : GameEvent(
        "family_milestone",
        mapOf(
            "milestone" to milestone,
            "total_cash_earned" to totalCashEarned
        )
    )

    data class CharacterHired(
        val characterType: String, // Chef, Manager, Staff, Specialist
        val totalCharacters: Int,
        val cost: Double
    ) : GameEvent(
        "character_hired",
        mapOf(
            "character_type" to characterType,
            "total_characters" to totalCharacters,
            "cost" to cost
        )
    )

    // Tutorial Events
    data class TutorialStarted(
        val tutorialStep: String
    ) : GameEvent(
        "tutorial_started",
        mapOf("tutorial_step" to tutorialStep)
    )

    data class TutorialStepCompleted(
        val tutorialStep: String,
        val timeSpentSeconds: Long
    ) : GameEvent(
        "tutorial_step_completed",
        mapOf(
            "tutorial_step" to tutorialStep,
            "time_spent_seconds" to timeSpentSeconds
        )
    )

    data class TutorialCompleted(
        val totalDurationSeconds: Long,
        val stepsCompleted: Int
    ) : GameEvent(
        "tutorial_completed",
        mapOf(
            "total_duration_seconds" to totalDurationSeconds,
            "steps_completed" to stepsCompleted
        )
    )

    data class TutorialSkipped(
        val atStep: String
    ) : GameEvent(
        "tutorial_skipped",
        mapOf("at_step" to atStep)
    )

    // Monetization Events
    data class IAPStarted(
        val productId: String,
        val productName: String,
        val price: Double
    ) : GameEvent(
        "iap_started",
        mapOf(
            "product_id" to productId,
            "product_name" to productName,
            "price" to price
        )
    )

    data class IAPPurchased(
        val productId: String,
        val revenue: Double,
        val currencyCode: String
    ) : GameEvent(
        "iap_purchased",
        mapOf(
            "product_id" to productId,
            "revenue" to revenue,
            "currency_code" to currencyCode
        )
    )

    data class AdRequested(
        val adType: String,
        val adNetwork: String
    ) : GameEvent(
        "ad_requested",
        mapOf(
            "ad_type" to adType,
            "ad_network" to adNetwork
        )
    )

    data class AdRewarded(
        val adType: String,
        val rewardType: String,
        val durationSeconds: Int
    ) : GameEvent(
        "ad_rewarded",
        mapOf(
            "ad_type" to adType,
            "reward_type" to rewardType,
            "duration_seconds" to durationSeconds
        )
    )

    // Error Events
    data class AppCrashed(
        val exception: String,
        val memoryUsed: Long,
        val cashAtCrash: Double
    ) : GameEvent(
        "app_crashed",
        mapOf(
            "exception" to exception,
            "memory_used" to memoryUsed,
            "cash_at_crash" to cashAtCrash
        )
    )

    data class AutoSaveFailed(
        val error: String?
    ) : GameEvent(
        "auto_save_failed",
        mapOf("error" to (error ?: "unknown"))
    )

    data class SaveRecovered(
        val version: Int
    ) : GameEvent(
        "save_recovered",
        mapOf("version" to version)
    )

    data class RecoveryFailed(
        val error: String?
    ) : GameEvent(
        "recovery_failed",
        mapOf("error" to (error ?: "unknown"))
    )

    // User Actions
    data class TapAction(
        val count: Int,
        val cashEarned: Double
    ) : GameEvent(
        "tap_action",
        mapOf(
            "count" to count,
            "cash_earned" to cashEarned
        )
    )

    data class SettingsChanged(
        val setting: String,
        val oldValue: Any,
        val newValue: Any
    ) : GameEvent(
        "settings_changed",
        mapOf(
            "setting" to setting,
            "old_value" to oldValue.toString(),
            "new_value" to newValue.toString()
        )
    )
}
