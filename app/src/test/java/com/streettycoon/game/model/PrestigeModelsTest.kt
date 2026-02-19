package com.streettycoon.game.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for PrestigeModels.kt
 * Tests prestige requirements, bonuses, and state calculations
 */
class PrestigeModelsTest {

    private fun emptyState() = PrestigeState()

    private fun stateWithBonuses(vararg bonuses: PrestigeBonus) =
        PrestigeState(earnedBonuses = bonuses.toList(), currentTier = bonuses.size)

    // ==================== PRESTIGE TIERS ====================

    @Test
    fun `buildPrestigeTiers returns non-empty list`() {
        val tiers = buildPrestigeTiers()
        assertTrue(tiers.isNotEmpty())
    }

    @Test
    fun `prestige tiers are ordered by tier number`() {
        val tiers = buildPrestigeTiers().sortedBy { it.tier }
        for (i in 1 until tiers.size) {
            assertTrue(tiers[i].tier > tiers[i - 1].tier)
        }
    }

    @Test
    fun `each tier has positive requirements`() {
        buildPrestigeTiers().forEach { tier ->
            assertTrue("Tier ${tier.tier} revenue requirement must be positive",
                tier.requirement.minTotalRevenue > 0)
            assertTrue("Tier ${tier.tier} level requirement must be positive",
                tier.requirement.minLevel > 0)
        }
    }

    @Test
    fun `later tiers require more revenue`() {
        val tiers = buildPrestigeTiers().sortedBy { it.tier }
        for (i in 1 until tiers.size) {
            assertTrue(tiers[i].requirement.minTotalRevenue >= tiers[i - 1].requirement.minTotalRevenue)
        }
    }

    // ==================== PRESTIGE REQUIREMENTS ====================

    @Test
    fun `PrestigeRequirement isMet when all conditions satisfied`() {
        val req = PrestigeRequirement(
            minTotalRevenue = 50000.0,
            minLevel = 10,
            minBusinessCount = 5
        )
        assertTrue(req.isMet(50000.0, 10, 5, false))
    }

    @Test
    fun `PrestigeRequirement fails when revenue below threshold`() {
        val req = PrestigeRequirement(50000.0, 10, 5)
        assertFalse(req.isMet(49999.0, 10, 5, false))
    }

    @Test
    fun `PrestigeRequirement fails when level below threshold`() {
        val req = PrestigeRequirement(50000.0, 10, 5)
        assertFalse(req.isMet(50000.0, 9, 5, false))
    }

    @Test
    fun `PrestigeRequirement fails when business count below threshold`() {
        val req = PrestigeRequirement(50000.0, 10, 5)
        assertFalse(req.isMet(50000.0, 10, 4, false))
    }

    @Test
    fun `PrestigeRequirement with allDistrictsUnlocked flag requires it`() {
        val req = PrestigeRequirement(50000.0, 10, 5, allDistrictsUnlocked = true)
        assertFalse(req.isMet(50000.0, 10, 5, allDistrictsUnlocked = false))
        assertTrue(req.isMet(50000.0, 10, 5, allDistrictsUnlocked = true))
    }

    // ==================== PRESTIGE STATE BONUSES ====================

    @Test
    fun `incomeMultiplier returns 1_0 with no bonuses`() {
        assertEquals(1.0, emptyState().incomeMultiplier(), 0.001)
    }

    @Test
    fun `incomeMultiplier multiplies all income bonuses`() {
        val state = stateWithBonuses(
            PrestigeBonus(PrestigeBonusType.INCOME_MULTIPLIER, 1.1, ""),
            PrestigeBonus(PrestigeBonusType.INCOME_MULTIPLIER, 1.2, "")
        )
        assertEquals(1.32, state.incomeMultiplier(), 0.01)  // 1.1 * 1.2
    }

    @Test
    fun `startingCashBonus sums all starting cash bonuses`() {
        val state = stateWithBonuses(
            PrestigeBonus(PrestigeBonusType.STARTING_CASH, 500.0, ""),
            PrestigeBonus(PrestigeBonusType.STARTING_CASH, 5000.0, "")
        )
        assertEquals(5500.0, state.startingCashBonus(), 0.01)
    }

    @Test
    fun `xpMultiplier multiplies all XP bonuses`() {
        val state = stateWithBonuses(
            PrestigeBonus(PrestigeBonusType.XP_MULTIPLIER, 1.2, ""),
            PrestigeBonus(PrestigeBonusType.XP_MULTIPLIER, 1.1, "")
        )
        assertEquals(1.32, state.xpMultiplier(), 0.01)  // 1.2 * 1.1
    }

    @Test
    fun `extraLoanSlots sums all loan bonus values`() {
        val state = stateWithBonuses(
            PrestigeBonus(PrestigeBonusType.LOAN_LIMIT_INCREASE, 1.0, ""),
            PrestigeBonus(PrestigeBonusType.LOAN_LIMIT_INCREASE, 2.0, "")
        )
        assertEquals(3, state.extraLoanSlots())
    }

    @Test
    fun `districtUnlockDiscount sums discount bonuses and caps at 50`() {
        val state = stateWithBonuses(
            PrestigeBonus(PrestigeBonusType.REDUCED_UNLOCK_COST, 0.3, ""),
            PrestigeBonus(PrestigeBonusType.REDUCED_UNLOCK_COST, 0.4, "")
        )
        // Sum = 0.7, but capped at 0.5
        assertEquals(0.5, state.districtUnlockDiscount(), 0.01)
    }

    // ==================== CAN PRESTIGE ====================

    @Test
    fun `canPrestige returns true when conditions met`() {
        val tiers = buildPrestigeTiers()
        val state = emptyState()  // currentTier = 0
        // Tier 1 requires 50000 revenue, level 10, 5 businesses
        assertTrue(state.canPrestige(tiers, 50000.0, 10, 5, false))
    }

    @Test
    fun `canPrestige returns false when conditions not met`() {
        val tiers = buildPrestigeTiers()
        val state = emptyState()
        assertFalse(state.canPrestige(tiers, 100.0, 1, 0, false))
    }

    @Test
    fun `canPrestige returns false when already at max tier`() {
        val tiers = buildPrestigeTiers()
        val maxTier = tiers.maxOf { it.tier }
        val state = PrestigeState(currentTier = maxTier)
        assertFalse(state.canPrestige(tiers, Double.MAX_VALUE, 100, 100, true))
    }

    // ==================== TIER CONTENT ====================

    @Test
    fun `each prestige tier has a badge emoji`() {
        buildPrestigeTiers().forEach { tier ->
            assertTrue("Tier ${tier.tier} badge should not be empty", tier.badgeEmoji.isNotEmpty())
        }
    }

    @Test
    fun `each prestige tier has at least one bonus`() {
        buildPrestigeTiers().forEach { tier ->
            assertTrue("Tier ${tier.tier} should have at least one bonus", tier.bonuses.isNotEmpty())
        }
    }
}
