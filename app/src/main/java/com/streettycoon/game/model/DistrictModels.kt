package com.streettycoon.game.model

import com.google.gson.annotations.SerializedName

/**
 * District Models
 *
 * District unlock system with configurable unlock conditions, grid layouts,
 * and ambient settings that change customer demand patterns.
 */

// ==================== UNLOCK CONDITIONS ====================

/**
 * Types of condition that must be met to unlock a district.
 */
enum class UnlockConditionType {
    @SerializedName("MIN_CASH") MIN_CASH,
    @SerializedName("MIN_TOTAL_REVENUE") MIN_TOTAL_REVENUE,
    @SerializedName("MIN_BUSINESSES") MIN_BUSINESSES,
    @SerializedName("MIN_LEVEL") MIN_LEVEL,
    @SerializedName("TECH_NODE_RESEARCHED") TECH_NODE_RESEARCHED,
    @SerializedName("DISTRICT_CLEARED") DISTRICT_CLEARED     // previous district fully developed
}

/**
 * A single condition for district unlocking.
 */
data class UnlockCondition(
    @SerializedName("type") val type: UnlockConditionType,
    @SerializedName("value") val value: Double,       // numeric threshold or 1.0 for boolean
    @SerializedName("targetId") val targetId: String = ""  // optional: nodeId or districtId
) {
    /**
     * Evaluate whether the condition is satisfied given current game metrics.
     *
     * @param cash Current player cash balance.
     * @param totalRevenue Lifetime total revenue earned.
     * @param businessCount Active businesses across all districts.
     * @param playerLevel Current player level.
     * @param researchedNodeIds Set of researched tech node IDs.
     * @param clearedDistrictIds Set of fully-developed district IDs.
     */
    fun isMet(
        cash: Double,
        totalRevenue: Double,
        businessCount: Int,
        playerLevel: Int,
        researchedNodeIds: Set<String>,
        clearedDistrictIds: Set<String>
    ): Boolean = when (type) {
        UnlockConditionType.MIN_CASH -> cash >= value
        UnlockConditionType.MIN_TOTAL_REVENUE -> totalRevenue >= value
        UnlockConditionType.MIN_BUSINESSES -> businessCount >= value.toInt()
        UnlockConditionType.MIN_LEVEL -> playerLevel >= value.toInt()
        UnlockConditionType.TECH_NODE_RESEARCHED -> targetId in researchedNodeIds
        UnlockConditionType.DISTRICT_CLEARED -> targetId in clearedDistrictIds
    }
}

// ==================== DISTRICT THEME ====================

/**
 * Visual and gameplay theme of a district.
 */
enum class DistrictTheme {
    @SerializedName("BAZAAR") BAZAAR,           // Traditional market – high food demand
    @SerializedName("TECH_HUB") TECH_HUB,      // Innovation district – electronics/services
    @SerializedName("ARTS_QUARTER") ARTS_QUARTER,  // Creative zone – boutiques, cafes, music
    @SerializedName("WATERFRONT") WATERFRONT,  // Tourist hotspot – hospitality, food, clubs
    @SerializedName("BUSINESS_DISTRICT") BUSINESS_DISTRICT  // Professional area – services, food
}

/**
 * Base demand multipliers for each district theme.
 */
fun districtThemeMultipliers(theme: DistrictTheme): Map<CustomerSegment, Float> = when (theme) {
    DistrictTheme.BAZAAR -> mapOf(
        CustomerSegment.COMMUTER to 1.5f,
        CustomerSegment.LOCAL_RESIDENT to 1.4f,
        CustomerSegment.TOURIST to 1.2f
    )
    DistrictTheme.TECH_HUB -> mapOf(
        CustomerSegment.PROFESSIONAL to 1.6f,
        CustomerSegment.YOUTH to 1.3f
    )
    DistrictTheme.ARTS_QUARTER -> mapOf(
        CustomerSegment.YOUTH to 1.5f,
        CustomerSegment.TOURIST to 1.4f,
        CustomerSegment.PROFESSIONAL to 1.1f
    )
    DistrictTheme.WATERFRONT -> mapOf(
        CustomerSegment.TOURIST to 2.0f,
        CustomerSegment.FAMILY to 1.5f
    )
    DistrictTheme.BUSINESS_DISTRICT -> mapOf(
        CustomerSegment.PROFESSIONAL to 1.8f,
        CustomerSegment.COMMUTER to 1.5f
    )
}

