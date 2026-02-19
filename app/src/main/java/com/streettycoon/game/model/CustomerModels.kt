package com.streettycoon.game.model

import com.google.gson.annotations.SerializedName
import kotlin.random.Random

/**
 * Customer Models
 *
 * NPC customer simulation with schedules, budgets, preferences, and
 * pathfinding-ready demand curves that react to player choices.
 */

// ==================== CUSTOMER PROFILE ====================

/**
 * Archetypes that determine spending habits and business preferences.
 */
enum class CustomerArchetype {
    @SerializedName("BUDGET_SHOPPER") BUDGET_SHOPPER,
    @SerializedName("VALUE_SEEKER") VALUE_SEEKER,
    @SerializedName("TRENDSETTER") TRENDSETTER,
    @SerializedName("EXPERIENCE_HUNTER") EXPERIENCE_HUNTER,
    @SerializedName("CONVENIENCE_LOVER") CONVENIENCE_LOVER,
    @SerializedName("LOYAL_REGULAR") LOYAL_REGULAR
}

/**
 * Time windows when a customer is active in the street.
 */
data class ActivityWindow(
    @SerializedName("startHour") val startHour: Int,   // 0–23
    @SerializedName("endHour") val endHour: Int,       // 0–23
    @SerializedName("frequency") val frequency: Float  // visits per active hour
)

/**
 * A named NPC customer with fixed preferences and a daily schedule.
 */
data class CustomerProfile(
    @SerializedName("customerId") val customerId: String,
    @SerializedName("name") val name: String,
    @SerializedName("segment") val segment: CustomerSegment,
    @SerializedName("archetype") val archetype: CustomerArchetype,
    @SerializedName("dailyBudget") val dailyBudget: Double,
    @SerializedName("schedule") val schedule: List<ActivityWindow>,
    @SerializedName("preferredTypes") val preferredTypes: List<BusinessType>,
    @SerializedName("loyalBusinessIds") val loyalBusinessIds: List<String> = emptyList(),
    @SerializedName("reputationSensitivity") val reputationSensitivity: Float = 0.5f
) {
    /**
     * Maximum willingness to spend in a single transaction (varies by archetype).
     */
    val maxSpendPerVisit: Double get() = when (archetype) {
        CustomerArchetype.BUDGET_SHOPPER -> dailyBudget * 0.2
        CustomerArchetype.VALUE_SEEKER -> dailyBudget * 0.25
        CustomerArchetype.TRENDSETTER -> dailyBudget * 0.4
        CustomerArchetype.EXPERIENCE_HUNTER -> dailyBudget * 0.5
        CustomerArchetype.CONVENIENCE_LOVER -> dailyBudget * 0.3
        CustomerArchetype.LOYAL_REGULAR -> dailyBudget * 0.35
    }

    /**
     * Base attraction score for a given business type (0.0–1.0).
     * Loyal businesses always score 1.0.
     */
    fun attractionScore(businessId: String, type: BusinessType, reputation: Float): Float {
        if (businessId in loyalBusinessIds) return 1.0f
        val typePref = if (type in preferredTypes) 1.0f else 0.3f
        val repPref = reputation / 100f * reputationSensitivity + (1f - reputationSensitivity)
        return (typePref * repPref).coerceIn(0f, 1f)
    }

    /**
     * Whether this customer is active during the given hour.
     */
    fun isActiveAt(hour: Int): Boolean = schedule.any { win ->
        if (win.startHour <= win.endHour) hour in win.startHour..win.endHour
        else hour >= win.startHour || hour <= win.endHour
    }

    /**
     * Expected visits per hour at a given time.
     */
    fun expectedVisitsAt(hour: Int): Float =
        schedule.filter { win ->
            if (win.startHour <= win.endHour) hour in win.startHour..win.endHour
            else hour >= win.startHour || hour <= win.endHour
        }.sumOf { it.frequency.toDouble() }.toFloat()
}

// ==================== CUSTOMER VISIT ====================

/**
 * Result of a single customer visit to a business.
 */
data class CustomerVisit(
    @SerializedName("visitId") val visitId: String,
    @SerializedName("customerId") val customerId: String,
    @SerializedName("businessId") val businessId: String,
    @SerializedName("revenue") val revenue: Double,
    @SerializedName("satisfied") val satisfied: Boolean,
    @SerializedName("reputationDelta") val reputationDelta: Float,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis()
)

// ==================== DEMAND CURVES ====================

/**
 * Factors that shift demand up or down for a business.
 */
data class DemandFactors(
    @SerializedName("baseCustomersPerHour") val baseCustomersPerHour: Int,
    @SerializedName("reputationMultiplier") val reputationMultiplier: Float,  // 0.5–2.0
    @SerializedName("eventMultiplier") val eventMultiplier: Float,           // 0.0–3.0
    @SerializedName("staffMultiplier") val staffMultiplier: Float,           // 0.5–2.0
    @SerializedName("locationScore") val locationScore: Float,               // 0.0–1.0
    @SerializedName("supplyFactor") val supplyFactor: Float                  // 0.0–1.0
) {
    /**
     * Effective customers per hour after all factors.
     */
    fun effectiveCustomersPerHour(): Int =
        (baseCustomersPerHour *
            reputationMultiplier *
            eventMultiplier *
            staffMultiplier *
            locationScore *
            supplyFactor).toInt().coerceAtLeast(0)
}

// ==================== NPC SIMULATION ====================

