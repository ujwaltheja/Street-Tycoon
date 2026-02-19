package com.streettycoon.game.model

import com.google.gson.annotations.SerializedName
import kotlin.random.Random

/**
 * Street Event Models
 *
 * Dynamic street events (festivals, protests, pop-ups, etc.) that temporarily
 * alter demand, reputation, and supply availability.
 */

// ==================== EVENT TYPES ====================

/**
 * Categories of street events with different gameplay effects.
 */
enum class StreetEventType {
    @SerializedName("FESTIVAL") FESTIVAL,       // boosts all food/entertainment demand
    @SerializedName("PROTEST") PROTEST,         // reduces foot traffic, damage to reputation
    @SerializedName("POP_UP_MARKET") POP_UP_MARKET,   // temporary rival stalls, supply disruption
    @SerializedName("CELEBRITY_VISIT") CELEBRITY_VISIT, // viral boost to specific business
    @SerializedName("HEALTH_INSPECTION") HEALTH_INSPECTION, // penalty for low-hygiene businesses
    @SerializedName("POWER_OUTAGE") POWER_OUTAGE,  // disables electronics businesses briefly
    @SerializedName("SUPPLY_SHORTAGE") SUPPLY_SHORTAGE, // raises supply costs
    @SerializedName("STREET_PARTY") STREET_PARTY,  // evening boost to entertainment
    @SerializedName("RAINY_DAY") RAINY_DAY,        // reduces outdoor businesses, boosts indoor
    @SerializedName("LOCAL_HOLIDAY") LOCAL_HOLIDAY  // large footfall across all businesses
}

/**
 * How the event's effect scales with player response.
 */
enum class EventResolution {
    @SerializedName("ACTIVE") ACTIVE,       // event is currently running
    @SerializedName("RESOLVED") RESOLVED,   // player accepted / completed the event
    @SerializedName("DISMISSED") DISMISSED, // player dismissed without acting
    @SerializedName("EXPIRED") EXPIRED      // event ended before player responded
}

// ==================== EFFECT DEFINITION ====================

/**
 * A single effect a street event applies to the game world.
 */
data class EventEffect(
    @SerializedName("demandMultiplier") val demandMultiplier: Float = 1.0f,
    @SerializedName("revenueMultiplier") val revenueMultiplier: Float = 1.0f,
    @SerializedName("reputationDelta") val reputationDelta: Float = 0f,
    @SerializedName("supplyCostMultiplier") val supplyCostMultiplier: Float = 1.0f,
    @SerializedName("affectedBusinessTypes") val affectedBusinessTypes: List<BusinessType> = emptyList(),
    @SerializedName("affectedSegments") val affectedSegments: List<CustomerSegment> = emptyList()
) {
    /** Whether this event has a positive net effect. */
    val isPositive: Boolean
        get() = demandMultiplier > 1f || revenueMultiplier > 1f || reputationDelta > 0f
}

// ==================== STREET EVENT ====================

/**
 * A dynamic street event with time limits and player actions.
 */
