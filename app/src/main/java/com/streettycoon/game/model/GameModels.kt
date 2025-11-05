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
 * Character types for staff system
 */
enum class CharacterType {
    @SerializedName("CHEF") CHEF,
    @SerializedName("MANAGER") MANAGER,
    @SerializedName("STAFF") STAFF,
    @SerializedName("SPECIALIST") SPECIALIST
}

/**
 * Character stats based on type
 */
data class CharacterStats(
    val type: CharacterType,
    val baseCost: Int,
    val incomeMultiplier: Float,
    val tapIncomeBonus: Float,
    val upgradeCostReduction: Float,
    val maxLevel: Int
) {
    companion object {
        fun getStatsForType(type: CharacterType): CharacterStats {
            return when (type) {
                CharacterType.CHEF -> CharacterStats(
                    type = CharacterType.CHEF,
                    baseCost = 1500,
                    incomeMultiplier = 1.5f,
                    tapIncomeBonus = 0.5f,
                    upgradeCostReduction = 0.0f,
                    maxLevel = 5
                )
                CharacterType.MANAGER -> CharacterStats(
                    type = CharacterType.MANAGER,
                    baseCost = 2500,
                    incomeMultiplier = 1.3f,
                    tapIncomeBonus = 0.0f,
                    upgradeCostReduction = 0.2f,
                    maxLevel = 5
                )
                CharacterType.STAFF -> CharacterStats(
                    type = CharacterType.STAFF,
                    baseCost = 800,
                    incomeMultiplier = 1.4f,
                    tapIncomeBonus = 0.0f,
                    upgradeCostReduction = 0.0f,
                    maxLevel = 3
                )
                CharacterType.SPECIALIST -> CharacterStats(
                    type = CharacterType.SPECIALIST,
                    baseCost = 3500,
                    incomeMultiplier = 1.6f,
                    tapIncomeBonus = 0.3f,
                    upgradeCostReduction = 0.1f,
                    maxLevel = 5
                )
            }
        }
    }
}

/**
 * Character data (named, levelable staff)
 */
data class Character(
    val characterId: String,
    val type: CharacterType,
    val name: String,
    val level: Int,
    val experience: Int,
    val productivityMultiplier: Float,
    val assignedStallId: Int,
    val isUnlocked: Boolean
) {
    /**
     * Get effective income bonus
     */
    fun getEffectiveIncomeBonus(): Double {
        val stats = CharacterStats.getStatsForType(type)
        return (stats.incomeMultiplier * productivityMultiplier).toDouble()
    }

    /**
     * Get effective tap bonus
     */
    fun getEffectiveTapBonus(): Double {
        val stats = CharacterStats.getStatsForType(type)
        return (stats.tapIncomeBonus * productivityMultiplier).toDouble()
    }

    /**
     * Get effective upgrade cost reduction
     */
    fun getEffectiveUpgradeCostReduction(): Double {
        val stats = CharacterStats.getStatsForType(type)
        return stats.upgradeCostReduction.toDouble()
    }

    /**
     * Check if character can level up
     */
    fun canLevelUp(): Boolean {
        val stats = CharacterStats.getStatsForType(type)
        val xpRequired = level * 100
        return level < stats.maxLevel && experience >= xpRequired
    }

    /**
     * Get XP required for next level
     */
    fun getXpRequired(): Int = level * 100

    /**
     * Get XP progress (0.0 to 1.0)
     */
    fun getXpProgress(): Float {
        val required = getXpRequired()
        return if (required == 0) 1f else (experience.toFloat() / required).coerceIn(0f, 1f)
    }

    /**
     * Get hire cost for this character type
     */
    fun getHireCost(): Int {
        val stats = CharacterStats.getStatsForType(type)
        return stats.baseCost
    }
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
 * Family member in the game
 */
data class FamilyMember(
    val memberId: String,
    val name: String,
    val relation: String,  // "player", "spouse", "child", "parent"
    val age: Int,
    val monthlyExpense: Double,
    val happiness: Float  // 0-100
)

/**
 * Spending category (housing, transport, food, education, health)
 */
data class SpendingCategory(
    val categoryId: String,
    val name: String,
    val type: String,  // "housing", "transport", "food", "education", "health"
    val monthlyExpense: Double,
    val level: Int,  // 0-3
    val nextUpgradeCost: Double,
    val currentItem: String  // e.g., "Small Room", "Bicycle", etc.
) {
    /**
     * Check if category can be upgraded
     */
    fun canUpgrade(): Boolean = level < 3

    /**
     * Get emoji icon for category type
     */
    fun getEmoji(): String {
        return when (type) {
            "housing" -> "🏠"
            "transport" -> "🚗"
            "food" -> "🍽️"
            "education" -> "📚"
            "health" -> "🏥"
            else -> "❓"
        }
    }

    /**
     * Get color for category level
     */
    fun getLevelColor(): androidx.compose.ui.graphics.Color {
        return when (level) {
            0 -> androidx.compose.ui.graphics.Color(0xFFBDBDBD)  // Gray
            1 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)  // Green
            2 -> androidx.compose.ui.graphics.Color(0xFF2196F3)  // Blue
            3 -> androidx.compose.ui.graphics.Color(0xFF9C27B0)  // Purple
            else -> androidx.compose.ui.graphics.Color.Gray
        }
    }
}