/**
 * Runtime NPC state (transient – not persisted between sessions).
 */
data class ActiveNPC(
    val customerId: String,
    val currentX: Float,
    val currentY: Float,
    val targetBusinessId: String?,
    val budgetRemaining: Double,
    val visitCount: Int = 0,
    val isLeaving: Boolean = false
)

/**
 * Lightweight simulation of street-level customer traffic.
 * Returns estimated visits for a list of businesses in a given hour.
 */
object CustomerSimulation {

    /**
     * Simulate one hour of customer activity across a set of businesses.
     *
     * @param hour Current in-game hour (0–23).
     * @param profiles Active customer profiles present on the street.
     * @param businesses Businesses with their current demand factors.
     * @param rng Random source for stochastic variation.
     * @return Map of businessId → simulated customer count this hour.
     */
    fun simulateHour(
        hour: Int,
        profiles: List<CustomerProfile>,
        businesses: List<Pair<Business, DemandFactors>>,
        rng: Random = Random.Default
    ): Map<String, Int> {
        val result = mutableMapOf<String, Int>()

        businesses.forEach { (business, demand) ->
            val effectiveBase = demand.effectiveCustomersPerHour()
            if (effectiveBase <= 0) {
                result[business.businessId] = 0
                return@forEach
            }

            // Active profiles that prefer this business type
            val attracted = profiles.count { profile ->
                profile.isActiveAt(hour) &&
                    profile.attractionScore(business.businessId, business.type, business.reputation) > 0.3f
            }

            // Poisson-like approximation: base demand scaled by attracted profiles
            val profileFactor = (attracted.toFloat() / profiles.size.coerceAtLeast(1)).coerceIn(0.2f, 1.5f)
            val rawCount = (effectiveBase * profileFactor).toInt()

            // Add ±20% stochastic variation
            val variation = rng.nextDouble(-0.2, 0.2)
            val finalCount = (rawCount * (1.0 + variation)).toInt().coerceAtLeast(0)

            result[business.businessId] = finalCount
        }

        return result
    }

    /**
     * Generate a batch of diverse customer profiles for a new district.
     *
     * @param count Number of profiles to generate.
     * @param rng Random source.
     */
    fun generateProfiles(count: Int, rng: Random = Random.Default): List<CustomerProfile> {
        val names = listOf(
            "Arjun", "Priya", "Dev", "Meera", "Rohan", "Ananya",
            "Vikram", "Sunita", "Karan", "Deepa", "Arun", "Kavya",
            "Nitin", "Pooja", "Sanjay", "Asha", "Rahul", "Neha"
        )
        val segments = CustomerSegment.values()
        val archetypes = CustomerArchetype.values()
        val allTypes = BusinessType.values()

        return (0 until count).map { i ->
            val segment = segments[rng.nextInt(segments.size)]
            val archetype = archetypes[rng.nextInt(archetypes.size)]
            val preferredCount = rng.nextInt(2, 5)
            val preferred = (0 until preferredCount).map { allTypes[rng.nextInt(allTypes.size)] }.distinct()

            // Schedule based on segment
            val schedule = defaultSchedule(segment, rng)

            // Budget based on archetype
            val budget = when (archetype) {
                CustomerArchetype.BUDGET_SHOPPER -> rng.nextDouble(50.0, 150.0)
                CustomerArchetype.VALUE_SEEKER -> rng.nextDouble(100.0, 250.0)
                CustomerArchetype.TRENDSETTER -> rng.nextDouble(200.0, 500.0)
                CustomerArchetype.EXPERIENCE_HUNTER -> rng.nextDouble(300.0, 700.0)
                CustomerArchetype.CONVENIENCE_LOVER -> rng.nextDouble(150.0, 350.0)
                CustomerArchetype.LOYAL_REGULAR -> rng.nextDouble(100.0, 300.0)
            }

            CustomerProfile(
                customerId = "npc_${i}_${rng.nextInt(10000)}",
                name = names[i % names.size],
                segment = segment,
                archetype = archetype,
                dailyBudget = budget,
                schedule = schedule,
                preferredTypes = preferred,
                reputationSensitivity = rng.nextFloat().coerceIn(0.2f, 0.9f)
            )
        }
    }

    private fun defaultSchedule(segment: CustomerSegment, rng: Random): List<ActivityWindow> =
        when (segment) {
            CustomerSegment.COMMUTER -> listOf(
                ActivityWindow(7, 9, rng.nextFloat() * 1.5f + 0.5f),
                ActivityWindow(17, 20, rng.nextFloat() * 1.5f + 0.5f)
            )
            CustomerSegment.TOURIST -> listOf(
                ActivityWindow(10, 22, rng.nextFloat() + 0.3f)
            )
            CustomerSegment.LOCAL_RESIDENT -> listOf(
                ActivityWindow(8, 21, rng.nextFloat() * 0.5f + 0.2f)
            )
            CustomerSegment.YOUTH -> listOf(
                ActivityWindow(14, 23, rng.nextFloat() + 0.5f)
            )
            CustomerSegment.PROFESSIONAL -> listOf(
                ActivityWindow(7, 9, rng.nextFloat() + 0.5f),
                ActivityWindow(12, 14, rng.nextFloat() + 0.3f),
                ActivityWindow(18, 21, rng.nextFloat() + 0.3f)
            )
            CustomerSegment.FAMILY -> listOf(
                ActivityWindow(10, 20, rng.nextFloat() * 0.5f + 0.1f)
            )
        }
}
