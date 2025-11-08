# Street Tycoon - Economic Simulation & Premium UI

## Overview

This document covers the **Economic Simulation Engine** and **Premium UI System** for Street Tycoon. These systems provide:

- Real-time economic simulation with deterministic behavior
- Comprehensive financial management (income, expenses, loans, investments)
- Level progression and XP system
- Random life events
- Premium animated UI components
- Extensive tuning parameters for game balance

## Table of Contents

1. [Quick Start](#quick-start)
2. [Economic Simulation](#economic-simulation)
3. [Tuning Parameters](#tuning-parameters)
4. [UI Components](#ui-components)
5. [Testing](#testing)
6. [Performance](#performance)
7. [Troubleshooting](#troubleshooting)

---

## Quick Start

### Prerequisites

- Android Studio Arctic Fox (2020.3.1) or later
- Kotlin 1.9+
- Gradle 8.0+
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)

### Building

```bash
# Clone the repository
git clone https://github.com/ujwaltheja/Street-Tycoon.git
cd Street-Tycoon

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run economic simulation tests specifically
./gradlew test --tests "com.streettycoon.game.economic.*"
```

### Integration

```kotlin
// Initialize the economic engine
val config = EconomicConfig(
    tickIntervalMs = 100,
    randomEventChance = 0.1f,
    minTicksBetweenEvents = 600
)
val engine = EconomicEngine(config)

// Initialize with player
val player = Player(
    playerId = "player1",
    name = "Player Name",
    wallet = Wallet(balance = 100.0)
)
engine.initialize(player)

// Tick the engine (call from game loop)
engine.tick(deltaTimeMs = 100)

// Observe state changes
engine.economicState.collect { state ->
    // Update UI with new state
    updateBalanceDisplay(state.player.wallet.balance)
}

// Add income
engine.addIncome(Income(
    incomeId = "sale_1",
    type = IncomeType.STALL_TAP,
    amount = 10.0,
    sourceId = "stall_tea",
    sourceName = "Tea Stall"
))

// Add expense
engine.addExpense(Expense(
    expenseId = "upgrade_1",
    type = ExpenseType.STALL_UPGRADE,
    amount = 100.0,
    targetId = "stall_tea",
    targetName = "Tea Stall Upgrade"
))
```

---

## Economic Simulation

### Core Entities

#### Player
Central entity tracking progression and financial state.

```kotlin
data class Player(
    val playerId: String,
    var name: String,
    val wallet: Wallet,
    var level: Int = 1,
    var experience: Int = 0,
    var totalPlaytimeSeconds: Long = 0,
    var achievementPoints: Int = 0,
    var hasFamily: Boolean = false,
    var childrenCount: Int = 0
)
```

**Key Methods**:
- `addExperience(amount: Int): Boolean` - Add XP, returns true if leveled up
- `getRequiredXP(): Int` - Calculate XP needed for next level
- `getLevelProgress(): Float` - Get progress to next level (0.0 to 1.0)

#### Wallet
Manages financial state with deposit/withdraw operations.

```kotlin
data class Wallet(
    var balance: Double = 100.0,
    var totalEarned: Double = 0.0,
    var totalSpent: Double = 0.0,
    var lifetimeEarnings: Double = 0.0,
    var lifetimeExpenses: Double = 0.0
)
```

**Key Methods**:
- `deposit(amount: Double): Double` - Add money, returns new balance
- `withdraw(amount: Double): Boolean` - Remove money, returns success
- `hasAmount(amount: Double): Boolean` - Check sufficient funds
- `getNetProfit(): Double` - Calculate total earned - total spent
- `getHealthRatio(): Double` - Financial health metric (0.0 to 1.0)

#### Loan
Debt instrument with interest and repayment tracking.

```kotlin
data class Loan(
    val loanId: String,
    val principal: Double,
    val interestRate: Double,
    val term: LoanTerm,
    var remainingBalance: Double,
    val startTimestamp: Long,
    val dueTimestamp: Long,
    var status: LoanStatus
)
```

**Loan Terms**:
- **SHORT**: 7 days, 5% interest
- **MEDIUM**: 14 days, 8% interest
- **LONG**: 30 days, 12% interest

**Key Methods**:
- `makePayment(amount: Double): Double` - Make payment, returns actual amount paid
- `getTotalRepayment(): Double` - Principal + interest
- `isOverdue(): Boolean` - Check if payment is overdue
- `getDaysRemaining(): Int` - Days until due date

### Economic Engine

The `EconomicEngine` class manages the simulation lifecycle:

```kotlin
class EconomicEngine(
    config: EconomicConfig,
    rng: Random
) {
    // Main tick function - call every 100ms
    fun tick(deltaTimeMs: Long)
    
    // Financial operations
    fun addIncome(income: Income): Boolean
    fun addExpense(expense: Expense): Boolean
    
    // Loan management
    fun takeLoan(principal: Double, term: LoanTerm): Boolean
    fun repayLoan(loanId: String, amount: Double): Boolean
    
    // Asset management
    fun purchaseAsset(asset: Asset): Boolean
    
    // Event handling
    fun applyEvent(event: GameEvent): Boolean
    
    // Persistence
    fun getStateSnapshot(): String
    fun loadState(json: String): Boolean
    fun reset()
}
```

### Tick Loop

The engine runs on a configurable tick loop (default 100ms):

```
Tick 1: Process passive income from assets
Tick 2: Check loan interest and penalties
Tick 3: Check for random event generation
Every 100 ticks: Process asset depreciation
Every tick: Update playtime tracking
```

**Example Integration**:
```kotlin
// In your game loop or coroutine
val tickJob = viewModelScope.launch {
    while (isActive) {
        val deltaMs = 100L
        engine.tick(deltaMs)
        delay(deltaMs)
    }
}
```

---

## Tuning Parameters

All tuning parameters are defined in `design/game-balance-config.json`. Here are the key sections:

### Economy Settings

#### Starting Values
```json
{
  "starting": {
    "cash": 100,
    "level": 1,
    "experience": 0
  }
}
```

#### Income Multipliers
```json
{
  "income": {
    "tapIncomeBase": 1.0,
    "tapIncomeMultiplier": 0.5,
    "passiveIncomeMultiplier": 0.5,
    "helperIncomeBase": 0.5
  }
}
```

**How to adjust**:
- Increase `tapIncomeBase` to make early game easier
- Increase `passiveIncomeMultiplier` for more idle income
- Adjust `helperIncomeBase` to balance helper value

#### Expense Scaling
```json
{
  "expenses": {
    "upgradeBaseCost": 100,
    "upgradeScalingFactor": 1.15,
    "helperBaseCost": 50,
    "helperScalingFactor": 1.3
  }
}
```

**Formula**: `cost = baseCost * (scalingFactor ^ count)`

**How to adjust**:
- Lower `scalingFactor` for gentler progression curve
- Increase `baseCost` to slow down early progression
- Balance with income rates for desired progression speed

### Loan System

```json
{
  "loans": {
    "terms": {
      "short": {
        "durationDays": 7,
        "interestRate": 0.05,
        "minAmount": 100,
        "maxAmount": 10000
      }
    },
    "limits": {
      "maxSimultaneousLoans": 3,
      "maxTotalDebt": 100000
    },
    "penalties": {
      "lateFeeRate": 0.02,
      "gracePeriodDays": 2
    }
  }
}
```

**How to adjust**:
- Lower `interestRate` to make loans more attractive
- Increase `maxSimultaneousLoans` for more complex debt management
- Adjust `gracePeriodDays` for forgiveness/challenge balance

### Progression System

```json
{
  "levels": {
    "xpBase": 100,
    "xpScalingFactor": 1.5,
    "maxLevel": 100,
    "xpPerCashEarned": 0.1
  },
  "rewards": {
    "cashRewardBase": 50,
    "cashRewardScaling": 1.3,
    "bonusMultiplierPerLevel": 0.1
  }
}
```

**XP Formula**: `xpRequired = xpBase * (xpScalingFactor ^ (level - 1))`

**How to adjust**:
- Lower `xpScalingFactor` (e.g., 1.3) for faster leveling
- Increase `xpPerCashEarned` to reward high-income strategies
- Adjust `cashRewardBase` to balance level-up rewards

### Random Events

```json
{
  "events": {
    "generation": {
      "baseChancePerTick": 0.001,
      "minTicksBetweenEvents": 600
    },
    "distribution": {
      "positiveWeight": 0.6,
      "negativeWeight": 0.4
    },
    "impact": {
      "positiveMin": 50,
      "positiveMax": 1500,
      "negativeMin": 50,
      "negativeMax": 500
    }
  }
}
```

**How to adjust**:
- Increase `baseChancePerTick` for more frequent events
- Adjust `positiveWeight` to make game easier/harder
- Scale `positiveMax` with player level for balanced impact
- Decrease `minTicksBetweenEvents` for more chaos

### Difficulty Presets

```json
{
  "difficulty": {
    "easy": {
      "incomeMultiplier": 1.5,
      "expenseMultiplier": 0.75,
      "xpMultiplier": 1.5
    },
    "normal": {
      "incomeMultiplier": 1.0,
      "expenseMultiplier": 1.0,
      "xpMultiplier": 1.0
    },
    "hard": {
      "incomeMultiplier": 0.75,
      "expenseMultiplier": 1.25,
      "xpMultiplier": 0.8
    }
  }
}
```

**How to apply**:
```kotlin
val config = EconomicConfig(
    incomeMultiplier = difficultySettings["incomeMultiplier"],
    expenseMultiplier = difficultySettings["expenseMultiplier"]
)
```

---

## UI Components

### Design Tokens

All design tokens are defined in `design/design-tokens.json`:

- **Colors**: Primary, semantic, game-specific, dark mode variants
- **Typography**: Font families, sizes, weights, line heights
- **Spacing**: Scale from 0dp to 96dp
- **Animations**: Durations, easings, keyframes
- **Components**: Variants and states for all UI elements

### Key Components

#### WalletCard
Displays current balance with animated count-up.

**Variants**:
- `default`: Standard card with border
- `elevated`: Higher elevation, no border
- `glass`: Translucent with blur effect

**Usage**:
```kotlin
WalletCard(
    balance = currentBalance,
    variant = WalletCardVariant.ELEVATED,
    onTap = { /* navigate to wallet details */ }
)
```

#### TransactionList
Scrollable list of recent transactions with slide-in animation.

**Props**:
- `transactions: List<Transaction>`
- `variant: TransactionListVariant` (default, compact)
- `onTransactionTap: (Transaction) -> Unit`

#### BalanceHeader
Top bar showing current balance and quick actions.

**Props**:
- `balance: Double`
- `level: Int`
- `onMenuClick: () -> Unit`

#### LevelBadge
Circular badge displaying player level with XP progress.

**Variants**:
- `default`: 48dp size
- `small`: 32dp size
- `large`: 64dp size with prominent styling

---

## Testing

### Unit Tests

**Economic Entities** (26 tests):
```bash
./gradlew test --tests "com.streettycoon.game.economic.EconomicEntitiesTest"
```

Tests cover:
- Wallet deposit/withdraw operations
- Player XP and level-up logic
- Loan payment calculations
- Asset ROI and depreciation
- Random event generation (with seeded RNG)

**Economic Engine** (30 tests):
```bash
./gradlew test --tests "com.streettycoon.game.economic.EconomicEngineTest"
```

Tests cover:
- Transaction pipeline
- Tick-based income processing
- Loan lifecycle management
- Asset purchase and passive income
- Event application
- State persistence
- Deterministic behavior

### Deterministic Testing

All tests use seeded random number generation for reproducibility:

```kotlin
val config = EconomicConfig(randomSeed = 12345L)
val engine = EconomicEngine(config, Random(12345L))
```

This ensures:
- ✅ Same inputs always produce same outputs
- ✅ Tests don't have false positives/negatives
- ✅ Easier to debug failures
- ✅ Reproducible bug reports

### Running All Tests

```bash
# Run all economic simulation tests
./gradlew test --tests "com.streettycoon.game.economic.*"

# Run with detailed output
./gradlew test --tests "com.streettycoon.game.economic.*" --info

# Generate test report
./gradlew test
# Open: app/build/reports/tests/test/index.html
```

### Example Test

```kotlin
@Test
fun `loan payment reduces balance and loan amount`() = runBlocking {
    // Setup
    engine.takeLoan(500.0, LoanTerm.SHORT)
    val state = engine.economicState.first()
    val loan = state.activeLoans[0]
    
    // Execute
    val success = engine.repayLoan(loan.loanId, 200.0)
    
    // Verify
    assertTrue(success)
    val updatedState = engine.economicState.first()
    assertEquals(325.0, updatedState.activeLoans[0].remainingBalance, 0.01)
    assertEquals(1300.0, updatedState.player.wallet.balance, 0.01)
}
```

---

## Performance

### Target Metrics

- **Frame Rate**: 60fps minimum
- **Tick Latency**: <10ms per tick
- **Memory**: <50MB for economic engine
- **Battery**: Minimal impact on background operation

### Optimization Tips

1. **Tick Rate**: Adjust `tickIntervalMs` based on device capability
   ```kotlin
   val config = EconomicConfig(
       tickIntervalMs = if (isLowEndDevice) 200 else 100
   )
   ```

2. **Event Generation**: Reduce `randomEventChance` on slower devices

3. **Transaction History**: Limited to 100 recent transactions (configurable)

4. **Asset Count**: Consider capping active assets at 50 for smooth performance

### Monitoring

```kotlin
// Log performance metrics
Log.d("EconomicEngine", "Tick time: ${tickDurationMs}ms")
Log.d("EconomicEngine", "Active loans: ${state.activeLoans.size}")
Log.d("EconomicEngine", "Assets: ${state.assets.size}")
```

---

## Troubleshooting

### Common Issues

#### Issue: Balance not updating in UI

**Solution**: Ensure you're collecting the state flow:
```kotlin
LaunchedEffect(Unit) {
    engine.economicState.collect { state ->
        updateBalance(state.player.wallet.balance)
    }
}
```

#### Issue: Tests failing inconsistently

**Solution**: Use fixed random seed in tests:
```kotlin
val engine = EconomicEngine(
    config = EconomicConfig(randomSeed = 12345L),
    rng = Random(12345L)
)
```

#### Issue: Loans not generating interest

**Solution**: Ensure `processLoanInterest()` is called during tick:
```kotlin
// This happens automatically in engine.tick()
engine.tick(100)
```

#### Issue: Events not generating

**Solution**: Check configuration:
```kotlin
val config = EconomicConfig(
    randomEventChance = 0.1f,  // 10% chance per eligible tick
    minTicksBetweenEvents = 600  // At least 60 seconds between events
)
```

### Debug Mode

Enable debug logging:
```kotlin
// In EconomicEngine
companion object {
    private const val TAG = "EconomicEngine"
    private const val DEBUG = true  // Set to true
}
```

---

## Acceptance Checklist

### Phase 1: Design System ✅
- [x] Design tokens JSON created with comprehensive system
- [x] Color palette with AA contrast compliance
- [x] Typography scale with accessible font sizes
- [x] Spacing and elevation systems defined
- [x] Component variants specified
- [x] Dark mode support included

### Phase 2: Economic Simulation ✅
- [x] Core entities implemented (Player, Wallet, Income, Expense, Asset, Loan, Event)
- [x] Economic engine with tick loop
- [x] Transaction pipeline
- [x] Event generator
- [x] Game balance config file
- [x] 56 unit tests passing (26 + 30)
- [x] Deterministic behavior with seeded RNG

### Phase 3: UI Components ⏳
- [ ] WalletCard component
- [ ] TransactionList component
- [ ] BalanceHeader component
- [ ] LevelBadge component
- [ ] MapNode component
- [ ] LoanModal component

### Phase 4: Animations ⏳
- [ ] Balance count-up animation
- [ ] Transaction slide animation
- [ ] Level-up celebration
- [ ] Map reveal animation
- [ ] Button press interactions

### Phase 5: Integration ⏳
- [ ] Hook economic engine to GameViewModel
- [ ] Real-time UI updates
- [ ] Persistence integration
- [ ] Performance profiling
- [ ] Accessibility validation

---

## Next Steps

1. **Implement UI Components**: Create Compose components based on design tokens
2. **Add Animations**: Implement animation specs from `ANIMATION_SPECIFICATIONS.md`
3. **Integration Testing**: Connect economic engine to existing game systems
4. **Performance Testing**: Profile on low/mid/high-end devices
5. **User Testing**: Gather feedback on game balance and progression

---

## Resources

- **Design Tokens**: `design/design-tokens.json`
- **Game Balance**: `design/game-balance-config.json`
- **Icon Specs**: `design/ICON_SPECIFICATION.md`
- **Animation Specs**: `design/ANIMATION_SPECIFICATIONS.md`
- **Main README**: `README.md`

## Support

For questions or issues:
- GitHub Issues: https://github.com/ujwaltheja/Street-Tycoon/issues
- Documentation: See `docs/` directory
- Tests: See `app/src/test/java/com/streettycoon/game/economic/`

---

**Built with ❤️ for educational and entertaining gameplay**