data class StreetEvent(
    @SerializedName("eventId") val eventId: String,
    @SerializedName("type") val type: StreetEventType,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("districtId") val districtId: String,
    @SerializedName("effect") val effect: EventEffect,
    @SerializedName("durationMs") val durationMs: Long,
    @SerializedName("startTimestamp") val startTimestamp: Long = System.currentTimeMillis(),
    @SerializedName("resolution") var resolution: EventResolution = EventResolution.ACTIVE,
    @SerializedName("actionLabel") val actionLabel: String? = null,   // CTA for the player
    @SerializedName("actionCost") val actionCost: Double = 0.0,       // Cost to resolve
    @SerializedName("actionReward") val actionReward: Double = 0.0    // Reward on resolution
) {
    /** Whether this event is still within its active time window. */
    fun isActive(): Boolean =
        resolution == EventResolution.ACTIVE &&
            System.currentTimeMillis() < startTimestamp + durationMs

    /** Time remaining in milliseconds. */
    fun remainingMs(): Long =
        (startTimestamp + durationMs - System.currentTimeMillis()).coerceAtLeast(0)

    /** Progress through the event (0.0–1.0). */
    fun progress(): Float {
        val elapsed = System.currentTimeMillis() - startTimestamp
        return (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
    }
}

// ==================== EVENT GENERATOR ====================

/**
 * Generates random street events using weighted selection.
 */
object StreetEventGenerator {

    private data class EventTemplate(
        val type: StreetEventType,
        val weight: Float,
        val titles: List<String>,
        val descriptions: List<String>,
        val effect: EventEffect,
        val durationMs: Long,
        val actionLabel: String? = null,
        val actionCost: Double = 0.0,
        val actionReward: Double = 0.0
    )

    private val TEMPLATES = listOf(
        EventTemplate(
            type = StreetEventType.FESTIVAL,
            weight = 0.15f,
            titles = listOf("Street Food Festival", "Cultural Fiesta", "Night Market Festival"),
            descriptions = listOf(
                "A lively food festival draws huge crowds to the street!",
                "The annual cultural celebration brings visitors from across the city.",
                "The night market festival fills every corner with eager shoppers."
            ),
            effect = EventEffect(
                demandMultiplier = 2.0f,
                revenueMultiplier = 1.3f,
                reputationDelta = 5f,
                affectedBusinessTypes = listOf(
                    BusinessType.FOOD_STALL, BusinessType.TEA_SHOP, BusinessType.JUICE_BAR,
                    BusinessType.STREET_FOOD, BusinessType.SOUVENIR_STALL
                ),
                affectedSegments = listOf(CustomerSegment.TOURIST, CustomerSegment.FAMILY)
            ),
            durationMs = 4 * 60 * 60 * 1000L  // 4 hours
        ),
        EventTemplate(
            type = StreetEventType.PROTEST,
            weight = 0.08f,
            titles = listOf("Workers' Protest", "Environmental Rally", "Rent Strike March"),
            descriptions = listOf(
                "Demonstrators block part of the street, cutting foot traffic.",
                "An environmental rally slows traffic and reduces outdoor business.",
                "Rent-strike marchers divert customers away from the area."
            ),
            effect = EventEffect(
                demandMultiplier = 0.4f,
                reputationDelta = -3f
            ),
            durationMs = 3 * 60 * 60 * 1000L,  // 3 hours
            actionLabel = "Offer free refreshments",
            actionCost = 200.0,
            actionReward = 150.0
        ),
        EventTemplate(
            type = StreetEventType.POP_UP_MARKET,
            weight = 0.12f,
            titles = listOf("Pop-Up Market", "Flash Bazaar", "Weekend Pop-Up"),
            descriptions = listOf(
                "A spontaneous pop-up market brings both competition and new customers.",
                "A flash bazaar appears overnight – expect rival stalls and curious buyers.",
                "Weekend pop-up vendors compete for space and shoppers."
            ),
            effect = EventEffect(
                demandMultiplier = 1.4f,
                supplyCostMultiplier = 1.2f,
                affectedSegments = listOf(CustomerSegment.TOURIST, CustomerSegment.YOUTH)
            ),
            durationMs = 6 * 60 * 60 * 1000L
        ),
        EventTemplate(
            type = StreetEventType.CELEBRITY_VISIT,
            weight = 0.05f,
            titles = listOf("Celebrity Spotted!", "Influencer Check-In", "Food Critic Visit"),
            descriptions = listOf(
                "A local celebrity just checked in on social media – traffic is spiking!",
                "A popular influencer is live-streaming from your street right now.",
                "A renowned food critic is touring the area – impress them for a massive boost."
            ),
            effect = EventEffect(
                demandMultiplier = 3.0f,
                revenueMultiplier = 1.5f,
                reputationDelta = 10f
            ),
            durationMs = 2 * 60 * 60 * 1000L,
            actionLabel = "Offer VIP experience",
            actionCost = 500.0,
            actionReward = 2000.0
        ),
        EventTemplate(
            type = StreetEventType.HEALTH_INSPECTION,
            weight = 0.10f,
            titles = listOf("Health Inspection", "Hygiene Audit", "Food Safety Check"),
            descriptions = listOf(
                "Health inspectors are visiting all food businesses in the district.",
                "A random hygiene audit is underway – ensure your kitchens are clean!",
                "Food safety officers are checking all stalls today."
            ),
            effect = EventEffect(
                reputationDelta = -5f,
                affectedBusinessTypes = listOf(
                    BusinessType.FOOD_STALL, BusinessType.TEA_SHOP, BusinessType.STREET_FOOD
                )
            ),
            durationMs = 2 * 60 * 60 * 1000L,
            actionLabel = "Hire cleaning crew",
            actionCost = 300.0,
            actionReward = 0.0
        ),
        EventTemplate(
            type = StreetEventType.POWER_OUTAGE,
            weight = 0.07f,
            titles = listOf("Power Outage", "Grid Failure", "Rolling Blackout"),
            descriptions = listOf(
                "A power outage has hit the district – electronic businesses are down.",
                "Grid maintenance has caused a temporary power failure.",
                "Rolling blackouts are affecting the area for the next few hours."
            ),
            effect = EventEffect(
                demandMultiplier = 0.2f,
                affectedBusinessTypes = listOf(
                    BusinessType.ARCADE, BusinessType.ELECTRONICS_SHOP, BusinessType.CLUB
                )
            ),
            durationMs = 3 * 60 * 60 * 1000L,
            actionLabel = "Buy backup generator",
            actionCost = 800.0,
            actionReward = 400.0
        ),
        EventTemplate(
            type = StreetEventType.SUPPLY_SHORTAGE,
            weight = 0.10f,
            titles = listOf("Supply Shortage", "Ingredient Scarcity", "Logistics Disruption"),
            descriptions = listOf(
                "A supply shortage is driving ingredient prices up across the district.",
                "Key ingredients are scarce – restock now before prices rise further.",
                "A logistics strike has disrupted deliveries and raised supply costs."
            ),
            effect = EventEffect(supplyCostMultiplier = 1.5f),
            durationMs = 5 * 60 * 60 * 1000L,
            actionLabel = "Bulk-buy reserves",
            actionCost = 600.0,
            actionReward = 300.0
        ),
        EventTemplate(
            type = StreetEventType.STREET_PARTY,
            weight = 0.12f,
            titles = listOf("Street Party", "Block Party", "Summer Street Rave"),
            descriptions = listOf(
                "An impromptu street party is drawing evening revellers.",
                "A neighbourhood block party brings locals out in force tonight.",
                "A summer street rave fills the night with music and energy."
            ),
            effect = EventEffect(
                demandMultiplier = 2.5f,
                revenueMultiplier = 1.4f,
                affectedBusinessTypes = listOf(
                    BusinessType.CLUB, BusinessType.MUSIC_VENUE, BusinessType.JUICE_BAR
                ),
                affectedSegments = listOf(CustomerSegment.YOUTH)
            ),
            durationMs = 4 * 60 * 60 * 1000L
        ),
        EventTemplate(
            type = StreetEventType.RAINY_DAY,
            weight = 0.13f,
            titles = listOf("Rainy Day", "Afternoon Showers", "Thunderstorm Alert"),
            descriptions = listOf(
                "Rain is keeping outdoor shoppers away – indoor businesses see a boost.",
                "Afternoon showers are sending commuters indoors.",
                "A thunderstorm warning has emptied outdoor stalls."
            ),
            effect = EventEffect(
                demandMultiplier = 0.5f,
                affectedBusinessTypes = listOf(
                    BusinessType.FOOD_STALL, BusinessType.STREET_FOOD, BusinessType.SOUVENIR_STALL
                )
            ),
            durationMs = 3 * 60 * 60 * 1000L
        ),
        EventTemplate(
            type = StreetEventType.LOCAL_HOLIDAY,
            weight = 0.08f,
            titles = listOf("Public Holiday", "Local Celebration", "Independence Day"),
            descriptions = listOf(
                "It's a public holiday – expect record footfall all day!",
                "The local festival brings the whole city out to celebrate.",
                "Independence Day celebrations flood the streets with visitors."
            ),
            effect = EventEffect(
                demandMultiplier = 2.8f,
                revenueMultiplier = 1.2f,
                reputationDelta = 3f
            ),
            durationMs = 8 * 60 * 60 * 1000L
        )
    )

    /**
     * Generate a random street event for the given district.
     *
     * @param districtId The district where the event occurs.
     * @param rng Random source (use a seeded instance for determinism).
     * @return A new [StreetEvent].
     */
    fun generate(districtId: String, rng: Random = Random.Default): StreetEvent {
        val template = weightedRandom(TEMPLATES, rng)
        val titleIndex = rng.nextInt(template.titles.size)
        val now = System.currentTimeMillis()

        return StreetEvent(
            eventId = "event_${now}_${rng.nextInt(10000)}",
            type = template.type,
            title = template.titles[titleIndex],
            description = template.descriptions[titleIndex],
            districtId = districtId,
            effect = template.effect,
            durationMs = template.durationMs,
            startTimestamp = now,
            actionLabel = template.actionLabel,
            actionCost = template.actionCost,
            actionReward = template.actionReward
        )
    }

    /** Select a template using weighted random selection. */
    private fun weightedRandom(templates: List<EventTemplate>, rng: Random): EventTemplate {
        val totalWeight = templates.sumOf { it.weight.toDouble() }
        var roll = rng.nextDouble() * totalWeight
        for (template in templates) {
            roll -= template.weight
            if (roll <= 0) return template
        }
        return templates.last()
    }
}
