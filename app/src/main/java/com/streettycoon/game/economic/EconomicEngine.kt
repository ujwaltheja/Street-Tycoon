package com.streettycoon.game.economic

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * Economic Engine
 * 
 * Core simulation engine that manages:
 * - Tick-based updates with configurable speed
 * - Transaction pipeline (income/expense processing)
 * - Event generation and handling
 * - Loan management and interest accrual
 * - Level progression and rewards
 * - State persistence
 */
class EconomicEngine(
    private val config: EconomicConfig = EconomicConfig(),
    private val rng: Random = Random(config.randomSeed)
) {
    companion object {
        private const val TAG = "EconomicEngine"
    }
    
    // Economic state
    private val _economicState = MutableStateFlow(EconomicState())
    val economicState: StateFlow<EconomicState> = _economicState.asStateFlow()
    
    // Transaction history (limited to last 100)
    private val _recentTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val recentTransactions: StateFlow<List<Transaction>> = _recentTransactions.asStateFlow()
    
    // Pending events
    private val _pendingEvents = MutableStateFlow<List<GameEvent>>(emptyList())
    val pendingEvents: StateFlow<List<GameEvent>> = _pendingEvents.asStateFlow()
    
    // Engine state
    private var tickCount = 0L
    private var lastEventCheckTick = 0L
    
    /**
     * Initialize engine with player data
     */
    fun initialize(player: Player) {
        _economicState.value = _economicState.value.copy(player = player)
        Log.d(TAG, "Economic engine initialized for player ${player.playerId}")
    }
    
    /**
     * Main tick function - called at regular intervals
     * @param deltaTimeMs Time elapsed since last tick in milliseconds
     */
    fun tick(deltaTimeMs: Long) {
        tickCount++
        val state = _economicState.value
        
        // Process passive income
        processPassiveIncome(deltaTimeMs)
        
        // Process loan interest
        processLoanInterest()
        
        // Check for random events
        if (shouldGenerateEvent()) {
            generateRandomEvent()
        }
        
        // Process asset depreciation (every 100 ticks)
        if (tickCount % 100 == 0L) {
            processAssetDepreciation()
        }
        
        // Update playtime
        updatePlaytime(deltaTimeMs)
    }
    
    /**
     * Process passive income from assets
     */
    private fun processPassiveIncome(deltaTimeMs: Long) {
        val state = _economicState.value
        val ticksElapsed = deltaTimeMs / config.tickIntervalMs.toDouble()
        
        state.assets.forEach { asset ->
            val income = asset.getNetIncomePerTick() * ticksElapsed
            if (income > 0) {
                addIncome(
                    Income(
                        incomeId = "passive_${System.currentTimeMillis()}",
                        type = IncomeType.STALL_PASSIVE,
                        amount = income,
                        sourceId = asset.assetId,
                        sourceName = asset.name,
                        description = "Passive income"
                    )
                )
            }
        }
    }
    
    /**
     * Process interest on active loans
     */
    private fun processLoanInterest() {
        val state = _economicState.value
        val now = System.currentTimeMillis()
        
        state.activeLoans.forEach { loan ->
            if (loan.isOverdue() && loan.status == LoanStatus.ACTIVE) {
                // Apply penalty for overdue loans
                val penalty = loan.remainingBalance * config.loanOverduePenaltyRate
                addExpense(
                    Expense(
                        expenseId = "penalty_${now}",
                        type = ExpenseType.LOAN_PAYMENT,
                        amount = penalty,
                        targetId = loan.loanId,
                        targetName = "Late Payment Penalty",
                        description = "Penalty for overdue loan"
                    )
                )
            }
        }
    }
    
    /**
     * Check if a random event should be generated
     */
    private fun shouldGenerateEvent(): Boolean {
        val ticksSinceLastEvent = tickCount - lastEventCheckTick
        if (ticksSinceLastEvent < config.minTicksBetweenEvents) {
            return false
        }
        
        return rng.nextFloat() < config.randomEventChance
    }
    
    /**
     * Generate a random event
     */
    private fun generateRandomEvent() {
        val event = GameEvent.generateRandom(rng)
        lastEventCheckTick = tickCount
        
        // Add to pending events
        val current = _pendingEvents.value.toMutableList()
        current.add(event)
        _pendingEvents.value = current
        
        Log.d(TAG, "Generated event: ${event.title} (${event.financialImpact})")
    }
    
    /**
     * Process asset depreciation
     */
    private fun processAssetDepreciation() {
        val state = _economicState.value
        state.assets.forEach { asset ->
            asset.depreciate(config.assetDepreciationRate)
        }
    }
    
    /**
     * Update player playtime
     */
    private fun updatePlaytime(deltaTimeMs: Long) {
        val state = _economicState.value
        state.player.totalPlaytimeSeconds += deltaTimeMs / 1000
        _economicState.value = state
    }
    
    /**
     * Add income to player wallet
     */
    fun addIncome(income: Income): Boolean {
        val state = _economicState.value
        val wallet = state.player.wallet
        
        val newBalance = wallet.deposit(income.amount)
        
        // Add to transaction history
        addTransaction(Transaction.fromIncome(income))
        
        // Award XP for income
        val xpGain = (income.amount * config.xpPerCashEarned).toInt()
        val leveledUp = state.player.addExperience(xpGain)
        
        if (leveledUp) {
            handleLevelUp(state.player.level)
        }
        
        _economicState.value = state
        
        Log.d(TAG, "Added income: ${income.amount} from ${income.sourceName}. New balance: $newBalance")
        return true
    }
    
    /**
     * Add expense and deduct from wallet
     */
    fun addExpense(expense: Expense): Boolean {
        val state = _economicState.value
        val wallet = state.player.wallet
        
        if (!wallet.hasAmount(expense.amount)) {
            Log.w(TAG, "Insufficient funds for expense: ${expense.amount}")
            return false
        }
        
        val success = wallet.withdraw(expense.amount)
        if (success) {
            addTransaction(Transaction.fromExpense(expense))
            _economicState.value = state
            Log.d(TAG, "Added expense: ${expense.amount} for ${expense.targetName}. New balance: ${wallet.balance}")
        }
        
        return success
    }
    
    /**
     * Take out a new loan
     */
    fun takeLoan(principal: Double, term: LoanTerm): Boolean {
        val state = _economicState.value
        
        // Check if player can take more loans
        if (state.activeLoans.size >= config.maxSimultaneousLoans) {
            Log.w(TAG, "Cannot take loan: max loans reached")
            return false
        }
        
        // Check loan amount limits
        if (principal < config.minLoanAmount || principal > config.maxLoanAmount) {
            Log.w(TAG, "Loan amount out of range: $principal")
            return false
        }
        
        // Create loan
        val loan = Loan.create(principal, term)
        
        // Add loan to state
        val newLoans = state.activeLoans + loan
        _economicState.value = state.copy(activeLoans = newLoans)
        
        // Disburse funds
        addIncome(
            Income(
                incomeId = "loan_${loan.loanId}",
                type = IncomeType.LOAN_DISBURSEMENT,
                amount = principal,
                sourceId = loan.loanId,
                sourceName = "Loan (${term.name})",
                description = "Loan disbursement"
            )
        )
        
        Log.d(TAG, "Loan taken: $principal at ${loan.interestRate * 100}% for ${term.name} term")
        return true
    }
    
    /**
     * Make a payment towards a loan
     */
    fun repayLoan(loanId: String, amount: Double): Boolean {
        val state = _economicState.value
        val loan = state.activeLoans.find { it.loanId == loanId } ?: return false
        
        if (!state.player.wallet.hasAmount(amount)) {
            return false
        }
        
        val actualPayment = loan.makePayment(amount)
        
        addExpense(
            Expense(
                expenseId = "repay_${System.currentTimeMillis()}",
                type = ExpenseType.LOAN_PAYMENT,
                amount = actualPayment,
                targetId = loanId,
                targetName = "Loan Payment",
                description = "Loan repayment"
            )
        )
        
        _economicState.value = state
        
        Log.d(TAG, "Loan payment: $actualPayment. Remaining: ${loan.remainingBalance}")
        return true
    }
    
    /**
     * Purchase an asset
     */
    fun purchaseAsset(asset: Asset): Boolean {
        val state = _economicState.value
        
        if (!state.player.wallet.hasAmount(asset.purchasePrice)) {
            return false
        }
        
        val success = addExpense(
            Expense(
                expenseId = "asset_${asset.assetId}",
                type = ExpenseType.STALL_UPGRADE,
                amount = asset.purchasePrice,
                targetId = asset.assetId,
                targetName = asset.name,
                description = "Purchased ${asset.type.name}"
            )
        )
        
        if (success) {
            val newAssets = state.assets + asset
            _economicState.value = state.copy(assets = newAssets)
            Log.d(TAG, "Asset purchased: ${asset.name} for ${asset.purchasePrice}")
        }
        
        return success
    }
    
    /**
     * Handle player level up
     */
    private fun handleLevelUp(newLevel: Int) {
        val levelDef = LevelDefinition.forLevel(newLevel)
        
        // Award cash reward
        addIncome(
            Income(
                incomeId = "levelup_$newLevel",
                type = IncomeType.ACHIEVEMENT,
                amount = levelDef.cashReward,
                sourceId = "level_$newLevel",
                sourceName = "Level $newLevel",
                description = "Level up reward"
            )
        )
        
        Log.d(TAG, "Level up! Now level $newLevel. Reward: ${levelDef.cashReward}")
    }
    
    /**
     * Apply an event to the game state
     */
    fun applyEvent(event: GameEvent): Boolean {
        event.hasBeenShown = true
        
        if (event.financialImpact > 0) {
            addIncome(
                Income(
                    incomeId = event.eventId,
                    type = IncomeType.EVENT_BONUS,
                    amount = event.financialImpact,
                    sourceId = event.eventId,
                    sourceName = event.title,
                    description = event.description
                )
            )
        } else if (event.financialImpact < 0) {
            addExpense(
                Expense(
                    expenseId = event.eventId,
                    type = ExpenseType.EVENT_COST,
                    amount = -event.financialImpact,
                    targetId = event.eventId,
                    targetName = event.title,
                    description = event.description
                )
            )
        }
        
        // Remove from pending
        val current = _pendingEvents.value.toMutableList()
        current.remove(event)
        _pendingEvents.value = current
        
        return true
    }
    
    /**
     * Add transaction to history
     */
    private fun addTransaction(transaction: Transaction) {
        val current = _recentTransactions.value.toMutableList()
        current.add(0, transaction) // Add to front
        
        // Keep only last 100 transactions
        if (current.size > 100) {
            current.removeAt(current.size - 1)
        }
        
        _recentTransactions.value = current
    }
    
    /**
     * Get current state snapshot as JSON
     */
    fun getStateSnapshot(): String {
        return Gson().toJson(_economicState.value)
    }
    
    /**
     * Load state from JSON
     */
    fun loadState(json: String): Boolean {
        return try {
            val state = Gson().fromJson(json, EconomicState::class.java)
            _economicState.value = state
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load state", e)
            false
        }
    }
    
    /**
     * Reset engine to initial state
     */
    fun reset() {
        _economicState.value = EconomicState()
        _recentTransactions.value = emptyList()
        _pendingEvents.value = emptyList()
        tickCount = 0
        lastEventCheckTick = 0
        Log.d(TAG, "Economic engine reset")
    }
}

