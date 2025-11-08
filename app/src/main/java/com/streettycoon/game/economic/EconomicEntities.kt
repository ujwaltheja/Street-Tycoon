package com.streettycoon.game.economic

import com.google.gson.annotations.SerializedName
import kotlin.random.Random

/**
 * Economic Simulation Entities
 * 
 * Core entities for the Street Tycoon economic simulation system:
 * - Player: Central entity with wallet and progression
 * - Wallet: Financial state management
 * - Income: Revenue sources (stalls, investments, events)
 * - Expense: Cost centers (family, upgrades, operations)
 * - Asset: Owned resources that generate value
 * - Loan: Debt instruments with interest
 * - Event: Random life events affecting finances
 * - Level: Progression system with rewards
 */

// ==================== WALLET & PLAYER ====================

/**
 * Wallet manages player's financial state
 */
data class Wallet(
    @SerializedName("balance") var balance: Double = 100.0,
    @SerializedName("totalEarned") var totalEarned: Double = 0.0,
    @SerializedName("totalSpent") var totalSpent: Double = 0.0,
    @SerializedName("lifetimeEarnings") var lifetimeEarnings: Double = 0.0,
    @SerializedName("lifetimeExpenses") var lifetimeExpenses: Double = 0.0
) {
    /**
     * Add money to wallet
     * @param amount Amount to add (must be positive)
     * @return New balance
     */
    fun deposit(amount: Double): Double {
        require(amount >= 0) { "Deposit amount must be positive" }
        balance += amount
        totalEarned += amount
        lifetimeEarnings += amount
        return balance
    }
    
    /**
     * Remove money from wallet
     * @param amount Amount to remove (must be positive)
     * @return true if successful, false if insufficient funds
     */
    fun withdraw(amount: Double): Boolean {
        require(amount >= 0) { "Withdrawal amount must be positive"}
        
        if (balance < amount) {
            return false
        }
        
        balance -= amount
        totalSpent += amount
        lifetimeExpenses += amount
        return true
    }
    
    /**
     * Check if wallet has sufficient funds
     */
    fun hasAmount(amount: Double): Boolean = balance >= amount
    
    /**
     * Get net profit (total earned - total spent)
     */
    fun getNetProfit(): Double = totalEarned - totalSpent
    
    /**
     * Get financial health ratio (0.0 to 1.0)
     * Returns ratio of balance to total earned
     */
    fun getHealthRatio(): Double {
        if (totalEarned == 0.0) return 1.0
        return (balance / totalEarned).coerceIn(0.0, 1.0)
    }
}

/**
 * Player entity with wallet and progression
 */
data class Player(
    @SerializedName("playerId") val playerId: String,
    @SerializedName("name") var name: String,
    @SerializedName("wallet") val wallet: Wallet = Wallet(),
    @SerializedName("level") var level: Int = 1,
    @SerializedName("experience") var experience: Int = 0,
    @SerializedName("totalPlaytimeSeconds") var totalPlaytimeSeconds: Long = 0,
    @SerializedName("achievementPoints") var achievementPoints: Int = 0,
    @SerializedName("hasFamily") var hasFamily: Boolean = false,
    @SerializedName("childrenCount") var childrenCount: Int = 0
) {
    /**
     * Add experience and check for level up
     * @return true if leveled up, false otherwise
     */
    fun addExperience(amount: Int): Boolean {
        experience += amount
        val requiredXP = getRequiredXP()
        
        if (experience >= requiredXP) {
            experience -= requiredXP
            level++
            return true
        }
        return false
    }
    
    /**
     * Get XP required for next level
     */
    fun getRequiredXP(): Int {
        return (100 * Math.pow(1.5, (level - 1).toDouble())).toInt()
    }
    
    /**
     * Get progress to next level (0.0 to 1.0)
     */
    fun getLevelProgress(): Float {
        val required = getRequiredXP()
        return (experience.toFloat() / required.toFloat()).coerceIn(0f, 1f)
    }
}

// ==================== INCOME SOURCES ====================

/**
 * Income type enumeration
 */
enum class IncomeType {
    @SerializedName("STALL_TAP") STALL_TAP,           // Manual tap income
    @SerializedName("STALL_PASSIVE") STALL_PASSIVE,   // Helper/passive income
    @SerializedName("INVESTMENT") INVESTMENT,          // Asset dividends
    @SerializedName("EVENT_BONUS") EVENT_BONUS,        // Random event rewards
    @SerializedName("ACHIEVEMENT") ACHIEVEMENT,        // Achievement rewards
    @SerializedName("LOAN_DISBURSEMENT") LOAN_DISBURSEMENT  // Loan principal received
}

