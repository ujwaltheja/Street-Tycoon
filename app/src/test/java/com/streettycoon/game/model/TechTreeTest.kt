package com.streettycoon.game.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for TechTree.kt
 * Tests tech node availability, bonus aggregation, and tree structure
 */
class TechTreeTest {

    private fun makeState(researchedIds: Set<String> = emptySet()): TechTreeState {
        val nodes = buildDefaultTree().map { node ->
            node.copy(isResearched = node.nodeId in researchedIds)
        }
        return TechTreeState(nodes)
    }

    // ==================== TREE STRUCTURE ====================

    @Test
    fun `buildDefaultTree returns non-empty tree`() {
        val nodes = buildDefaultTree()
        assertTrue(nodes.isNotEmpty())
    }

    @Test
    fun `all categories are represented`() {
        val nodes = buildDefaultTree()
        val categories = nodes.map { it.category }.toSet()
        TechCategory.values().forEach { category ->
            assertTrue("Category $category should be in default tree", category in categories)
        }
    }

    @Test
    fun `all prerequisite nodeIds exist in the tree`() {
        val nodes = buildDefaultTree()
        val nodeIds = nodes.map { it.nodeId }.toSet()
        nodes.forEach { node ->
            node.prerequisites.forEach { prereqId ->
                assertTrue(
                    "Prerequisite $prereqId for ${node.nodeId} must exist in tree",
                    prereqId in nodeIds
                )
            }
        }
    }

    // ==================== AVAILABILITY ====================

    @Test
    fun `nodes with no prerequisites are available from start`() {
        val state = makeState()
        val available = state.availableNodes()
        val rootNodes = buildDefaultTree().filter { it.prerequisites.isEmpty() }
        rootNodes.forEach { root ->
            assertTrue("Root node ${root.nodeId} should be available", available.any { it.nodeId == root.nodeId })
        }
    }

    @Test
    fun `node is unavailable when prerequisite not researched`() {
        val state = makeState()  // Nothing researched
        val available = state.availableNodes()
        val nodesWithPrereqs = buildDefaultTree().filter { it.prerequisites.isNotEmpty() }
        // At least some nodes with prerequisites should NOT be available
        assertTrue(nodesWithPrereqs.any { node -> available.none { it.nodeId == node.nodeId } })
    }

    @Test
    fun `node becomes available after researching prerequisites`() {
        val bulkOrderPrereq = "ops_efficient_supply"
        val state = makeState(setOf(bulkOrderPrereq))
        val available = state.availableNodes()
        assertTrue(available.any { it.nodeId == "ops_bulk_orders" })
    }

    @Test
    fun `researched node is not in available list`() {
        val state = makeState(setOf("ops_efficient_supply"))
        val available = state.availableNodes()
        assertFalse(available.any { it.nodeId == "ops_efficient_supply" })
    }

    // ==================== BONUS AGGREGATION ====================

    @Test
    fun `aggregateBonus returns default multiplier of one when no nodes researched`() {
        val state = makeState()
        assertEquals(1.0, state.aggregateBonus(TechBonusType.REVENUE_MULTIPLIER), 0.001)
    }

    @Test
    fun `aggregateBonus multiplies revenue multipliers`() {
        // Research two nodes that both have revenue multipliers
        val state = makeState(setOf("cx_loyalty_program", "mkt_social_buzz"))
        val multiplier = state.aggregateBonus(TechBonusType.REVENUE_MULTIPLIER)
        // cx_loyalty_program: 1.1, mkt_social_buzz: 1.1 → 1.1 * 1.1 = 1.21
        assertEquals(1.21, multiplier, 0.01)
    }

    @Test
    fun `aggregateBonus sums additive bonuses`() {
        // Research two morale-boosting nodes
        val state = makeState(setOf("staff_onboarding", "staff_incentives"))
        val morale = state.aggregateBonus(TechBonusType.STAFF_MORALE)
        // 10 + 15 = 25
        assertEquals(25.0, morale, 0.01)
    }

    // ==================== UNLOCK BONUSES ====================

    @Test
    fun `unlockedBusinessTypes is empty with no research`() {
        val state = makeState()
        assertTrue(state.unlockedBusinessTypes().isEmpty())
    }

    @Test
    fun `cx_vip_lounge unlocks CLUB business type`() {
        val state = makeState(setOf("cx_vip_lounge"))
        assertTrue(BusinessType.CLUB.name in state.unlockedBusinessTypes())
    }

    @Test
    fun `exp_entertainment_row unlocks ARCADE and MUSIC_VENUE`() {
        val state = makeState(setOf("exp_entertainment_row"))
        assertTrue(BusinessType.ARCADE.name in state.unlockedBusinessTypes())
        assertTrue(BusinessType.MUSIC_VENUE.name in state.unlockedBusinessTypes())
    }

    // ==================== FIND NODE ====================

    @Test
    fun `find returns node for valid ID`() {
        val state = makeState()
        assertNotNull(state.find("ops_efficient_supply"))
    }

    @Test
    fun `find returns null for invalid ID`() {
        val state = makeState()
        assertNull(state.find("non_existent"))
    }

    // ==================== NODE PROPERTIES ====================

    @Test
    fun `all nodes have positive cost`() {
        buildDefaultTree().forEach { node ->
            assertTrue("${node.nodeId} must have positive cost", node.cost > 0)
        }
    }

    @Test
    fun `all nodes have at least one bonus`() {
        buildDefaultTree().forEach { node ->
            assertTrue("${node.nodeId} must have at least one bonus", node.bonuses.isNotEmpty())
        }
    }
}
