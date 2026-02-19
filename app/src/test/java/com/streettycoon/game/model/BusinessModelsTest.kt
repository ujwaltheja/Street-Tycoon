package com.streettycoon.game.model

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

/**
 * Unit tests for BusinessModels.kt
 * Tests plot grid, supply chain, and business mechanics
 */
class BusinessModelsTest {

    // ==================== BUSINESS CONFIG ====================

    @Test
    fun `BusinessConfig getConfig returns config for all types`() {
        BusinessType.values().forEach { type ->
            val config = BusinessConfig.getConfig(type)
            assertNotNull("Config should exist for $type", config)
            assertTrue("Base cost must be positive for $type", config.baseCost > 0)
            assertTrue("Revenue must be positive for $type", config.baseRevenuePerCustomer > 0)
            assertTrue("Max staff must be positive for $type", config.maxStaff > 0)
        }
    }

    @Test
    fun `BusinessConfig club has correct settings`() {
        val config = BusinessConfig.getConfig(BusinessType.CLUB)
        assertEquals(BusinessType.CLUB, config.type)
        assertEquals(PlotSize.EXTRA_LARGE, config.footprint)
        assertTrue(config.maxStaff >= 10)
        assertTrue(config.reputationWeight > 2.0f)
    }

    @Test
    fun `BusinessConfig arcade has large footprint`() {
        val config = BusinessConfig.getConfig(BusinessType.ARCADE)
        assertEquals(PlotSize.LARGE, config.footprint)
    }

    // ==================== PLOT GRID ====================

    @Test
    fun `StreetGrid canPlace returns true for empty grid`() {
        val grid = StreetGrid(districtId = "d1", columns = 10, rows = 10)
        assertTrue(grid.canPlace(0, 0, PlotSize.SMALL))
        assertTrue(grid.canPlace(5, 5, PlotSize.MEDIUM))
    }

    @Test
    fun `StreetGrid canPlace returns false out of bounds`() {
        val grid = StreetGrid(districtId = "d1", columns = 5, rows = 5)
        assertFalse(grid.canPlace(4, 4, PlotSize.MEDIUM)) // 4+2=6 > 5
        assertFalse(grid.canPlace(-1, 0, PlotSize.TINY))
    }

    @Test
    fun `StreetGrid canPlace detects occupied cells`() {
        val plot = Plot(
            plotId = "p1",
            districtId = "d1",
            originX = 2,
            originY = 2,
            size = PlotSize.MEDIUM,  // 2x2
            purchaseCost = 100.0,
            isOwned = true
        )
        val grid = StreetGrid(districtId = "d1", columns = 10, rows = 10, plots = listOf(plot))

        // Overlapping position should fail
        assertFalse(grid.canPlace(2, 2, PlotSize.TINY))
        assertFalse(grid.canPlace(1, 1, PlotSize.MEDIUM))  // Would overlap at (2,2)+(3,3)

        // Non-overlapping position should succeed
        assertTrue(grid.canPlace(4, 4, PlotSize.SMALL))
    }

    @Test
    fun `StreetGrid freePositions returns correct positions`() {
        val grid = StreetGrid(districtId = "d1", columns = 3, rows = 3)
        val free = grid.freePositions(PlotSize.TINY)
        assertEquals(9, free.size)  // All 3x3 cells are free for TINY (1x1)
    }

    @Test
    fun `Plot getCells returns correct cell list`() {
        val plot = Plot(
            plotId = "p1", districtId = "d1",
            originX = 2, originY = 3,
            size = PlotSize.MEDIUM,  // 2x2
            purchaseCost = 100.0
        )
        val cells = plot.getCells()
        assertEquals(4, cells.size)
        assertTrue(cells.contains(2 to 3))
        assertTrue(cells.contains(3 to 3))
        assertTrue(cells.contains(2 to 4))
        assertTrue(cells.contains(3 to 4))
    }

    @Test
    fun `Plot isOccupied reflects businessId presence`() {
        val emptyPlot = Plot("p1", "d1", 0, 0, PlotSize.TINY, null, 100.0)
        val occupiedPlot = Plot("p2", "d1", 2, 0, PlotSize.TINY, "biz1", 100.0)
        assertFalse(emptyPlot.isOccupied)
        assertTrue(occupiedPlot.isOccupied)
    }