// ==================== STATE & CONFIG ====================

/**
 * Economic simulation state
 */
data class EconomicState(
    @SerializedName("player") var player: Player = Player("default", "Player"),
    @SerializedName("assets") val assets: List<Asset> = emptyList(),
    @SerializedName("activeLoans") val activeLoans: List<Loan> = emptyList()
)

/**
 * Economic engine configuration
 */
data class EconomicConfig(
    // Tick settings
    @SerializedName("tickIntervalMs") val tickIntervalMs: Long = 100,
    
    // Income/Expense multipliers
    @SerializedName("incomeMultiplier") val incomeMultiplier: Double = 1.0,
    @SerializedName("expenseMultiplier") val expenseMultiplier: Double = 1.0,
    
    // Loan settings
    @SerializedName("minLoanAmount") val minLoanAmount: Double = 100.0,
    @SerializedName("maxLoanAmount") val maxLoanAmount: Double = 50000.0,
    @SerializedName("maxSimultaneousLoans") val maxSimultaneousLoans: Int = 3,
    @SerializedName("loanOverduePenaltyRate") val loanOverduePenaltyRate: Double = 0.02,
    
    // Event settings
    @SerializedName("randomEventChance") val randomEventChance: Float = 0.1f,
    @SerializedName("minTicksBetweenEvents") val minTicksBetweenEvents: Long = 600, // 60 seconds at 100ms ticks
    
    // Asset settings
    @SerializedName("assetDepreciationRate") val assetDepreciationRate: Double = 0.001,
    
    // Progression settings
    @SerializedName("xpPerCashEarned") val xpPerCashEarned: Double = 0.1,
    
    // Random seed for deterministic testing
    @SerializedName("randomSeed") val randomSeed: Long = System.currentTimeMillis()
)

/**
 * Transaction record for UI display
 */
data class Transaction(
    @SerializedName("transactionId") val transactionId: String,
    @SerializedName("type") val type: TransactionType,
    @SerializedName("amount") val amount: Double,
    @SerializedName("description") val description: String,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromIncome(income: Income): Transaction {
            return Transaction(
                transactionId = income.incomeId,
                type = TransactionType.INCOME,
                amount = income.amount,
                description = income.getDisplayText(),
                timestamp = income.timestamp
            )
        }
        
        fun fromExpense(expense: Expense): Transaction {
            return Transaction(
                transactionId = expense.expenseId,
                type = TransactionType.EXPENSE,
                amount = -expense.amount,
                description = expense.getDisplayText(),
                timestamp = expense.timestamp
            )
        }
    }
}

enum class TransactionType {
    @SerializedName("INCOME") INCOME,
    @SerializedName("EXPENSE") EXPENSE
}
