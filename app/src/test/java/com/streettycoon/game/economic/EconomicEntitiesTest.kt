package com.streettycoon.game.economic

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Unit tests for Economic Entities
 * Tests core financial calculations and business logic
 */
class EconomicEntitiesTest {
    
    private lateinit var wallet: Wallet
    private lateinit var player: Player
    
    @Before
    fun setup() {
        wallet = Wallet(balance = 100.0)
        player = Player(playerId = "test", name = "Test Player")
    }
    
    // ==================== WALLET TESTS ====================
    
    @Test
    fun `wallet deposit increases balance`() {
        val newBalance = wallet.deposit(50.0)
        assertEquals(150.0, newBalance, 0.01)
        assertEquals(150.0, wallet.balance, 0.01)
        assertEquals(50.0, wallet.totalEarned, 0.01)
    }
    
    @Test
    fun `wallet withdraw with sufficient funds succeeds`() {
        val success = wallet.withdraw(50.0)
        assertTrue(success)
        assertEquals(50.0, wallet.balance, 0.01)
        assertEquals(50.0, wallet.totalSpent, 0.01)
    }
    
    @Test
    fun `wallet withdraw with insufficient funds fails`() {
        val success = wallet.withdraw(150.0)
        assertFalse(success)
        assertEquals(100.0, wallet.balance, 0.01) // Balance unchanged
        assertEquals(0.0, wallet.totalSpent, 0.01) // No spending recorded
    }
    
    @Test
    fun `wallet hasAmount checks correctly`() {
        assertTrue(wallet.hasAmount(50.0))
        assertTrue(wallet.hasAmount(100.0))
        assertFalse(wallet.hasAmount(150.0))
    }
    
    @Test
    fun `wallet getNetProfit calculates correctly`() {
        wallet.deposit(100.0)
        wallet.withdraw(30.0)
        assertEquals(70.0, wallet.getNetProfit(), 0.01)
    }
    
