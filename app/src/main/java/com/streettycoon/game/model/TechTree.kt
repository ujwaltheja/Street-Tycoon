package com.streettycoon.game.model

import com.google.gson.annotations.SerializedName

/**
 * Tech Tree
 *
 * Upgrades organized into a directed acyclic graph. Each node has
 * prerequisites and confers bonuses to specific game systems.
 */

// ==================== TECH NODE ====================

/**
 * Broad categories of tech tree upgrades.
 */
enum class TechCategory {
    @SerializedName("OPERATIONS") OPERATIONS,         // supply chain, efficiency
    @SerializedName("CUSTOMER_EXPERIENCE") CUSTOMER_EXPERIENCE,
    @SerializedName("MARKETING") MARKETING,
    @SerializedName("STAFF") STAFF,
    @SerializedName("FINANCE") FINANCE,
    @SerializedName("EXPANSION") EXPANSION
}

/**
 * The type of bonus a node provides.
 */
enum class TechBonusType {
    @SerializedName("REVENUE_MULTIPLIER") REVENUE_MULTIPLIER,
    @SerializedName("EXPENSE_REDUCTION") EXPENSE_REDUCTION,
    @SerializedName("CUSTOMER_CAPACITY") CUSTOMER_CAPACITY,
    @SerializedName("STAFF_MORALE") STAFF_MORALE,
    @SerializedName("SUPPLY_COST_REDUCTION") SUPPLY_COST_REDUCTION,
    @SerializedName("REPUTATION_GAIN") REPUTATION_GAIN,
    @SerializedName("UNLOCK_BUSINESS_TYPE") UNLOCK_BUSINESS_TYPE,
    @SerializedName("UNLOCK_DISTRICT") UNLOCK_DISTRICT,
    @SerializedName("XP_MULTIPLIER") XP_MULTIPLIER,
    @SerializedName("LOAN_INTEREST_REDUCTION") LOAN_INTEREST_REDUCTION
}

/**
 * A single bonus granted by a tech node.
 */
data class TechBonus(
    @SerializedName("type") val type: TechBonusType,
    @SerializedName("value") val value: Double,              // Amount (multiplier or flat delta)
    @SerializedName("targetId") val targetId: String = ""   // Optional: businessType or districtId
)

/**
 * A single node in the tech tree.
 */
data class TechNode(
    @SerializedName("nodeId") val nodeId: String,
    @SerializedName("category") val category: TechCategory,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("description") val description: String,
    @SerializedName("cost") val cost: Double,
    @SerializedName("prerequisites") val prerequisites: List<String>,  // nodeIds
    @SerializedName("bonuses") val bonuses: List<TechBonus>,
    @SerializedName("isResearched") var isResearched: Boolean = false
) {
    /** Whether all prerequisites have been researched. */
    fun isAvailable(researchedIds: Set<String>): Boolean =
        !isResearched && prerequisites.all { it in researchedIds }
}

// ==================== TECH TREE STATE ====================

/**
 * Full tech tree state for a player.
 */
data class TechTreeState(
    @SerializedName("nodes") val nodes: List<TechNode> = buildDefaultTree(),
    @SerializedName("totalSpent") val totalSpent: Double = 0.0
) {
    /** All researched node IDs. */
    val researchedIds: Set<String> get() = nodes.filter { it.isResearched }.map { it.nodeId }.toSet()

    /** Nodes available to research (prerequisites met, not yet researched). */
    fun availableNodes(): List<TechNode> =
        nodes.filter { it.isAvailable(researchedIds) }

    /** Find a node by ID. */
    fun find(nodeId: String): TechNode? = nodes.find { it.nodeId == nodeId }

    /**
     * Aggregate a specific bonus type across all researched nodes.
     * Multiplier bonuses are multiplied; additive bonuses are summed.
     */
    fun aggregateBonus(type: TechBonusType, targetId: String = ""): Double {
        val matching = nodes.filter { it.isResearched }.flatMap { it.bonuses }
            .filter { it.type == type && (it.targetId.isEmpty() || it.targetId == targetId) }

        return if (type == TechBonusType.REVENUE_MULTIPLIER ||
            type == TechBonusType.EXPENSE_REDUCTION ||
            type == TechBonusType.XP_MULTIPLIER
        ) {
            matching.fold(1.0) { acc, bonus -> acc * bonus.value }
        } else {
            matching.sumOf { it.value }
        }
    }

    /** List of unlocked business types from the tech tree. */
    fun unlockedBusinessTypes(): Set<String> =
        nodes.filter { it.isResearched }
            .flatMap { it.bonuses }
            .filter { it.type == TechBonusType.UNLOCK_BUSINESS_TYPE }
            .map { it.targetId }
            .toSet()
}