/**
 * Family state containing members and spending categories
 */
data class FamilyState(
    val members: List<FamilyMember> = emptyList(),
    val categories: List<SpendingCategory> = emptyList(),
    val totalMonthlyExpense: Double = 0.0,
    val averageHappiness: Float = 100f,
    val savingsBalance: Double = 0.0,
    val lastMonthlyDeductionTimestamp: Long = 0,
    val isMarried: Boolean = false,
    val totalChildren: Int = 0
) {
    /**
     * Get financial health score (0-100)
     */
    fun getFinancialHealthScore(monthlyIncome: Double): Float {
        if (monthlyIncome < 0.01) return 50f

        val expenseRatio = (totalMonthlyExpense / monthlyIncome).toFloat()

        // Optimal range is 5-20% of income spent on family
        return when {
            expenseRatio < 0.05f -> 30f  // Too little spending
            expenseRatio > 0.20f -> 40f  // Too much spending
            else -> 100f  // Healthy balance
        }
    }

    /**
     * Get expense ratio as percentage
     */
    fun getExpenseRatio(monthlyIncome: Double): Int {
        if (monthlyIncome < 0.01) return 0
        return ((totalMonthlyExpense / monthlyIncome) * 100).toInt()
    }

    /**
     * Find category by ID
     */
    fun findCategory(categoryId: String): SpendingCategory? {
        return categories.find { it.categoryId == categoryId }
    }

    /**
     * Get number of family members
     */
    fun getMemberCount(): Int = members.size
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
    val characters: List<Character> = emptyList(),
    val familyState: FamilyState = FamilyState(),
    // Progression tracking
    val totalUpgradesCompleted: Int = 0,
    val totalHelpersHired: Int = 0,
    val totalPlaytimeSeconds: Long = 0,
    val gameStartTimestamp: Long = 0
) {
    fun findStall(stallId: Int): Stall? = stalls.find { it.id == stallId }
    fun findZone(zoneId: Int): Zone? = zones.find { it.id == zoneId }
    fun findCharacter(characterId: String): Character? = characters.find { it.characterId == characterId }

    fun getTotalIncomePerSecond(): Double {
        return stalls.filter { it.isUnlocked }.sumOf { it.getTotalIncomePerSecond() }
    }

    /**
     * Get playtime in hours
     */
    fun getPlaytimeHours(): Long = totalPlaytimeSeconds / 3600

    /**
     * Get characters assigned to a specific stall
     */
    fun getCharactersForStall(stallId: Int): List<Character> {
        return characters.filter { it.assignedStallId == stallId && it.isUnlocked }
    }

    /**
     * Get all unlocked characters
     */
    fun getUnlockedCharacters(): List<Character> {
        return characters.filter { it.isUnlocked }
    }

    /**
     * Get estimated monthly income (passive income × 30 days)
     */
    fun getMonthlyIncomeEstimate(): Double {
        val totalIncomePerSecond = getTotalIncomePerSecond()
        // Convert to monthly (30 days × 24 hours × 60 minutes × 60 seconds)
        return totalIncomePerSecond * 30 * 24 * 60 * 60
    }
}

/**
 * Action result
 */
data class ActionResult(
    val success: Boolean,
    val message: String
)
