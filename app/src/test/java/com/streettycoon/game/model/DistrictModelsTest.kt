package com.streettycoon.game.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for DistrictModels.kt
 * Tests unlock conditions, district themes, and default district setup
 */
class DistrictModelsTest {

    // ==================== UNLOCK CONDITIONS ====================

    @Test
    fun `MIN_CASH condition satisfied when cash meets threshold`() {
        val cond = UnlockCondition(UnlockConditionType.MIN_CASH, 1000.0)
        assertTrue(cond.isMet(1000.0, 0.0, 0, 1, emptySet(), emptySet()))
        assertFalse(cond.isMet(999.0, 0.0, 0, 1, emptySet(), emptySet()))
    }

    @Test
    fun `MIN_TOTAL_REVENUE condition checks lifetime revenue`() {
        val cond = UnlockCondition(UnlockConditionType.MIN_TOTAL_REVENUE, 5000.0)
        assertTrue(cond.isMet(0.0, 5000.0, 0, 1, emptySet(), emptySet()))
        assertFalse(cond.isMet(0.0, 4999.0, 0, 1, emptySet(), emptySet()))
    }

    @Test
    fun `MIN_BUSINESSES condition checks active business count`() {
        val cond = UnlockCondition(UnlockConditionType.MIN_BUSINESSES, 5.0)
        assertTrue(cond.isMet(0.0, 0.0, 5, 1, emptySet(), emptySet()))
        assertFalse(cond.isMet(0.0, 0.0, 4, 1, emptySet(), emptySet()))
    }

    @Test
    fun `MIN_LEVEL condition checks player level`() {
        val cond = UnlockCondition(UnlockConditionType.MIN_LEVEL, 10.0)
        assertTrue(cond.isMet(0.0, 0.0, 0, 10, emptySet(), emptySet()))
        assertFalse(cond.isMet(0.0, 0.0, 0, 9, emptySet(), emptySet()))
    }

    @Test
    fun `TECH_NODE_RESEARCHED checks node presence`() {
        val cond = UnlockCondition(UnlockConditionType.TECH_NODE_RESEARCHED, 1.0, "exp_second_district")
        assertTrue(cond.isMet(0.0, 0.0, 0, 1, setOf("exp_second_district"), emptySet()))
        assertFalse(cond.isMet(0.0, 0.0, 0, 1, emptySet(), emptySet()))
    }

    @Test
    fun `DISTRICT_CLEARED checks cleared district set`() {
        val cond = UnlockCondition(UnlockConditionType.DISTRICT_CLEARED, 1.0, "district_1")
        assertTrue(cond.isMet(0.0, 0.0, 0, 1, emptySet(), setOf("district_1")))
        assertFalse(cond.isMet(0.0, 0.0, 0, 1, emptySet(), emptySet()))
    }

    // ==================== DISTRICT THEME ====================

    @Test
    fun `districtThemeMultipliers returns values for all themes`() {
        DistrictTheme.values().forEach { theme ->
            val multipliers = districtThemeMultipliers(theme)
            assertFalse("Theme $theme should have multipliers", multipliers.isEmpty())
            multipliers.values.forEach { v -> assertTrue(v > 0f) }
        }
    }

    @Test
    fun `WATERFRONT theme boosts tourist multiplier highest`() {
        val waterfront = districtThemeMultipliers(DistrictTheme.WATERFRONT)
        val tourist = waterfront[CustomerSegment.TOURIST] ?: 1.0f
        assertTrue("Tourist multiplier in Waterfront should be high", tourist >= 1.5f)
    }

    // ==================== DISTRICT ====================

    @Test
    fun `District allConditionsMet with no conditions is true`() {
        val grid = StreetGrid("d1", 5, 5)
        val district = District("d1", "Test", "Desc", DistrictTheme.BAZAAR,
            0.0, emptyList(), grid)
        assertTrue(district.allConditionsMet(0.0, 0.0, 0, 1, emptySet(), emptySet()))
    }

    @Test
    fun `District allConditionsMet fails if any condition fails`() {
        val grid = StreetGrid("d1", 5, 5)
        val conditions = listOf(
            UnlockCondition(UnlockConditionType.MIN_CASH, 1000.0),
            UnlockCondition(UnlockConditionType.MIN_LEVEL, 5.0)
        )
        val district = District("d1", "Test", "Desc", DistrictTheme.BAZAAR,
            1000.0, conditions, grid)

        // Cash OK, level NOT OK
        assertFalse(district.allConditionsMet(1000.0, 0.0, 0, 4, emptySet(), emptySet()))
        // Both OK
        assertTrue(district.allConditionsMet(1000.0, 0.0, 0, 5, emptySet(), emptySet()))
    }

    @Test
    fun `District demandMultiplier delegates to theme`() {
        val grid = StreetGrid("d1", 5, 5)
        val district = District("d1", "Bazaar", "Desc", DistrictTheme.BAZAAR,
            0.0, emptyList(), grid)
        val multiplier = district.demandMultiplier(CustomerSegment.COMMUTER)
        assertTrue(multiplier > 0f)
    }

    @Test
    fun `District isFullyDeveloped returns false for empty grid`() {
        val grid = StreetGrid("d1", 5, 5)
        val district = District("d1", "Test", "Desc", DistrictTheme.BAZAAR,
            0.0, emptyList(), grid)
        assertFalse(district.isFullyDeveloped())
    }

    // ==================== DEFAULT DISTRICTS ====================

    @Test
    fun `buildDefaultDistricts returns three districts`() {
        val districts = buildDefaultDistricts()
        assertEquals(3, districts.size)
    }

    @Test
    fun `first district is unlocked by default`() {
        val districts = buildDefaultDistricts()
        val first = districts.minByOrNull { it.sortOrder }!!
        assertTrue(first.isUnlocked)
        assertTrue(first.unlockConditions.isEmpty())
    }

    @Test
    fun `later districts have more demanding conditions`() {
        val districts = buildDefaultDistricts().sortedBy { it.sortOrder }
        // District 2 and 3 should have conditions
        assertTrue(districts[1].unlockConditions.isNotEmpty())
        assertTrue(districts[2].unlockConditions.isNotEmpty())
    }

    @Test
    fun `district grids have valid dimensions`() {
        buildDefaultDistricts().forEach { d ->
            assertTrue("${d.districtId} grid columns must be positive", d.grid.columns > 0)
            assertTrue("${d.districtId} grid rows must be positive", d.grid.rows > 0)
        }
    }

    @Test
    fun `districts have different themes`() {
        val districts = buildDefaultDistricts()
        val themes = districts.map { it.theme }.toSet()
        // Each district should have a unique theme
        assertEquals(districts.size, themes.size)
    }
}