    // ==================== SUPPLY CHAIN ====================

    @Test
    fun `SupplyItem consume decreases stock`() {
        val item = SupplyItem(
            itemId = "i1", name = "Flour", category = SupplyCategory.INGREDIENTS,
            currentStock = 100.0, maxStock = 200.0,
            consumptionPerCustomer = 5.0, restockCost = 50.0
        )
        val success = item.consume()
        assertTrue(success)
        assertEquals(95.0, item.currentStock, 0.01)
    }

    @Test
    fun `SupplyItem consume fails when out of stock`() {
        val item = SupplyItem(
            itemId = "i1", name = "Flour", category = SupplyCategory.INGREDIENTS,
            currentStock = 3.0, maxStock = 200.0,
            consumptionPerCustomer = 5.0, restockCost = 50.0
        )
        assertFalse(item.consume())
        assertEquals(3.0, item.currentStock, 0.01)  // Unchanged
    }

    @Test
    fun `SupplyItem isLow detects low stock`() {
        val item = SupplyItem(
            itemId = "i1", name = "Flour", category = SupplyCategory.INGREDIENTS,
            currentStock = 15.0, maxStock = 100.0,
            consumptionPerCustomer = 1.0, restockCost = 50.0
        )
        assertTrue(item.isLow())  // 15% < 20%
    }

    @Test
    fun `SupplyItem restock fills to max and returns cost`() {
        val item = SupplyItem(
            itemId = "i1", name = "Flour", category = SupplyCategory.INGREDIENTS,
            currentStock = 50.0, maxStock = 200.0,
            consumptionPerCustomer = 1.0, restockCost = 100.0
        )
        val cost = item.restock()
        assertEquals(200.0, item.currentStock, 0.01)
        assertTrue(cost > 0.0)
    }

    @Test
    fun `SupplyChain consumeForCustomer returns false when any supply is empty`() {
        val items = listOf(
            SupplyItem("i1", "Flour", SupplyCategory.INGREDIENTS, 100.0, 200.0, 5.0, 50.0),
            SupplyItem("i2", "Oil", SupplyCategory.INGREDIENTS, 2.0, 100.0, 5.0, 30.0) // Too low
        )
        val chain = SupplyChain("biz1", items)
        assertFalse(chain.consumeForCustomer())
    }

    @Test
    fun `SupplyChain maxCustomersServable returns correct minimum`() {
        val items = listOf(
            SupplyItem("i1", "Flour", SupplyCategory.INGREDIENTS, 100.0, 200.0, 5.0, 50.0),  // 20 customers
            SupplyItem("i2", "Oil", SupplyCategory.INGREDIENTS, 30.0, 100.0, 5.0, 30.0)     // 6 customers
        )
        val chain = SupplyChain("biz1", items)
        assertEquals(6, chain.maxCustomersServable())
    }

    // ==================== BUSINESS INSTANCE ====================

    @Test
    fun `Business revenuePerCustomer scales with level`() {
        val chain = SupplyChain("b1")
        val biz = Business("b1", "p1", BusinessType.FOOD_STALL, "Test Stall", level = 1,
            reputation = 100f, supplyChain = chain)
        val revenue1 = biz.revenuePerCustomer()

        val biz2 = biz.copy(level = 2)
        val revenue2 = biz2.revenuePerCustomer()

        assertTrue(revenue2 > revenue1)
    }

    @Test
    fun `Business revenuePerCustomer scales with reputation`() {
        val chain = SupplyChain("b1")
        val highRep = Business("b1", "p1", BusinessType.FOOD_STALL, "Test Stall",
            reputation = 100f, supplyChain = chain)
        val lowRep = Business("b2", "p2", BusinessType.FOOD_STALL, "Test Stall",
            reputation = 10f, supplyChain = chain)

        assertTrue(highRep.revenuePerCustomer() > lowRep.revenuePerCustomer())
    }

    @Test
    fun `Business adjustReputation clamps to valid range`() {
        val chain = SupplyChain("b1")
        val biz = Business("b1", "p1", BusinessType.FOOD_STALL, "Test Stall",
            reputation = 90f, supplyChain = chain)

        biz.adjustReputation(20f)
        assertEquals(100f, biz.reputation, 0.01f)

        biz.adjustReputation(-200f)
        assertEquals(0f, biz.reputation, 0.01f)
    }
}