// ==================== DISTRICT ====================

/**
 * A street district that can be unlocked and developed.
 */
data class District(
    @SerializedName("districtId") val districtId: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("theme") val theme: DistrictTheme,
    @SerializedName("unlockCost") val unlockCost: Double,
    @SerializedName("unlockConditions") val unlockConditions: List<UnlockCondition>,
    @SerializedName("grid") val grid: StreetGrid,
    @SerializedName("isUnlocked") var isUnlocked: Boolean = false,
    @SerializedName("unlockTimestamp") var unlockTimestamp: Long = 0L,
    @SerializedName("sortOrder") val sortOrder: Int = 0
) {
    /** Whether all unlock conditions are satisfied. */
    fun allConditionsMet(
        cash: Double,
        totalRevenue: Double,
        businessCount: Int,
        playerLevel: Int,
        researchedNodeIds: Set<String>,
        clearedDistrictIds: Set<String>
    ): Boolean = unlockConditions.all {
        it.isMet(cash, totalRevenue, businessCount, playerLevel, researchedNodeIds, clearedDistrictIds)
    }

    /** Number of active businesses in this district. */
    fun activeBusinessCount(): Int = grid.activePlots().size

    /** Whether this district is fully developed (all plots owned and occupied). */
    fun isFullyDeveloped(): Boolean {
        val owned = grid.ownedPlots()
        return owned.isNotEmpty() && owned.all { it.isOccupied }
    }

    /** Demand multiplier for a customer segment based on district theme. */
    fun demandMultiplier(segment: CustomerSegment): Float =
        districtThemeMultipliers(theme)[segment] ?: 1.0f
}

// ==================== DEFAULT DISTRICTS ====================

/**
 * Build the starting set of districts for a new game.
 */
fun buildDefaultDistricts(): List<District> = listOf(
    District(
        districtId = "district_1",
        name = "The Bazaar",
        description = "The heart of the street economy – a bustling traditional market full of food stalls and local traders.",
        theme = DistrictTheme.BAZAAR,
        unlockCost = 0.0,
        unlockConditions = emptyList(),  // Starting district – always unlocked
        grid = StreetGrid(
            districtId = "district_1",
            columns = 8,
            rows = 6
        ),
        isUnlocked = true,
        sortOrder = 1
    ),
    District(
        districtId = "district_2",
        name = "Arts Quarter",
        description = "A trendy creative district attracting youth culture, boutiques, and independent music venues.",
        theme = DistrictTheme.ARTS_QUARTER,
        unlockCost = 5000.0,
        unlockConditions = listOf(
            UnlockCondition(UnlockConditionType.MIN_TOTAL_REVENUE, 10000.0),
            UnlockCondition(UnlockConditionType.MIN_BUSINESSES, 3),
            UnlockCondition(UnlockConditionType.TECH_NODE_RESEARCHED, 1.0, "exp_second_district")
        ),
        grid = StreetGrid(
            districtId = "district_2",
            columns = 10,
            rows = 8
        ),
        sortOrder = 2
    ),
    District(
        districtId = "district_3",
        name = "The Waterfront",
        description = "A premium riverside promenade drawing tourists, upscale clubs, and high-end dining.",
        theme = DistrictTheme.WATERFRONT,
        unlockCost = 25000.0,
        unlockConditions = listOf(
            UnlockCondition(UnlockConditionType.MIN_TOTAL_REVENUE, 100000.0),
            UnlockCondition(UnlockConditionType.MIN_BUSINESSES, 10),
            UnlockCondition(UnlockConditionType.MIN_LEVEL, 15.0),
            UnlockCondition(UnlockConditionType.TECH_NODE_RESEARCHED, 1.0, "exp_third_district"),
            UnlockCondition(UnlockConditionType.DISTRICT_CLEARED, 1.0, "district_2")
        ),
        grid = StreetGrid(
            districtId = "district_3",
            columns = 12,
            rows = 10
        ),
        sortOrder = 3
    )
)