    @Test
    fun `wallet getHealthRatio calculates correctly`() {
        wallet.deposit(100.0) // Total earned = 100, balance = 200
        val healthRatio = wallet.getHealthRatio()
        assertEquals(2.0, healthRatio, 0.01) // Clamped to 1.0
        assertEquals(1.0, healthRatio.coerceIn(0.0, 1.0), 0.01)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `wallet deposit negative amount throws exception`() {
        wallet.deposit(-10.0)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `wallet withdraw negative amount throws exception`() {
        wallet.withdraw(-10.0)
    }
    
    // ==================== PLAYER TESTS ====================
    
    @Test
    fun `player addExperience without level up`() {
        val leveledUp = player.addExperience(50)
        assertFalse(leveledUp)
        assertEquals(1, player.level)
        assertEquals(50, player.experience)
    }
    
    @Test
    fun `player addExperience with level up`() {
        val requiredXP = player.getRequiredXP() // 100 for level 1
        val leveledUp = player.addExperience(requiredXP)
        assertTrue(leveledUp)
        assertEquals(2, player.level)
        assertEquals(0, player.experience) // Overflow XP consumed
    }
    
    @Test
    fun `player addExperience with overflow XP`() {
        val requiredXP = player.getRequiredXP() // 100 for level 1
        player.addExperience(requiredXP + 25) // 125 XP
        assertEquals(2, player.level)
        assertEquals(25, player.experience) // 25 XP carried over
    }
    
    @Test
    fun `player getRequiredXP scales correctly`() {
        assertEquals(100, player.getRequiredXP())
        player.level = 2
        assertEquals(150, player.getRequiredXP()) // 100 * 1.5^1
        player.level = 3
        assertEquals(225, player.getRequiredXP()) // 100 * 1.5^2
    }
    
    @Test
    fun `player getLevelProgress calculates correctly`() {
        player.experience = 50
        val progress = player.getLevelProgress()
        assertEquals(0.5f, progress, 0.01f) // 50/100
    }
    
    // ==================== LOAN TESTS ====================
    
    @Test
    fun `loan getTotalRepayment calculates correctly`() {
        val loan = Loan.create(1000.0, LoanTerm.SHORT)
        assertEquals(1050.0, loan.getTotalRepayment(), 0.01) // 1000 * 1.05
    }
    
    @Test
    fun `loan getInterestAmount calculates correctly`() {
        val loan = Loan.create(1000.0, LoanTerm.SHORT)
        assertEquals(50.0, loan.getInterestAmount(), 0.01) // 1000 * 0.05
    }
    
    @Test
    fun `loan makePayment full repayment`() {
        val loan = Loan.create(1000.0, LoanTerm.SHORT)
        val totalOwed = loan.getTotalRepayment()
        
        val paid = loan.makePayment(totalOwed)
        assertEquals(totalOwed, paid, 0.01)
        assertEquals(0.0, loan.remainingBalance, 0.01)
        assertEquals(LoanStatus.PAID_OFF, loan.status)
    }
    
    @Test
    fun `loan makePayment partial repayment`() {
        val loan = Loan.create(1000.0, LoanTerm.SHORT)
        
        val paid = loan.makePayment(500.0)
        assertEquals(500.0, paid, 0.01)
        assertEquals(550.0, loan.remainingBalance, 0.01) // 1050 - 500
        assertEquals(LoanStatus.ACTIVE, loan.status)
    }
    
    @Test
    fun `loan makePayment overpayment caps at remaining balance`() {
        val loan = Loan.create(1000.0, LoanTerm.SHORT)
        
        val paid = loan.makePayment(2000.0) // Try to pay more than owed
        assertEquals(loan.getTotalRepayment(), paid, 0.01)
        assertEquals(0.0, loan.remainingBalance, 0.01)
        assertEquals(LoanStatus.PAID_OFF, loan.status)
    }
    
    @Test
    fun `loan term configurations are correct`() {
        val shortLoan = Loan.create(1000.0, LoanTerm.SHORT)
        assertEquals(0.05, shortLoan.interestRate, 0.001)
        
        val mediumLoan = Loan.create(1000.0, LoanTerm.MEDIUM)
        assertEquals(0.08, mediumLoan.interestRate, 0.001)
        
        val longLoan = Loan.create(1000.0, LoanTerm.LONG)
        assertEquals(0.12, longLoan.interestRate, 0.001)
    }
    
    // ==================== ASSET TESTS ====================
    
    @Test
    fun `asset getNetIncomePerTick calculates correctly`() {
        val asset = Asset(
            assetId = "test",
            type = AssetType.STALL,
            name = "Test Stall",
            purchasePrice = 1000.0,
            currentValue = 1000.0,
            incomePerTick = 10.0,
            maintenanceCost = 2.0
        )
        
        assertEquals(8.0, asset.getNetIncomePerTick(), 0.01)
    }
    
    @Test
    fun `asset getROI calculates correctly`() {
        val asset = Asset(
            assetId = "test",
            type = AssetType.STALL,
            name = "Test Stall",
            purchasePrice = 1000.0,
            currentValue = 1200.0
        )
        
        assertEquals(20.0, asset.getROI(), 0.01) // (1200-1000)/1000 * 100
    }
    
    @Test
    fun `asset depreciate reduces value`() {
        val asset = Asset(
            assetId = "test",
            type = AssetType.STALL,
            name = "Test Stall",
            purchasePrice = 1000.0,
            currentValue = 1000.0
        )
        
        asset.depreciate(0.1) // 10% depreciation
        assertEquals(900.0, asset.currentValue, 0.01)
    }
    
    @Test
    fun `asset depreciate has minimum value floor`() {
        val asset = Asset(
            assetId = "test",
            type = AssetType.STALL,
            name = "Test Stall",
            purchasePrice = 1000.0,
            currentValue = 150.0
        )
        
        asset.depreciate(0.5) // 50% depreciation
        // Should stop at 10% of purchase price (100)
        assertEquals(100.0, asset.currentValue, 0.01)
    }
    
    // ==================== EVENT TESTS ====================
    
    @Test
    fun `GameEvent generateRandom creates valid positive events`() {
        val rng = Random(12345) // Fixed seed for determinism
        
        // Generate multiple events, should get some positive
        var positiveCount = 0
        repeat(100) {
            val event = GameEvent.generateRandom(rng)
            if (event.type == EventType.POSITIVE) {
                positiveCount++
                assertTrue(event.financialImpact > 0)
                assertNotNull(event.title)
                assertNotNull(event.description)
            }
        }
        
        // With 60% positive weight, should have roughly 60 positive events
        assertTrue(positiveCount in 50..70) // Allow some variance
    }
    
    @Test
    fun `GameEvent generateRandom creates valid negative events`() {
        val rng = Random(12345) // Fixed seed
        
        var negativeCount = 0
        repeat(100) {
            val event = GameEvent.generateRandom(rng)
            if (event.type == EventType.NEGATIVE) {
                negativeCount++
                assertTrue(event.financialImpact < 0)
                assertNotNull(event.title)
                assertNotNull(event.description)
            }
        }
        
        // With 40% negative weight, should have roughly 40 negative events
        assertTrue(negativeCount in 30..50)
    }
    
    // ==================== INCOME/EXPENSE TESTS ====================
    
    @Test
    fun `Income getDisplayText formats correctly`() {
        val income = Income(
            incomeId = "test",
            type = IncomeType.STALL_TAP,
            amount = 10.0,
            sourceId = "stall1",
            sourceName = "Tea Stall"
        )
        
        val displayText = income.getDisplayText()
        assertTrue(displayText.contains("Tea Stall"))
    }
    
    @Test
    fun `Expense getDisplayText formats correctly`() {
        val expense = Expense(
            expenseId = "test",
            type = ExpenseType.STALL_UPGRADE,
            amount = 100.0,
            targetId = "stall1",
            targetName = "Tea Stall"
        )
        
        val displayText = expense.getDisplayText()
        assertTrue(displayText.contains("Upgraded"))
        assertTrue(displayText.contains("Tea Stall"))
    }
    
    // ==================== LEVEL DEFINITION TESTS ====================
    
    @Test
    fun `LevelDefinition forLevel scales correctly`() {
        val level1 = LevelDefinition.forLevel(1)
        assertEquals(100, level1.requiredXP)
        assertEquals(50.0, level1.cashReward, 1.0)
        
        val level2 = LevelDefinition.forLevel(2)
        assertEquals(150, level2.requiredXP)
        assertTrue(level2.cashReward > level1.cashReward)
        
        val level5 = LevelDefinition.forLevel(5)
        assertTrue(level5.unlocks.contains("Family System"))
    }
}
