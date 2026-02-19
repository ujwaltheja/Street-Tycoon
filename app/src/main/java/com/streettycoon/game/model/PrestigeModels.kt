package com.streettycoon.game.model

import com.google.gson.annotations.SerializedName

/**
 * Prestige System
 *
 * End-game replayability mechanics. After reaching the prestige threshold
 * the player can "prestige" – resetting progression in exchange for
 * permanent multipliers and cosmetic unlocks that carry across runs.
 */

// ==================== PRESTIGE BONUSES ====================

/**
 * Types of permanent bonus granted by prestiging.
 */
enum class PrestigeBonusType {
    @SerializedName("INCOME_MULTIPLIER") INCOME_MULTIPLIER,
    @SerializedName("STARTING_CASH") STARTING_CASH,
    @SerializedName("XP_MULTIPLIER") XP_MULTIPLIER,
    @SerializedName("LOAN_LIMIT_INCREASE") LOAN_LIMIT_INCREASE,
    @SerializedName("UNLOCK_COSMETIC") UNLOCK_COSMETIC,
    @SerializedName("UNLOCK_BUSINESS_TYPE") UNLOCK_BUSINESS_TYPE,
    @SerializedName("REDUCED_UNLOCK_COST") REDUCED_UNLOCK_COST
}

/**
 * A single permanent bonus carried across prestige resets.
 */
data class PrestigeBonus(
    @SerializedName("type") val type: PrestigeBonusType,
    @SerializedName("value") val value: Double,
    @SerializedName("description") val description: String,
    @SerializedName("targetId") val targetId: String = ""
)

// ==================== PRESTIGE TIER ====================

/**
 * Definition for a single prestige tier.
 */
data class PrestigeTier(
    @SerializedName("tier") val tier: Int,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("requirement") val requirement: PrestigeRequirement,
    @SerializedName("bonuses") val bonuses: List<PrestigeBonus>,
    @SerializedName("badgeEmoji") val badgeEmoji: String,
    @SerializedName("description") val description: String
)

/**
 * Condition that must be met to unlock a prestige tier.
 */
data class PrestigeRequirement(
    @SerializedName("minTotalRevenue") val minTotalRevenue: Double,
    @SerializedName("minLevel") val minLevel: Int,
    @SerializedName("minBusinessCount") val minBusinessCount: Int,
    @SerializedName("allDistrictsUnlocked") val allDistrictsUnlocked: Boolean = false
) {
    fun isMet(
        totalRevenue: Double,
        level: Int,
        businessCount: Int,
        allDistrictsUnlocked: Boolean
    ): Boolean =
        totalRevenue >= minTotalRevenue &&
            level >= minLevel &&
            businessCount >= minBusinessCount &&
            (!this.allDistrictsUnlocked || allDistrictsUnlocked)
}

// ==================== PRESTIGE STATE ====================

/**
 * Player's prestige progress across all runs.
 */
data class PrestigeState(
    @SerializedName("currentTier") val currentTier: Int = 0,
    @SerializedName("totalPrestiges") val totalPrestiges: Int = 0,
    @SerializedName("earnedBonuses") val earnedBonuses: List<PrestigeBonus> = emptyList(),
    @SerializedName("lifetimeRevenue") val lifetimeRevenue: Double = 0.0,
    @SerializedName("unlockedCosmetics") val unlockedCosmetics: List<String> = emptyList()
) {
    /**
     * Aggregate income multiplier from all earned bonuses.
     */
    fun incomeMultiplier(): Double =
        earnedBonuses
            .filter { it.type == PrestigeBonusType.INCOME_MULTIPLIER }
            .fold(1.0) { acc, b -> acc * b.value }

    /**
     * Starting cash bonus for the next run.
     */
    fun startingCashBonus(): Double =
        earnedBonuses
            .filter { it.type == PrestigeBonusType.STARTING_CASH }
            .sumOf { it.value }

    /**
     * XP multiplier from prestige bonuses.
     */
    fun xpMultiplier(): Double =
        earnedBonuses
            .filter { it.type == PrestigeBonusType.XP_MULTIPLIER }
            .fold(1.0) { acc, b -> acc * b.value }

    /**
     * Additional loan slots granted through prestige.
     */
    fun extraLoanSlots(): Int =
        earnedBonuses
            .filter { it.type == PrestigeBonusType.LOAN_LIMIT_INCREASE }
            .sumOf { it.value.toInt() }

    /**
     * Discount on district unlock costs (0.0–1.0 fraction off).
     */
    fun districtUnlockDiscount(): Double =
        earnedBonuses
            .filter { it.type == PrestigeBonusType.REDUCED_UNLOCK_COST }
            .sumOf { it.value }
            .coerceAtMost(0.5) // max 50% discount

    /**
     * Check whether the player qualifies for the next prestige tier.
     */
    fun canPrestige(
        tiers: List<PrestigeTier>,
        totalRevenue: Double,
        level: Int,
        businessCount: Int,
        allDistrictsUnlocked: Boolean
    ): Boolean {
        val nextTier = tiers.find { it.tier == currentTier + 1 } ?: return false
        return nextTier.requirement.isMet(totalRevenue, level, businessCount, allDistrictsUnlocked)
    }
}

