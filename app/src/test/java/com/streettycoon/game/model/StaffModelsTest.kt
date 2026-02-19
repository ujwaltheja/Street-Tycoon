package com.streettycoon.game.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for StaffModels.kt
 * Tests staff morale, training, and performance mechanics
 */
class StaffModelsTest {

    private fun makeStaff(
        role: StaffRole = StaffRole.COOK,
        level: Int = 1,
        morale: Float = 70f,
        completedTraining: List<TrainingProgram> = emptyList()
    ) = StaffMember(
        staffId = "s1",
        name = "Test Worker",
        role = role,
        businessId = "b1",
        level = level,
        morale = morale,
        weeklySalary = weeklySalary(role, level),
        completedTraining = completedTraining
    )

    // ==================== MORALE LEVEL ====================

    @Test
    fun `moraleLevel maps score to correct enum`() {
        assertEquals(MoraleLevel.BURNED_OUT, moraleLevel(10f))
        assertEquals(MoraleLevel.LOW, moraleLevel(35f))
        assertEquals(MoraleLevel.NEUTRAL, moraleLevel(50f))
        assertEquals(MoraleLevel.MOTIVATED, moraleLevel(75f))
        assertEquals(MoraleLevel.EXCELLENT, moraleLevel(90f))
    }

    @Test
    fun `MoraleLevel multipliers are ordered correctly`() {
        assertTrue(MoraleLevel.BURNED_OUT.multiplier < MoraleLevel.LOW.multiplier)
        assertTrue(MoraleLevel.LOW.multiplier < MoraleLevel.NEUTRAL.multiplier)
        assertTrue(MoraleLevel.NEUTRAL.multiplier < MoraleLevel.MOTIVATED.multiplier)
        assertTrue(MoraleLevel.MOTIVATED.multiplier < MoraleLevel.EXCELLENT.multiplier)
    }

    // ==================== PERFORMANCE ====================

    @Test
    fun `performanceMultiplier increases with level`() {
        val level1 = makeStaff(level = 1, morale = 60f)
        val level3 = makeStaff(level = 3, morale = 60f)
        assertTrue(level3.performanceMultiplier() > level1.performanceMultiplier())
    }

    @Test
    fun `performanceMultiplier increases with morale`() {
        val low = makeStaff(morale = 15f)
        val high = makeStaff(morale = 90f)
        assertTrue(high.performanceMultiplier() > low.performanceMultiplier())
    }

    @Test
    fun `performanceMultiplier increases with training`() {
        val untrained = makeStaff()
        val trained = makeStaff(completedTraining = listOf(TrainingProgram.SPEED, TrainingProgram.UPSELLING))
        assertTrue(trained.performanceMultiplier() > untrained.performanceMultiplier())
    }

    // ==================== REVENUE BONUS ====================

    @Test
    fun `manager provides higher revenue bonus than cleaner`() {
        val manager = makeStaff(role = StaffRole.MANAGER, level = 2)
        val cleaner = makeStaff(role = StaffRole.CLEANER, level = 2)
        assertTrue(manager.revenueBonus() > cleaner.revenueBonus())
    }

    @Test
    fun `upselling training adds revenue bonus`() {
        val without = makeStaff(role = StaffRole.SERVER)
        val with = makeStaff(role = StaffRole.SERVER, completedTraining = listOf(TrainingProgram.UPSELLING))
        assertTrue(with.revenueBonus() > without.revenueBonus())
    }

    // ==================== THROUGHPUT BONUS ====================

    @Test
    fun `speed training increases throughput bonus`() {
        val without = makeStaff(role = StaffRole.COOK)
        val with = makeStaff(role = StaffRole.COOK, completedTraining = listOf(TrainingProgram.SPEED))
        assertTrue(with.throughputBonus() > without.throughputBonus())
    }

    // ==================== EXPERIENCE / LEVEL-UP ====================

    @Test
    fun `addExperience levels up when threshold met`() {
        val staff = makeStaff(level = 1)
        val leveled = staff.addExperience(100)  // level 1 needs 100 XP
        assertTrue(leveled)
        assertEquals(2, staff.level)
        assertEquals(0, staff.experience)
    }

    @Test
    fun `addExperience does not level up below threshold`() {
        val staff = makeStaff(level = 1)
        val leveled = staff.addExperience(50)
        assertFalse(leveled)
        assertEquals(1, staff.level)
        assertEquals(50, staff.experience)
    }

    // ==================== MORALE ADJUSTMENT ====================

    @Test
    fun `adjustMorale clamps to 0-100`() {
        val staff = makeStaff(morale = 90f)
        staff.adjustMorale(50f)
        assertEquals(100f, staff.morale, 0.01f)

        staff.adjustMorale(-200f)
        assertEquals(0f, staff.morale, 0.01f)
    }

    // ==================== SALARY & HIRE COST ====================

    @Test
    fun `hireCost is positive for all roles`() {
        StaffRole.values().forEach { role ->
            assertTrue("hireCost should be positive for $role", hireCost(role) > 0)
        }
    }

    @Test
    fun `weeklySalary scales with level`() {
        val level1 = weeklySalary(StaffRole.COOK, 1)
        val level3 = weeklySalary(StaffRole.COOK, 3)
        assertTrue(level3 > level1)
    }

    @Test
    fun `manager has higher hire cost than cleaner`() {
        assertTrue(hireCost(StaffRole.MANAGER) > hireCost(StaffRole.CLEANER))
    }

    // ==================== STAFF ROSTER ====================

    @Test
    fun `StaffRoster averagePerformance is 0_5 when empty`() {
        val roster = StaffRoster("b1")
        assertEquals(0.5f, roster.averagePerformance(), 0.01f)
    }

    @Test
    fun `StaffRoster totalWeeklySalaryCost sums all salaries`() {
        val staff = listOf(
            makeStaff(role = StaffRole.COOK),
            makeStaff(role = StaffRole.CASHIER)
        )
        val roster = StaffRoster("b1", staff)
        val expected = weeklySalary(StaffRole.COOK, 1) + weeklySalary(StaffRole.CASHIER, 1)
        assertEquals(expected, roster.totalWeeklySalaryCost(), 0.01)
    }

    @Test
    fun `StaffRoster hasManager detects manager presence`() {
        val noManager = StaffRoster("b1", listOf(makeStaff(StaffRole.COOK)))
        assertFalse(noManager.hasManager())

        val withManager = StaffRoster("b1", listOf(makeStaff(StaffRole.MANAGER)))
        assertTrue(withManager.hasManager())
    }

    // ==================== TRAINING ====================

    @Test
    fun `trainingCost is positive for all programs`() {
        TrainingProgram.values().forEach { program ->
            assertTrue("trainingCost should be positive for $program", trainingCost(program) > 0)
        }
    }

    @Test
    fun `trainingDurationMs is positive for all programs`() {
        TrainingProgram.values().forEach { program ->
            assertTrue("Duration should be positive for $program", trainingDurationMs(program) > 0)
        }
    }

    @Test
    fun `isTrainingComplete returns false when no active training`() {
        val staff = makeStaff()
        assertFalse(staff.isTrainingComplete())
    }
}
