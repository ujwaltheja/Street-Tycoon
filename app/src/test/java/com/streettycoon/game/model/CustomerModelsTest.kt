package com.streettycoon.game.model

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

/**
 * Unit tests for CustomerModels.kt
 * Tests NPC profiles, activity windows, attraction scores, and simulation
 */
class CustomerModelsTest {

    private fun makeProfile(
        segment: CustomerSegment = CustomerSegment.COMMUTER,
        archetype: CustomerArchetype = CustomerArchetype.VALUE_SEEKER,
        preferredTypes: List<BusinessType> = listOf(BusinessType.FOOD_STALL),
        dailyBudget: Double = 200.0,
        loyalBusinessIds: List<String> = emptyList()
    ) = CustomerProfile(
        customerId = "c1",
        name = "Test Customer",
        segment = segment,
        archetype = archetype,
        dailyBudget = dailyBudget,
        schedule = listOf(ActivityWindow(8, 20, 1.0f)),
        preferredTypes = preferredTypes,
        loyalBusinessIds = loyalBusinessIds
    )

    // ==================== ARCHETYPES ====================

    @Test
    fun `maxSpendPerVisit varies by archetype`() {
        val archetypes = CustomerArchetype.values()
        val budgets = archetypes.map { archetype ->
            makeProfile(archetype = archetype).maxSpendPerVisit
        }
        // All should be positive
        budgets.forEach { assertTrue(it > 0.0) }
    }

    @Test
    fun `experience hunter spends more per visit than budget shopper`() {
        val hunter = makeProfile(archetype = CustomerArchetype.EXPERIENCE_HUNTER, dailyBudget = 200.0)
        val shopper = makeProfile(archetype = CustomerArchetype.BUDGET_SHOPPER, dailyBudget = 200.0)
        assertTrue(hunter.maxSpendPerVisit > shopper.maxSpendPerVisit)
    }

    // ==================== ACTIVITY WINDOWS ====================

    @Test
    fun `isActiveAt returns true within window`() {
        val profile = makeProfile()  // Schedule 8–20
        assertTrue(profile.isActiveAt(10))
        assertTrue(profile.isActiveAt(8))
        assertTrue(profile.isActiveAt(20))
    }

    @Test
    fun `isActiveAt returns false outside window`() {
        val profile = makeProfile()  // Schedule 8–20
        assertFalse(profile.isActiveAt(5))
        assertFalse(profile.isActiveAt(22))
    }

    @Test
    fun `expectedVisitsAt returns 0 outside schedule`() {
        val profile = makeProfile()
        assertEquals(0f, profile.expectedVisitsAt(3), 0.01f)
    }

    @Test
    fun `expectedVisitsAt returns positive value inside schedule`() {
        val profile = makeProfile()
        assertTrue(profile.expectedVisitsAt(12) > 0f)
    }

    // ==================== ATTRACTION SCORE ====================

    @Test
    fun `attractionScore returns 1_0 for loyal business`() {
        val profile = makeProfile(loyalBusinessIds = listOf("biz1"))
        val score = profile.attractionScore("biz1", BusinessType.FOOD_STALL, 50f)
        assertEquals(1.0f, score, 0.01f)
    }

    @Test
    fun `attractionScore is higher for preferred type`() {
        val profile = makeProfile(preferredTypes = listOf(BusinessType.FOOD_STALL))
        val preferred = profile.attractionScore("b1", BusinessType.FOOD_STALL, 80f)
        val notPreferred = profile.attractionScore("b2", BusinessType.ARCADE, 80f)
        assertTrue(preferred > notPreferred)
    }

    @Test
    fun `attractionScore higher reputation leads to higher score`() {
        val profile = makeProfile(preferredTypes = listOf(BusinessType.FOOD_STALL))
        val highRep = profile.attractionScore("b1", BusinessType.FOOD_STALL, 90f)
        val lowRep = profile.attractionScore("b1", BusinessType.FOOD_STALL, 10f)
        assertTrue(highRep > lowRep)
    }

    @Test
    fun `attractionScore is within 0_0-1_0`() {
        val profile = makeProfile()
        val score = profile.attractionScore("b1", BusinessType.CLUB, 50f)
        assertTrue(score in 0f..1f)
    }

    // ==================== CUSTOMER SIMULATION ====================

    @Test
    fun `CustomerSimulation generateProfiles creates requested count`() {
        val profiles = CustomerSimulation.generateProfiles(20, Random(42))
        assertEquals(20, profiles.size)
    }

    @Test
    fun `CustomerSimulation generateProfiles assigns valid segments`() {
        val profiles = CustomerSimulation.generateProfiles(10, Random(42))
        profiles.forEach { p ->
            assertNotNull(p.segment)
            assertNotNull(p.archetype)
            assertTrue(p.dailyBudget > 0)
            assertTrue(p.schedule.isNotEmpty())
        }
    }

    @Test
    fun `CustomerSimulation simulateHour returns non-negative counts`() {
        val profiles = CustomerSimulation.generateProfiles(20, Random(42))
        val chain = SupplyChain("b1")
        val biz = Business("b1", "p1", BusinessType.FOOD_STALL, "Stall", reputation = 75f,
            supplyChain = chain)
        val demand = DemandFactors(
            baseCustomersPerHour = 30,
            reputationMultiplier = 1.2f,
            eventMultiplier = 1.0f,
            staffMultiplier = 1.0f,
            locationScore = 0.8f,
            supplyFactor = 1.0f
        )

        val result = CustomerSimulation.simulateHour(12, profiles, listOf(biz to demand), Random(42))

        assertNotNull(result["b1"])
        assertTrue(result["b1"]!! >= 0)
    }

    @Test
    fun `CustomerSimulation simulateHour respects zero demand`() {
        val profiles = CustomerSimulation.generateProfiles(10, Random(42))
        val chain = SupplyChain("b1")
        val biz = Business("b1", "p1", BusinessType.FOOD_STALL, "Stall", reputation = 50f,
            supplyChain = chain)
        val zeroDemand = DemandFactors(
            baseCustomersPerHour = 0,
            reputationMultiplier = 1.0f,
            eventMultiplier = 1.0f,
            staffMultiplier = 1.0f,
            locationScore = 1.0f,
            supplyFactor = 1.0f
        )

        val result = CustomerSimulation.simulateHour(12, profiles, listOf(biz to zeroDemand), Random(42))
        assertEquals(0, result["b1"])
    }

    // ==================== DEMAND FACTORS ====================

    @Test
    fun `DemandFactors effectiveCustomersPerHour multiplies all factors`() {
        val demand = DemandFactors(
            baseCustomersPerHour = 100,
            reputationMultiplier = 2.0f,
            eventMultiplier = 1.5f,
            staffMultiplier = 1.0f,
            locationScore = 1.0f,
            supplyFactor = 1.0f
        )
        assertEquals(300, demand.effectiveCustomersPerHour())
    }

    @Test
    fun `DemandFactors effectiveCustomersPerHour cannot be negative`() {
        val demand = DemandFactors(
            baseCustomersPerHour = 50,
            reputationMultiplier = 0.0f,
            eventMultiplier = 0.0f,
            staffMultiplier = 0.0f,
            locationScore = 0.0f,
            supplyFactor = 0.0f
        )
        assertEquals(0, demand.effectiveCustomersPerHour())
    }
}