// ==================== DEFAULT PRESTIGE TIERS ====================

/**
 * Build the default set of prestige tiers.
 */
fun buildPrestigeTiers(): List<PrestigeTier> = listOf(
    PrestigeTier(
        tier = 1,
        displayName = "Street Hustler",
        badgeEmoji = "🥉",
        description = "Your first prestige. You've proven you can build an empire from nothing.",
        requirement = PrestigeRequirement(
            minTotalRevenue = 50000.0,
            minLevel = 10,
            minBusinessCount = 5
        ),
        bonuses = listOf(
            PrestigeBonus(
                type = PrestigeBonusType.INCOME_MULTIPLIER,
                value = 1.1,
                description = "+10% income multiplier (permanent)"
            ),
            PrestigeBonus(
                type = PrestigeBonusType.STARTING_CASH,
                value = 500.0,
                description = "Start each new run with an extra ₹500"
            )
        )
    ),
    PrestigeTier(
        tier = 2,
        displayName = "Block Boss",
        badgeEmoji = "🥈",
        description = "You control the block. Your reputation precedes you.",
        requirement = PrestigeRequirement(
            minTotalRevenue = 200000.0,
            minLevel = 20,
            minBusinessCount = 10
        ),
        bonuses = listOf(
            PrestigeBonus(
                type = PrestigeBonusType.INCOME_MULTIPLIER,
                value = 1.15,
                description = "+15% income multiplier (permanent)"
            ),
            PrestigeBonus(
                type = PrestigeBonusType.XP_MULTIPLIER,
                value = 1.2,
                description = "+20% XP gain (permanent)"
            ),
            PrestigeBonus(
                type = PrestigeBonusType.LOAN_LIMIT_INCREASE,
                value = 1.0,
                description = "+1 simultaneous loan slot"
            )
        )
    ),
    PrestigeTier(
        tier = 3,
        displayName = "District Mogul",
        badgeEmoji = "🥇",
        description = "Multiple districts bow to your empire. A true city legend.",
        requirement = PrestigeRequirement(
            minTotalRevenue = 1000000.0,
            minLevel = 30,
            minBusinessCount = 20,
            allDistrictsUnlocked = true
        ),
        bonuses = listOf(
            PrestigeBonus(
                type = PrestigeBonusType.INCOME_MULTIPLIER,
                value = 1.25,
                description = "+25% income multiplier (permanent)"
            ),
            PrestigeBonus(
                type = PrestigeBonusType.STARTING_CASH,
                value = 5000.0,
                description = "Start each run with ₹5,000 bonus"
            ),
            PrestigeBonus(
                type = PrestigeBonusType.REDUCED_UNLOCK_COST,
                value = 0.2,
                description = "20% discount on all district unlock costs"
            ),
            PrestigeBonus(
                type = PrestigeBonusType.UNLOCK_COSMETIC,
                value = 1.0,
                targetId = "golden_stall_skin",
                description = "Unlock exclusive Golden Stall skin"
            )
        )
    )
)
