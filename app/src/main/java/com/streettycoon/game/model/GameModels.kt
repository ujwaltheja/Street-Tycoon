package com.streettycoon.game.model

import com.google.gson.annotations.SerializedName

/**
 * Stall types matching C++ enum
 */
enum class StallType {
    @SerializedName("TEA") TEA,
    @SerializedName("DOSA") DOSA,
    @SerializedName("MOMOS") MOMOS,
    @SerializedName("JUICE") JUICE
}

/**
 * Helper data class
 */
data class Helper(
    val id: Int,
    val level: Int,
    val incomePerSecond: Double
)

/**
 * Stall data class
 */
data class Stall(
    val id: Int,
    val type: StallType,
    val level: Int,
    val zoneId: Int,
    val baseIncome: Double,
    val tapIncome: Double,
    val lastServedTimestamp: Long,
    val isUnlocked: Boolean,
    val helpers: List<Helper>
) {
    fun getTotalIncomePerSecond(): Double {
        val helperIncome = helpers.sumOf { it.incomePerSecond }
        return helperIncome * (1.0 + (level - 1) * 0.5)
    }

    fun getUpgradeCost(): Double {
        return baseIncome * 10.0 * Math.pow(1.15, level.toDouble())
    }

    fun getHelperCost(): Double {
        return baseIncome * 20.0 * Math.pow(1.3, helpers.size.toDouble())
    }

    fun getUnlockCost(): Double {
        return baseIncome * 5.0
    }
}

/**
 * Zone data class
 */
data class Zone(
    val id: Int,
    val name: String,
    val isUnlocked: Boolean,
    val unlockCost: Double
)

/**
 * Complete game state snapshot
 */
data class GameState(
    val version: Int,
    val playerCash: Double,
    val playerTokens: Int,
    val lastUpdateTimestamp: Long,
    val totalCustomersServed: Long,
    val totalEarnings: Double,
    val currentDay: Int,
    val lastDailyRewardTimestamp: Long,
    val zones: List<Zone>,
    val stalls: List<Stall>
) {
    fun findStall(stallId: Int): Stall? = stalls.find { it.id == stallId }
    fun findZone(zoneId: Int): Zone? = zones.find { it.id == zoneId }

    fun getTotalIncomePerSecond(): Double {
        return stalls.filter { it.isUnlocked }.sumOf { it.getTotalIncomePerSecond() }
    }
}

/**
 * Action result
 */
data class ActionResult(
    val success: Boolean,
    val message: String
)
