package com.streettycoon.game.model

import com.google.gson.annotations.SerializedName

/**
 * Staff Models
 *
 * Staff management system with morale, training, and performance mechanics.
 * Staff directly affect business output quality and customer experience.
 */

// ==================== STAFF ROLES ====================

/**
 * Available staff roles, each with different skill profiles.
 */
enum class StaffRole {
    @SerializedName("COOK") COOK,
    @SerializedName("CASHIER") CASHIER,
    @SerializedName("SERVER") SERVER,
    @SerializedName("CLEANER") CLEANER,
    @SerializedName("MANAGER") MANAGER,
    @SerializedName("SECURITY") SECURITY,
    @SerializedName("SPECIALIST") SPECIALIST
}

/**
 * Training program a staff member can undertake.
 */
enum class TrainingProgram {
    @SerializedName("CUSTOMER_SERVICE") CUSTOMER_SERVICE,   // +reputation on service
    @SerializedName("SPEED") SPEED,                         // serve customers faster
    @SerializedName("UPSELLING") UPSELLING,                 // +revenue per customer
    @SerializedName("HYGIENE") HYGIENE,                     // reduces health inspection risk
    @SerializedName("LEADERSHIP") LEADERSHIP                // boosts nearby staff morale
}

// ==================== MORALE ====================

/**
 * Morale level of a staff member – affects performance multiplier.
 */
enum class MoraleLevel(val multiplier: Float, val label: String) {
    @SerializedName("BURNED_OUT") BURNED_OUT(0.4f, "Burned Out"),
    @SerializedName("LOW") LOW(0.7f, "Low"),
    @SerializedName("NEUTRAL") NEUTRAL(1.0f, "Neutral"),
    @SerializedName("MOTIVATED") MOTIVATED(1.2f, "Motivated"),
    @SerializedName("EXCELLENT") EXCELLENT(1.5f, "Excellent")
}

/**
 * Derive morale level from a 0–100 morale score.
 */
fun moraleLevel(score: Float): MoraleLevel = when {
    score < 20f -> MoraleLevel.BURNED_OUT
    score < 40f -> MoraleLevel.LOW
    score < 60f -> MoraleLevel.NEUTRAL
    score < 80f -> MoraleLevel.MOTIVATED
    else -> MoraleLevel.EXCELLENT
}

// ==================== STAFF MEMBER ====================

/**
 * A staff member employed at a business.
 */
data class StaffMember(
    @SerializedName("staffId") val staffId: String,
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: StaffRole,
    @SerializedName("businessId") val businessId: String,
    @SerializedName("level") var level: Int = 1,
    @SerializedName("experience") var experience: Int = 0,
    @SerializedName("morale") var morale: Float = 70f,
    @SerializedName("weeklySalary") val weeklySalary: Double,
    @SerializedName("completedTraining") val completedTraining: List<TrainingProgram> = emptyList(),
    @SerializedName("activeTraining") var activeTraining: TrainingProgram? = null,
    @SerializedName("trainingCompletesAt") var trainingCompletesAt: Long = 0L,
    @SerializedName("hireTimestamp") val hireTimestamp: Long = System.currentTimeMillis()
) {
    /** Current morale level enum. */
    val moraleLevel: MoraleLevel get() = moraleLevel(morale)

    /**
     * Overall performance multiplier combining level, morale, and training bonuses.
     */
    fun performanceMultiplier(): Float {
        val levelBonus = 1.0f + (level - 1) * 0.15f
        val moraleBonus = moraleLevel.multiplier
        val trainingBonus = completedTraining.fold(1.0f) { acc, t ->
            acc * trainingBonus(t)
        }
        return levelBonus * moraleBonus * trainingBonus
    }

    /**
     * Revenue-per-customer bonus provided by this staff member's role and level.
     */
    fun revenueBonus(): Double = when (role) {
        StaffRole.SERVER -> 0.05 * level
        StaffRole.MANAGER -> 0.10 * level
        StaffRole.SPECIALIST -> 0.15 * level
        else -> 0.0
    } + if (TrainingProgram.UPSELLING in completedTraining) 0.10 else 0.0

    /**
     * Customer throughput bonus (number of extra customers served per hour).
     */
    fun throughputBonus(): Int = when (role) {
        StaffRole.COOK, StaffRole.SERVER -> level * 2
        StaffRole.MANAGER -> level * 3
        else -> level
    } + if (TrainingProgram.SPEED in completedTraining) 5 else 0

    /**
     * Experience required to reach the next level.
     */
    fun xpForNextLevel(): Int = level * 100

    /**
     * Add experience and return true if a level-up occurred.
     */
    fun addExperience(amount: Int): Boolean {
        experience += amount
        val required = xpForNextLevel()
        return if (experience >= required) {
            experience -= required
            level++
            true
        } else false
    }

    /**
     * Adjust morale by delta (clamped to 0–100).
     */
    fun adjustMorale(delta: Float) {
        morale = (morale + delta).coerceIn(0f, 100f)
    }

    /** Check whether active training is complete. */
    fun isTrainingComplete(): Boolean =
        activeTraining != null && System.currentTimeMillis() >= trainingCompletesAt

    private fun trainingBonus(program: TrainingProgram): Float = when (program) {
        TrainingProgram.CUSTOMER_SERVICE -> 1.05f
        TrainingProgram.SPEED -> 1.08f
        TrainingProgram.UPSELLING -> 1.10f
        TrainingProgram.HYGIENE -> 1.03f
        TrainingProgram.LEADERSHIP -> 1.07f
    }
}