/**
 * Income source representing revenue
 */
data class Income(
    @SerializedName("incomeId") val incomeId: String,
    @SerializedName("type") val type: IncomeType,
    @SerializedName("amount") val amount: Double,
    @SerializedName("sourceId") val sourceId: String,  // Stall ID, asset ID, etc.
    @SerializedName("sourceName") val sourceName: String,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("description") val description: String = ""
) {
    /**
     * Get display text for UI
     */
    fun getDisplayText(): String {
        return when (type) {
            IncomeType.STALL_TAP -> "Served customer at $sourceName"
            IncomeType.STALL_PASSIVE -> "Passive income from $sourceName"
            IncomeType.INVESTMENT -> "Investment returns from $sourceName"
            IncomeType.EVENT_BONUS -> "Lucky event: $description"
            IncomeType.ACHIEVEMENT -> "Achievement unlocked: $description"
            IncomeType.LOAN_DISBURSEMENT -> "Loan received: $sourceName"
        }
    }
}

// ==================== EXPENSES ====================

/**
 * Expense type enumeration
 */
enum class ExpenseType {
    @SerializedName("FAMILY_HOUSING") FAMILY_HOUSING,
    @SerializedName("FAMILY_TRANSPORT") FAMILY_TRANSPORT,
    @SerializedName("FAMILY_FOOD") FAMILY_FOOD,
    @SerializedName("FAMILY_EDUCATION") FAMILY_EDUCATION,
    @SerializedName("FAMILY_HEALTH") FAMILY_HEALTH,
    @SerializedName("STALL_UPGRADE") STALL_UPGRADE,
    @SerializedName("HELPER_HIRE") HELPER_HIRE,
    @SerializedName("CHARACTER_HIRE") CHARACTER_HIRE,
    @SerializedName("EVENT_COST") EVENT_COST,
    @SerializedName("LOAN_PAYMENT") LOAN_PAYMENT,
    @SerializedName("ZONE_UNLOCK") ZONE_UNLOCK
}

/**
 * Expense representing a cost
 */
data class Expense(
    @SerializedName("expenseId") val expenseId: String,
    @SerializedName("type") val type: ExpenseType,
    @SerializedName("amount") val amount: Double,
    @SerializedName("targetId") val targetId: String,  // What was purchased
    @SerializedName("targetName") val targetName: String,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("isRecurring") val isRecurring: Boolean = false,
    @SerializedName("description") val description: String = ""
) {
    /**
     * Get display text for UI
     */
    fun getDisplayText(): String {
        val prefix = if (isRecurring) "Monthly" else "Paid"
        return when (type) {
            ExpenseType.FAMILY_HOUSING -> "$prefix: Housing ($targetName)"
            ExpenseType.FAMILY_TRANSPORT -> "$prefix: Transport ($targetName)"
            ExpenseType.FAMILY_FOOD -> "$prefix: Food ($targetName)"
            ExpenseType.FAMILY_EDUCATION -> "$prefix: Education ($targetName)"
            ExpenseType.FAMILY_HEALTH -> "$prefix: Health Insurance ($targetName)"
            ExpenseType.STALL_UPGRADE -> "Upgraded $targetName"
            ExpenseType.HELPER_HIRE -> "Hired helper for $targetName"
            ExpenseType.CHARACTER_HIRE -> "Hired $targetName"
            ExpenseType.EVENT_COST -> "Unexpected expense: $description"
            ExpenseType.LOAN_PAYMENT -> "Loan payment: $targetName"
            ExpenseType.ZONE_UNLOCK -> "Unlocked zone: $targetName"
        }
    }
}

// ==================== ASSETS ====================

/**
 * Asset type enumeration
 */
enum class AssetType {
    @SerializedName("STALL") STALL,
    @SerializedName("PROPERTY") PROPERTY,
    @SerializedName("VEHICLE") VEHICLE,
    @SerializedName("EQUIPMENT") EQUIPMENT
}

/**
 * Asset representing owned resources
 */
