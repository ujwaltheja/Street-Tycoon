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
 * Gate types for map progression
 */
enum class GateType {
    @SerializedName("UPGRADES_COMPLETED") UPGRADES_COMPLETED,
    @SerializedName("HELPERS_HIRED") HELPERS_HIRED,
    @SerializedName("EARNINGS_THRESHOLD") EARNINGS_THRESHOLD,
    @SerializedName("PLAYTIME_HOURS") PLAYTIME_HOURS
}

/**
 * Map gate data for progression tracking
 */
data class MapGate(
    val type: GateType,
    val targetValue: Int,
    val currentValue: Int = 0,
    val isCompleted: Boolean = false,
    val description: String
) {
    /**
     * Progress percentage (0.0 to 1.0)
     */
    val progress: Float
        get() = if (targetValue == 0) 0f else (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)

    /**
     * Progress percentage for display (0-100)
     */
    val progressPercent: Int
        get() = (progress * 100).toInt()
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
    val unlockCost: Double,
    val gates: List<MapGate> = emptyList()
) {
    /**
     * Check if all gates are completed
     */
    fun allGatesComplete(): Boolean = gates.isEmpty() || gates.all { it.isCompleted }

    /**
     * Get number of completed gates
     */
    fun getCompletedGatesCount(): Int = gates.count { it.isCompleted }

    /**
     * Get progress ratio (0.0 to 1.0)
     */
    fun getProgressRatio(): Float {
        if (gates.isEmpty()) return 1f
        return getCompletedGatesCount().toFloat() / gates.size
    }
}

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
    val stalls: List<Stall>,
    // Progression tracking
    val totalUpgradesCompleted: Int = 0,
    val totalHelpersHired: Int = 0,
    val totalPlaytimeSeconds: Long = 0,
    val gameStartTimestamp: Long = 0
) {
    fun findStall(stallId: Int): Stall? = stalls.find { it.id == stallId }
    fun findZone(zoneId: Int): Zone? = zones.find { it.id == zoneId }

    fun getTotalIncomePerSecond(): Double {
        return stalls.filter { it.isUnlocked }.sumOf { it.getTotalIncomePerSecond() }
    }

    /**
     * Get playtime in hours
     */
    fun getPlaytimeHours(): Long = totalPlaytimeSeconds / 3600
}

/**
 * Action result
 */
data class ActionResult(
    val success: Boolean,
    val message: String
)