// ==================== STAFF ROSTER ====================

/**
 * Base hire cost per role.
 */
fun hireCost(role: StaffRole): Double = when (role) {
    StaffRole.CLEANER -> 200.0
    StaffRole.CASHIER -> 300.0
    StaffRole.COOK -> 400.0
    StaffRole.SERVER -> 350.0
    StaffRole.SECURITY -> 450.0
    StaffRole.MANAGER -> 800.0
    StaffRole.SPECIALIST -> 1200.0
}

/**
 * Base weekly salary per role (scales with level).
 */
fun weeklySalary(role: StaffRole, level: Int): Double {
    val base = when (role) {
        StaffRole.CLEANER -> 50.0
        StaffRole.CASHIER -> 70.0
        StaffRole.COOK -> 100.0
        StaffRole.SERVER -> 80.0
        StaffRole.SECURITY -> 90.0
        StaffRole.MANAGER -> 150.0
        StaffRole.SPECIALIST -> 200.0
    }
    return base * (1.0 + (level - 1) * 0.1)
}

/**
 * Training cost per program.
 */
fun trainingCost(program: TrainingProgram): Double = when (program) {
    TrainingProgram.CUSTOMER_SERVICE -> 150.0
    TrainingProgram.SPEED -> 200.0
    TrainingProgram.UPSELLING -> 250.0
    TrainingProgram.HYGIENE -> 100.0
    TrainingProgram.LEADERSHIP -> 400.0
}

/**
 * Training duration in milliseconds.
 */
fun trainingDurationMs(program: TrainingProgram): Long = when (program) {
    TrainingProgram.CUSTOMER_SERVICE -> 4 * 60 * 60 * 1000L   // 4 hours
    TrainingProgram.SPEED -> 6 * 60 * 60 * 1000L               // 6 hours
    TrainingProgram.UPSELLING -> 8 * 60 * 60 * 1000L           // 8 hours
    TrainingProgram.HYGIENE -> 2 * 60 * 60 * 1000L             // 2 hours
    TrainingProgram.LEADERSHIP -> 12 * 60 * 60 * 1000L         // 12 hours
}

/**
 * Aggregate staff stats for a single business.
 */
data class StaffRoster(
    @SerializedName("businessId") val businessId: String,
    @SerializedName("staff") val staff: List<StaffMember> = emptyList()
) {
    /** Combined performance multiplier (average of all staff). */
    fun averagePerformance(): Float {
        if (staff.isEmpty()) return 0.5f
        return staff.map { it.performanceMultiplier() }.average().toFloat()
    }

    /** Total weekly salary cost. */
    fun totalWeeklySalaryCost(): Double = staff.sumOf { it.weeklySalary }

    /** Average morale score across all staff. */
    fun averageMorale(): Float {
        if (staff.isEmpty()) return 50f
        return staff.map { it.morale }.average().toFloat()
    }

    /** Find staff by ID. */
    fun find(staffId: String): StaffMember? = staff.find { it.staffId == staffId }

    /** Count of staff by role. */
    fun countByRole(role: StaffRole): Int = staff.count { it.role == role }

    /** Check if roster has a manager. */
    fun hasManager(): Boolean = staff.any { it.role == StaffRole.MANAGER }
}