data class Asset(
    @SerializedName("assetId") val assetId: String,
    @SerializedName("type") val type: AssetType,
    @SerializedName("name") val name: String,
    @SerializedName("purchasePrice") val purchasePrice: Double,
    @SerializedName("currentValue") var currentValue: Double,
    @SerializedName("incomePerTick") val incomePerTick: Double = 0.0,
    @SerializedName("maintenanceCost") val maintenanceCost: Double = 0.0,
    @SerializedName("level") var level: Int = 1,
    @SerializedName("purchaseTimestamp") val purchaseTimestamp: Long = System.currentTimeMillis()
) {
    /**
     * Calculate net income per tick (income - maintenance)
     */
    fun getNetIncomePerTick(): Double = incomePerTick - maintenanceCost
    
    /**
     * Calculate return on investment (%)
     */
    fun getROI(): Double {
        if (purchasePrice == 0.0) return 0.0
        return ((currentValue - purchasePrice) / purchasePrice) * 100.0
    }
    
    /**
     * Depreciate asset value over time
     */
    fun depreciate(depreciationRate: Double) {
        currentValue *= (1.0 - depreciationRate)
        currentValue = currentValue.coerceAtLeast(purchasePrice * 0.1) // Min 10% of purchase price
    }
}

// ==================== LOANS ====================

/**
 * Loan term length
 */
enum class LoanTerm {
    @SerializedName("SHORT") SHORT,      // 7 days, 5% interest
    @SerializedName("MEDIUM") MEDIUM,    // 14 days, 8% interest
    @SerializedName("LONG") LONG         // 30 days, 12% interest
}

/**
 * Loan status
 */
enum class LoanStatus {
    @SerializedName("ACTIVE") ACTIVE,
    @SerializedName("PAID_OFF") PAID_OFF,
    @SerializedName("DEFAULTED") DEFAULTED
}

/**
 * Loan entity for borrowing money
 */