// ==================== DEFAULT TREE DEFINITION ====================

/**
 * Build the default tech tree structure.
 * Returns a fixed, balanced progression graph.
 */
fun buildDefaultTree(): List<TechNode> = listOf(

    // ---------- OPERATIONS ----------
    TechNode(
        nodeId = "ops_efficient_supply",
        category = TechCategory.OPERATIONS,
        displayName = "Efficient Supply",
        description = "Reduce supply costs across all businesses by 10%.",
        cost = 500.0,
        prerequisites = emptyList(),
        bonuses = listOf(TechBonus(TechBonusType.SUPPLY_COST_REDUCTION, 0.9))
    ),
    TechNode(
        nodeId = "ops_bulk_orders",
        category = TechCategory.OPERATIONS,
        displayName = "Bulk Orders",
        description = "Further reduce supply costs by 15% and increase max stock.",
        cost = 1200.0,
        prerequisites = listOf("ops_efficient_supply"),
        bonuses = listOf(TechBonus(TechBonusType.SUPPLY_COST_REDUCTION, 0.85))
    ),
    TechNode(
        nodeId = "ops_automation",
        category = TechCategory.OPERATIONS,
        displayName = "Automation",
        description = "Automated systems raise revenue by 20% in all businesses.",
        cost = 3000.0,
        prerequisites = listOf("ops_bulk_orders"),
        bonuses = listOf(TechBonus(TechBonusType.REVENUE_MULTIPLIER, 1.2))
    ),

    // ---------- CUSTOMER EXPERIENCE ----------
    TechNode(
        nodeId = "cx_loyalty_program",
        category = TechCategory.CUSTOMER_EXPERIENCE,
        displayName = "Loyalty Program",
        description = "Repeat customers spend 10% more per visit.",
        cost = 600.0,
        prerequisites = emptyList(),
        bonuses = listOf(TechBonus(TechBonusType.REVENUE_MULTIPLIER, 1.1))
    ),
    TechNode(
        nodeId = "cx_premium_service",
        category = TechCategory.CUSTOMER_EXPERIENCE,
        displayName = "Premium Service",
        description = "Faster service generates 15% more customers per hour.",
        cost = 1500.0,
        prerequisites = listOf("cx_loyalty_program"),
        bonuses = listOf(TechBonus(TechBonusType.CUSTOMER_CAPACITY, 1.15))
    ),
    TechNode(
        nodeId = "cx_vip_lounge",
        category = TechCategory.CUSTOMER_EXPERIENCE,
        displayName = "VIP Lounge",
        description = "High-value customers spend 30% more. Unlocks Club business type.",
        cost = 5000.0,
        prerequisites = listOf("cx_premium_service"),
        bonuses = listOf(
            TechBonus(TechBonusType.REVENUE_MULTIPLIER, 1.3),
            TechBonus(TechBonusType.UNLOCK_BUSINESS_TYPE, 1.0, BusinessType.CLUB.name)
        )
    ),

    // ---------- MARKETING ----------
    TechNode(
        nodeId = "mkt_street_signage",
        category = TechCategory.MARKETING,
        displayName = "Street Signage",
        description = "Eye-catching signs boost footfall by 10%.",
        cost = 400.0,
        prerequisites = emptyList(),
        bonuses = listOf(TechBonus(TechBonusType.CUSTOMER_CAPACITY, 1.1))
    ),
    TechNode(
        nodeId = "mkt_social_buzz",
        category = TechCategory.MARKETING,
        displayName = "Social Buzz",
        description = "Social media campaigns raise reputation by +5 and revenue by 10%.",
        cost = 1000.0,
        prerequisites = listOf("mkt_street_signage"),
        bonuses = listOf(
            TechBonus(TechBonusType.REPUTATION_GAIN, 5.0),
            TechBonus(TechBonusType.REVENUE_MULTIPLIER, 1.1)
        )
    ),
    TechNode(
        nodeId = "mkt_influencer",
        category = TechCategory.MARKETING,
        displayName = "Influencer Partnership",
        description = "Viral events bring 50% more tourists to your district.",
        cost = 4000.0,
        prerequisites = listOf("mkt_social_buzz"),
        bonuses = listOf(
            TechBonus(TechBonusType.CUSTOMER_CAPACITY, 1.5, CustomerSegment.TOURIST.name),
            TechBonus(TechBonusType.REVENUE_MULTIPLIER, 1.2)
        )
    ),

    // ---------- STAFF ----------
    TechNode(
        nodeId = "staff_onboarding",
        category = TechCategory.STAFF,
        displayName = "Structured Onboarding",
        description = "New staff start with higher morale (+10).",
        cost = 300.0,
        prerequisites = emptyList(),
        bonuses = listOf(TechBonus(TechBonusType.STAFF_MORALE, 10.0))
    ),
    TechNode(
        nodeId = "staff_incentives",
        category = TechCategory.STAFF,
        displayName = "Performance Incentives",
        description = "Bonus structure keeps morale high, raising productivity 15%.",
        cost = 800.0,
        prerequisites = listOf("staff_onboarding"),
        bonuses = listOf(
            TechBonus(TechBonusType.STAFF_MORALE, 15.0),
            TechBonus(TechBonusType.REVENUE_MULTIPLIER, 1.15)
        )
    ),
    TechNode(
        nodeId = "staff_management_school",
        category = TechCategory.STAFF,
        displayName = "Management School",
        description = "Managers train twice as fast and boost overall revenue by 20%.",
        cost = 2500.0,
        prerequisites = listOf("staff_incentives"),
        bonuses = listOf(TechBonus(TechBonusType.REVENUE_MULTIPLIER, 1.2))
    ),

    // ---------- FINANCE ----------
    TechNode(
        nodeId = "fin_credit_history",
        category = TechCategory.FINANCE,
        displayName = "Good Credit",
        description = "Reduce loan interest rates by 2%.",
        cost = 500.0,
        prerequisites = emptyList(),
        bonuses = listOf(TechBonus(TechBonusType.LOAN_INTEREST_REDUCTION, 0.02))
    ),
    TechNode(
        nodeId = "fin_investor_relations",
        category = TechCategory.FINANCE,
        displayName = "Investor Relations",
        description = "Access to better loan terms: reduce interest by a further 3%.",
        cost = 2000.0,
        prerequisites = listOf("fin_credit_history"),
        bonuses = listOf(TechBonus(TechBonusType.LOAN_INTEREST_REDUCTION, 0.03))
    ),
    TechNode(
        nodeId = "fin_revenue_diversification",
        category = TechCategory.FINANCE,
        displayName = "Revenue Diversification",
        description = "Passive income from all assets increased by 25%.",
        cost = 4000.0,
        prerequisites = listOf("fin_investor_relations"),
        bonuses = listOf(TechBonus(TechBonusType.REVENUE_MULTIPLIER, 1.25))
    ),

    // ---------- EXPANSION ----------
    TechNode(
        nodeId = "exp_second_district",
        category = TechCategory.EXPANSION,
        displayName = "District Expansion",
        description = "Unlock the second street district.",
        cost = 5000.0,
        prerequisites = listOf("ops_efficient_supply", "cx_loyalty_program"),
        bonuses = listOf(TechBonus(TechBonusType.UNLOCK_DISTRICT, 1.0, "district_2"))
    ),
    TechNode(
        nodeId = "exp_entertainment_row",
        category = TechCategory.EXPANSION,
        displayName = "Entertainment Row",
        description = "Unlock Arcade and Music Venue business types.",
        cost = 7000.0,
        prerequisites = listOf("exp_second_district"),
        bonuses = listOf(
            TechBonus(TechBonusType.UNLOCK_BUSINESS_TYPE, 1.0, BusinessType.ARCADE.name),
            TechBonus(TechBonusType.UNLOCK_BUSINESS_TYPE, 1.0, BusinessType.MUSIC_VENUE.name)
        )
    ),
    TechNode(
        nodeId = "exp_third_district",
        category = TechCategory.EXPANSION,
        displayName = "Premium District",
        description = "Unlock the premium third district.",
        cost = 20000.0,
        prerequisites = listOf("exp_entertainment_row", "cx_vip_lounge"),
        bonuses = listOf(TechBonus(TechBonusType.UNLOCK_DISTRICT, 1.0, "district_3"))
    )
)