data class Loan(
    @SerializedName("loanId") val loanId: String,
    @SerializedName("principal") val principal: Double,
    @SerializedName("interestRate") val interestRate: Double,  // Decimal (e.g., 0.05 = 5%)
    @SerializedName("term") val term: LoanTerm,
    @SerializedName("remainingBalance") var remainingBalance: Double,
    @SerializedName("startTimestamp") val startTimestamp: Long = System.currentTimeMillis(),
    @SerializedName("dueTimestamp") val dueTimestamp: Long,
    @SerializedName("status") var status: LoanStatus = LoanStatus.ACTIVE,
    @SerializedName("lastPaymentTimestamp") var lastPaymentTimestamp: Long = 0
) {
    /**
     * Calculate total amount to repay (principal + interest)
     */
    fun getTotalRepayment(): Double = principal * (1.0 + interestRate)
    
    /**
     * Calculate interest amount
     */
    fun getInterestAmount(): Double = principal * interestRate
    
    /**
     * Make a payment towards the loan
     * @return Amount actually paid (might be less than requested)
     */
    fun makePayment(amount: Double): Double {
        val actualPayment = amount.coerceAtMost(remainingBalance)
        remainingBalance -= actualPayment
        lastPaymentTimestamp = System.currentTimeMillis()
        
        if (remainingBalance <= 0.0) {
            status = LoanStatus.PAID_OFF
            remainingBalance = 0.0
        }
        
        return actualPayment
    }
    
    /**
     * Check if loan is overdue
     */
    fun isOverdue(): Boolean {
        return status == LoanStatus.ACTIVE && System.currentTimeMillis() > dueTimestamp
    }
    
    /**
     * Get days remaining until due
     */
    fun getDaysRemaining(): Int {
        val now = System.currentTimeMillis()
        val remaining = dueTimestamp - now
        return (remaining / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }
    
    companion object {
        /**
         * Create a new loan with specified term
         */
        fun create(principal: Double, term: LoanTerm): Loan {
            val (interestRate, durationDays) = when (term) {
                LoanTerm.SHORT -> 0.05 to 7
                LoanTerm.MEDIUM -> 0.08 to 14
                LoanTerm.LONG -> 0.12 to 30
            }
            
            val now = System.currentTimeMillis()
            val dueDate = now + (durationDays * 24 * 60 * 60 * 1000L)
            val totalRepayment = principal * (1.0 + interestRate)
            
            return Loan(
                loanId = "loan_${now}",
                principal = principal,
                interestRate = interestRate,
                term = term,
                remainingBalance = totalRepayment,
                startTimestamp = now,
                dueTimestamp = dueDate
            )
        }
    }
}

// ==================== EVENTS ====================

/**
 * Event type enumeration
 */
enum class EventType {
    @SerializedName("POSITIVE") POSITIVE,    // Gain money
    @SerializedName("NEGATIVE") NEGATIVE,    // Lose money
    @SerializedName("NEUTRAL") NEUTRAL       // No financial impact
}

/**
 * Random life event affecting player
 */
data class GameEvent(
    @SerializedName("eventId") val eventId: String,
    @SerializedName("type") val type: EventType,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("financialImpact") val financialImpact: Double,  // Positive or negative
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("hasBeenShown") var hasBeenShown: Boolean = false
) {
    companion object {
        /**
         * Generate a random event
         */
        fun generateRandom(rng: Random = Random.Default): GameEvent {
            val now = System.currentTimeMillis()
            val isPositive = rng.nextFloat() < 0.6  // 60% positive, 40% negative
            
            return if (isPositive) {
                generatePositiveEvent(rng, now)
            } else {
                generateNegativeEvent(rng, now)
            }
        }
        
        private fun generatePositiveEvent(rng: Random, timestamp: Long): GameEvent {
            val events = listOf(
                Triple("Customer Tip", "A happy customer left a generous tip!", rng.nextDouble(50.0, 200.0)),
                Triple("Food Festival", "Local food festival brings extra customers!", rng.nextDouble(100.0, 500.0)),
                Triple("Social Media Fame", "Your stall went viral on social media!", rng.nextDouble(200.0, 800.0)),
                Triple("Bulk Order", "Large catering order received!", rng.nextDouble(300.0, 1000.0)),
                Triple("Competition Win", "Won local street food competition!", rng.nextDouble(500.0, 1500.0))
            )
            
            val (title, desc, amount) = events.random(rng)
            
            return GameEvent(
                eventId = "event_$timestamp",
                type = EventType.POSITIVE,
                title = title,
                description = desc,
                financialImpact = amount,
                timestamp = timestamp
            )
        }
        
        private fun generateNegativeEvent(rng: Random, timestamp: Long): GameEvent {
            val events = listOf(
                Triple("Equipment Repair", "Stove needs urgent repair", -rng.nextDouble(50.0, 150.0)),
                Triple("Health Inspection", "Failed health inspection, paid fine", -rng.nextDouble(100.0, 300.0)),
                Triple("Ingredient Spoilage", "Some ingredients went bad", -rng.nextDouble(75.0, 200.0)),
                Triple("Theft", "Minor theft from cash register", -rng.nextDouble(50.0, 250.0)),
                Triple("Utility Bill", "Unexpected high electricity bill", -rng.nextDouble(100.0, 400.0))
            )
            
            val (title, desc, amount) = events.random(rng)
            
            return GameEvent(
                eventId = "event_$timestamp",
                type = EventType.NEGATIVE,
                title = title,
                description = desc,
                financialImpact = amount,
                timestamp = timestamp
            )
        }
    }
}

// ==================== LEVEL SYSTEM ====================

/**
 * Level definition with rewards
 */
data class LevelDefinition(
    @SerializedName("level") val level: Int,
    @SerializedName("requiredXP") val requiredXP: Int,
    @SerializedName("cashReward") val cashReward: Double,
    @SerializedName("unlocks") val unlocks: List<String> = emptyList(),
    @SerializedName("bonusMultiplier") val bonusMultiplier: Float = 1.0f
) {
    companion object {
        /**
         * Get level definition for specific level
         */
        fun forLevel(level: Int): LevelDefinition {
            val requiredXP = (100 * Math.pow(1.5, (level - 1).toDouble())).toInt()
            val cashReward = 50.0 * Math.pow(1.3, (level - 1).toDouble())
            val bonusMultiplier = 1.0f + (level - 1) * 0.1f
            
            val unlocks = mutableListOf<String>()
            when (level) {
                5 -> unlocks.add("Family System")
                10 -> unlocks.add("Loans System")
                15 -> unlocks.add("Investments")
                20 -> unlocks.add("Premium Stalls")
            }
            
            return LevelDefinition(
                level = level,
                requiredXP = requiredXP,
                cashReward = cashReward,
                unlocks = unlocks,
                bonusMultiplier = bonusMultiplier
            )
        }
    }
}
