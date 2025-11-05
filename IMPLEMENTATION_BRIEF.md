# Street Tycoon: Complete Implementation Brief & Analysis

**Repository**: https://github.com/ujwaltheja/Street-Tycoon
**Analysis Date**: November 2025
**Branch**: `claude/street-tycoon-analysis-features-011CUpwiEqm5qLyPk3wcNaQh`

---

## Executive Summary

Street Tycoon is a **hybrid Kotlin/C++ incremental tycoon game** with a solid technical foundation. The current codebase (~834 lines of C++, extensive Kotlin UI) demonstrates good architectural patterns with clear separation between UI and game logic. However, to transform this from an MVP into a compelling, retention-focused game with educational value, **5 major feature sets** need implementation along with critical architectural improvements.

### Current State Assessment

**✅ Strengths**:
- Clean hybrid architecture (Kotlin UI + C++ simulation)
- Deterministic game simulation with tick-based updates
- Offline-first persistence with Room database
- Modern UI with Jetpack Compose
- JNI bridge with minimal surface area (7 methods)
- Multi-language support (EN, HI, KN)
- Monetization hooks (AdMob, Google Play Billing)

**⚠️ Critical Gaps**:
1. **No progression gating** - Maps unlock purely by cash threshold
2. **No character system** - Helpers are anonymous, no progression
3. **No real-life simulation** - Pure business, missing educational depth
4. **Minimal UI polish** - Basic Compose, no animations or rich interactions
5. **No audio** - Silent gameplay, missing immersion
6. **No tests** - Zero unit/integration tests found
7. **No CI/CD** - No automated build/test pipeline

---

## Part 1: Code Quality & Technical Debt Analysis

### 1.1 Critical Issues (Fix Immediately)

#### Issue #1: Missing Unit Tests
**Severity**: 🔴 CRITICAL
**Impact**: Cannot validate correctness, high regression risk
**Location**: No test files found in `app/src/test/`

**Recommendation**:
```kotlin
// Add GameSimulation tests
@Test fun `tapServe increases playerCash`()
@Test fun `upgrade cost increases exponentially`()
@Test fun `offline earnings capped at 4 hours`()

// Add C++ tests with Google Test
TEST(GameSimulation, TickIncreasesPassiveIncome)
TEST(GameSimulation, SerializationRoundTrip)
```

#### Issue #2: JNI Error Handling
**Severity**: 🔴 CRITICAL
**Impact**: App crashes on native exceptions
**Location**: `jni_bridge.cpp` - no try-catch around native calls

**Current**:
```cpp
// jni_bridge.cpp - unsafe
JNIEXPORT jstring JNICALL Java_..._nativeGetSnapshot(...) {
    auto* sim = reinterpret_cast<GameSimulation*>(handle);
    std::string json = sim->getSnapshot();  // Can throw
    return env->NewStringUTF(json.c_str());
}
```

**Fix Required**:
```cpp
JNIEXPORT jstring JNICALL Java_..._nativeGetSnapshot(...) {
    try {
        auto* sim = reinterpret_cast<GameSimulation*>(handle);
        if (!sim) {
            // Log error, return default JSON
            return env->NewStringUTF("{}");
        }
        std::string json = sim->getSnapshot();
        return env->NewStringUTF(json.c_str());
    } catch (const std::exception& e) {
        // Log to Android logcat
        __android_log_print(ANDROID_LOG_ERROR, "StreetTycoon",
                            "Native error: %s", e.what());
        return env->NewStringUTF("{}");
    }
}
```

#### Issue #3: Race Condition in Tick Loop
**Severity**: 🟠 HIGH
**Impact**: Potential state corruption if save happens during tick
**Location**: `GameViewModel.kt` - tick loop and auto-save run concurrently

**Recommendation**:
```kotlin
class GameViewModel {
    private val stateMutex = Mutex()

    private fun startTickLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                stateMutex.withLock {
                    simulation.tick(deltaTime)
                    _gameState.value = simulation.getSnapshot()
                }
                delay(100)
            }
        }
    }

    private fun saveGame() {
        viewModelScope.launch(Dispatchers.IO) {
            stateMutex.withLock {
                repository.saveGame(simulation)
            }
        }
    }
}
```

#### Issue #4: Memory Leak in Native Pointer Lifecycle
**Severity**: 🟠 HIGH
**Impact**: Memory leak if ViewModel cleared without cleanup
**Location**: `GameSimulation.kt` - nativeHandle not destroyed in all cases

**Fix Required**:
```kotlin
class GameSimulation {
    private var nativeHandle: Long = 0L

    init {
        nativeHandle = nativeCreate()
    }

    // Add cleanup method
    fun destroy() {
        if (nativeHandle != 0L) {
            nativeDestroy(nativeHandle)
            nativeHandle = 0L
        }
    }

    protected fun finalize() {
        destroy()  // Safety net
    }
}

// In GameViewModel
override fun onCleared() {
    simulation.destroy()
    super.onCleared()
}
```

### 1.2 Medium-Priority Issues

#### Issue #5: No State Migration Strategy
**Severity**: 🟡 MEDIUM
**Impact**: Breaking changes force user data loss

**Recommendation**:
```cpp
struct GameState {
    int version;  // Already present

    bool migrate(int fromVersion, int toVersion) {
        if (fromVersion == 1 && toVersion == 2) {
            // Add new fields, set defaults
            return true;
        }
        return false;
    }
};
```

#### Issue #6: Hardcoded Game Balance
**Severity**: 🟡 MEDIUM
**Impact**: Cannot A/B test or adjust without app update

**Recommendation**: Move to JSON config files in `assets/game_config.json`

#### Issue #7: No Logging/Analytics
**Severity**: 🟡 MEDIUM
**Impact**: Cannot debug production issues or track user behavior

**Recommendation**: Add Firebase Analytics for key events

### 1.3 Performance Issues

#### Issue #8: Full State Serialization Every Tick
**Severity**: 🟠 HIGH (for scalability)
**Impact**: JSON serialization overhead grows with game state size

**Current**: 100ms tick → full JSON serialize → parse in Kotlin
**Optimization**: Delta updates or binary protocol

#### Issue #9: No Frame Budget Management
**Severity**: 🟡 MEDIUM
**Impact**: Stuttering on low-end devices

**Recommendation**: Reduce tick frequency on low-end devices (200ms instead of 100ms)

---

## Part 2: Feature Implementation Plan

### Priority Matrix

| Priority | Feature | Complexity | Time Estimate | Impact |
|----------|---------|------------|---------------|--------|
| **P0** | Map Gate System | Medium | 3-4 days | 🔥 Retention |
| **P0** | Fix Critical Bugs | Medium | 2 days | 🔥 Stability |
| **P1** | Character System | High | 5-6 days | ⭐ Engagement |
| **P1** | Music & Sound | Low | 2 days | ⭐ Immersion |
| **P2** | Family Spending | High | 6-7 days | 💡 Educational |
| **P2** | UI Enhancements | Medium | 4-5 days | 💡 Polish |
| **P3** | Tests & CI | Medium | 3-4 days | 🛡️ Quality |

---

## Part 3: Detailed Feature Specifications

### Feature A: Map Progression Lock System

**Goal**: Enforce linear progression with achievement-based unlocks

#### Requirements
1. Each map requires completing **4 gates** before unlock
2. Gate types: Upgrades, Helpers, Earnings, Playtime
3. Visual progress indicators in UI
4. Native validation to prevent hacking

#### Data Schema Changes

**C++ Side** (`game_state.h`):
```cpp
enum class GateType {
    UPGRADES_COMPLETED,
    HELPERS_HIRED,
    EARNINGS_THRESHOLD,
    PLAYTIME_HOURS
};

struct MapGate {
    GateType type;
    int targetValue;
    int currentValue;
    bool isCompleted;
    std::string description;

    MapGate() : targetValue(0), currentValue(0), isCompleted(false) {}

    void updateProgress(int newValue) {
        currentValue = std::max(currentValue, newValue);
        isCompleted = (currentValue >= targetValue);
    }
};

struct Zone {
    int id;
    std::string name;
    bool isUnlocked;
    double unlockCost;
    std::vector<MapGate> gates;  // NEW

    bool checkAllGatesComplete() const {
        for (const auto& gate : gates) {
            if (!gate.isCompleted) return false;
        }
        return true;
    }
};
```

**Kotlin Side** (`GameModels.kt`):
```kotlin
enum class GateType {
    UPGRADES_COMPLETED,
    HELPERS_HIRED,
    EARNINGS_THRESHOLD,
    PLAYTIME_HOURS
}

data class MapGate(
    val type: GateType,
    val targetValue: Int,
    val currentValue: Int = 0,
    val isCompleted: Boolean = false,
    val description: String
) {
    val progress: Float
        get() = (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)
}

data class Zone(
    val id: Int,
    val name: String,
    val isUnlocked: Boolean,
    val unlockCost: Double,
    val gates: List<MapGate> = emptyList()  // NEW
) {
    fun allGatesComplete(): Boolean = gates.all { it.isCompleted }
}
```

#### UI Components

**MapGateProgressCard**:
```kotlin
@Composable
fun MapGateProgressCard(gate: MapGate, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gate.isCompleted) Color(0xFFE8F5E9) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    gate.description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { gate.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (gate.isCompleted) Color(0xFF4CAF50) else Color(0xFF2196F3)
                )

                Text(
                    "${gate.currentValue} / ${gate.targetValue}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (gate.isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
```

#### Implementation Checklist
- [ ] Add `MapGate` struct to C++ `game_state.h`
- [ ] Update JSON serializer to handle gates
- [ ] Implement gate validation in `GameSimulation::checkMapUnlock()`
- [ ] Add Kotlin data classes for gates
- [ ] Create `MapGateProgressCard` composable
- [ ] Update `MapScreen` to show gate progress
- [ ] Add gate progress updates in tick loop
- [ ] Write tests for gate completion logic

---

### Feature B: Real-Life Family Spending System

**Goal**: Educational money management simulation with personal life events

#### Core Mechanics

**Spending Categories**:
1. **Housing**: Small Room → Apartment → House → Villa (₹500-7500/month)
2. **Transportation**: Walking → Scooter → Car → Luxury Car (₹0-15000/month)
3. **Food**: Basic → Premium (₹300-1000/month)
4. **Education**: School → College → Coaching (₹2000-5000/month per child)
5. **Health**: Insurance + Emergency fund (₹1000-3000/month)

**Life Events**:
- **Marriage** (Day 7+): ₹10k-50k one-time, adds spouse
- **Baby Born** (Day 30+ if married): ₹5k + ₹1.5k/month ongoing
- **Education Milestone**: Unlocks business bonuses
- **Health Emergency**: Random event requiring immediate payment

#### Data Schema

**C++ Side**:
```cpp
struct FamilyMember {
    std::string memberId;
    std::string name;
    std::string relation;  // "spouse", "child", "parent"
    int age;
    double monthlyExpense;
    float happiness;  // 0-100

    FamilyMember() : age(0), monthlyExpense(0), happiness(100.0f) {}
};

struct SpendingCategory {
    std::string categoryId;
    std::string name;
    double monthlyExpense;
    int level;  // 0-3
    double nextUpgradeCost;

    SpendingCategory() : monthlyExpense(0), level(0), nextUpgradeCost(0) {}
};

struct FamilyState {
    std::vector<FamilyMember> members;
    std::vector<SpendingCategory> categories;
    double totalMonthlyExpense;
    float averageHappiness;
    double savingsBalance;

    FamilyState() : totalMonthlyExpense(0), averageHappiness(100.0f),
                    savingsBalance(0) {}

    void calculateMonthlyExpense() {
        totalMonthlyExpense = 0;
        for (const auto& cat : categories) {
            totalMonthlyExpense += cat.monthlyExpense;
        }
        for (const auto& member : members) {
            totalMonthlyExpense += member.monthlyExpense;
        }
    }

    float getFinancialHealthScore() const {
        // 0-100 score based on expense-to-income ratio
        return 100.0f; // TODO: Calculate
    }
};

// Add to GameState
struct GameState {
    // ... existing fields
    FamilyState familyState;  // NEW
};
```

#### UI Design

**FamilyDashboardScreen**:
```kotlin
@Composable
fun FamilyDashboardScreen(
    familyState: FamilyState,
    onSpendClick: (SpendingCategory) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Key Metrics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1565C0)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Family Health",
                        fontSize = 16.sp,
                        color = Color.White
                    )

                    Text(
                        "${familyState.averageHappiness.toInt()}%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem("Members", familyState.members.size.toString())
                        MetricItem("Monthly", "₹${familyState.totalMonthlyExpense.toInt()}")
                        MetricItem("Savings", "₹${familyState.savingsBalance.toInt()}")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Spending Categories",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Spending Category Grid
        items(familyState.categories.chunked(2)) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { category ->
                    SpendingCategoryCard(
                        category = category,
                        onClick = { onSpendClick(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Life Events
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Life Events",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            LifeEventButton(
                icon = "💍",
                title = "Get Married",
                description = "₹10,000 - ₹50,000",
                onClick = { /* Trigger marriage */ }
            )
        }

        item {
            LifeEventButton(
                icon = "👶",
                title = "Have a Baby",
                description = "₹5,000 + ₹1,500/mo",
                onClick = { /* Trigger baby */ }
            )
        }
    }
}
```

#### Balancing Formula

**Financial Health Impact**:
```cpp
float GameState::updateFamilyHappiness() {
    float expenseRatio = familyState.totalMonthlyExpense /
                         (getTotalIncomePerMonth() + 0.01);

    if (expenseRatio > 0.20f) {
        // Spending too much - happiness increase, business slows
        familyState.averageHappiness += 10.0f;
        businessGrowthMultiplier = 0.95f;  // 5% slower growth
    } else if (expenseRatio < 0.05f) {
        // Spending too little - unhappy family
        familyState.averageHappiness -= 5.0f;
        if (familyState.averageHappiness < 30.0f) {
            // Family members may leave
        }
    } else {
        // Optimal balance
        businessGrowthMultiplier = 1.0f;
    }

    familyState.averageHappiness = std::clamp(
        familyState.averageHappiness, 0.0f, 100.0f
    );

    return familyState.averageHappiness;
}
```

---

### Feature C: Character System (Chef, Manager, Staff, Specialist)

**Goal**: Replace generic helpers with named, progressable characters

#### Character Types & Stats

```cpp
enum class CharacterType {
    CHEF,       // +50% tap income, +10% helper efficiency
    MANAGER,    // -20% upgrade cost, manages 3 stalls
    STAFF,      // +40% helper income, generic
    SPECIALIST  // +60% zone-specific income
};

struct CharacterStats {
    CharacterType type;
    int baseCost;
    float incomeMultiplier;
    float tapIncomeBonus;
    float upgradeCostReduction;
    int maxLevel;

    static CharacterStats getStatsForType(CharacterType type) {
        switch (type) {
            case CharacterType::CHEF:
                return {CHEF, 1500, 1.5f, 0.5f, 0.0f, 5};
            case CharacterType::MANAGER:
                return {MANAGER, 2500, 1.3f, 0.0f, 0.2f, 5};
            case CharacterType::STAFF:
                return {STAFF, 800, 1.4f, 0.0f, 0.0f, 3};
            case CharacterType::SPECIALIST:
                return {SPECIALIST, 3500, 1.6f, 0.3f, 0.1f, 5};
        }
    }
};

struct Character {
    std::string characterId;
    CharacterType type;
    std::string name;
    int level;
    int experience;
    float productivityMultiplier;
    int assignedStallId;
    std::string costume;  // "default", "royal", "business"
    bool isUnlocked;

    Character() : level(1), experience(0), productivityMultiplier(1.0f),
                  assignedStallId(-1), isUnlocked(false) {}

    double getEffectiveIncomeBonus() const {
        CharacterStats stats = CharacterStats::getStatsForType(type);
        return stats.incomeMultiplier * productivityMultiplier;
    }

    bool canLevelUp() const {
        CharacterStats stats = CharacterStats::getStatsForType(type);
        return level < stats.maxLevel && experience >= (level * 100);
    }

    void levelUp() {
        if (canLevelUp()) {
            level++;
            experience = 0;
            productivityMultiplier += 0.1f;
        }
    }
};
```

#### Character Name Generation (Indian Names)

```kotlin
object CharacterNameGenerator {
    private val chefNames = listOf(
        "रजत (Rajat)", "संजय (Sanjay)", "विक्रम (Vikram)", "अर्जुन (Arjun)",
        "कमल (Kamal)", "विनोद (Vinod)", "सुरेश (Suresh)", "राजेश (Rajesh)"
    )

    private val managerNames = listOf(
        "प्रिया (Priya)", "नीता (Neeta)", "राज (Raj)", "अमित (Amit)",
        "मीरा (Meera)", "अंजली (Anjali)", "रोहित (Rohit)", "नेहा (Neha)"
    )

    private val staffNames = listOf(
        "मोहन (Mohan)", "पवन (Pawan)", "बलजीत (Baljit)", "गुरप्रीत (Gurpreet)",
        "राहुल (Rahul)", "विजय (Vijay)", "दीपक (Deepak)", "अनिल (Anil)"
    )

    fun generateName(type: CharacterType): String {
        return when (type) {
            CharacterType.CHEF -> chefNames.random()
            CharacterType.MANAGER -> managerNames.random()
            CharacterType.STAFF -> staffNames.random()
            CharacterType.SPECIALIST -> staffNames.random()
        }
    }
}
```

---

### Feature D: UI Enhancements

#### Material3 Theme

```kotlin
val StreetTycoonColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50),        // Money green
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFFF9800),      // Upgrade orange
    onSecondary = Color.White,
    tertiary = Color(0xFF2196F3),       // Special blue
    onTertiary = Color.White,
    error = Color(0xFFF44336),
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1C1C)
)

@Composable
fun StreetTycoonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StreetTycoonColorScheme,
        typography = StreetTycoonTypography,
        content = content
    )
}
```

#### Animated Components

**Money Counter Animation**:
```kotlin
@Composable
fun AnimatedMoneyCounter(amount: Double) {
    val animatedAmount by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "money"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_rupee),
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = formatCurrency(animatedAmount.toDouble()),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )
    }
}
```

**Tap Serve Button with Combo**:
```kotlin
@Composable
fun TapServeButton(
    onTap: () -> Unit,
    comboCount: Int,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (comboCount > 0) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    )

    Box(
        modifier = modifier
            .size(200.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF388E3C)
                    )
                )
            )
            .clickable(
                onClick = onTap,
                indication = rememberRipple(bounded = true),
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(R.drawable.ic_hand_tap),
                contentDescription = "Tap to Serve",
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )

            Text(
                "SERVE",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (comboCount > 1) {
                Text(
                    "×$comboCount COMBO!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFEB3B)
                )
            }
        }
    }
}
```

---

### Feature E: Music & Sound Integration

#### Architecture: Media3

```kotlin
// build.gradle.kts additions
dependencies {
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-session:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
}
```

#### MusicManager Implementation

```kotlin
class MusicManager(private val context: Context) {
    private val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_GAME)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true
        )
        .build()

    private var currentTheme: MusicTheme = MusicTheme.DAY

    enum class MusicTheme {
        DAY, NIGHT, CELEBRATION, MENU
    }

    fun initialize() {
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.volume = 0.5f
        playTheme(MusicTheme.DAY)
    }

    fun playTheme(theme: MusicTheme) {
        if (currentTheme == theme && player.isPlaying) return

        val resId = when (theme) {
            MusicTheme.DAY -> R.raw.bg_music_street_day
            MusicTheme.NIGHT -> R.raw.bg_music_street_night
            MusicTheme.CELEBRATION -> R.raw.bg_music_celebration
            MusicTheme.MENU -> R.raw.bg_music_menu
        }

        val mediaItem = MediaItem.Builder()
            .setUri(RawResourceDataSource.buildRawResourceUri(resId))
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        currentTheme = theme
    }

    fun setVolume(volume: Float) {
        player.volume = volume.coerceIn(0f, 1f)
    }

    fun pause() = player.pause()
    fun resume() = player.play()
    fun release() = player.release()
}
```

#### SoundEffectsManager

```kotlin
class SoundEffectsManager(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val sounds = mutableMapOf<SoundEffect, Int>()

    enum class SoundEffect {
        TAP, UPGRADE, UNLOCK, EARN, FAIL, MARRY, BABY, LEVEL_UP
    }

    init {
        sounds[SoundEffect.TAP] = soundPool.load(context, R.raw.sfx_tap, 1)
        sounds[SoundEffect.UPGRADE] = soundPool.load(context, R.raw.sfx_upgrade, 1)
        sounds[SoundEffect.UNLOCK] = soundPool.load(context, R.raw.sfx_unlock, 1)
        sounds[SoundEffect.EARN] = soundPool.load(context, R.raw.sfx_earn, 1)
        sounds[SoundEffect.FAIL] = soundPool.load(context, R.raw.sfx_fail, 1)
        sounds[SoundEffect.MARRY] = soundPool.load(context, R.raw.sfx_marry, 1)
        sounds[SoundEffect.BABY] = soundPool.load(context, R.raw.sfx_baby, 1)
        sounds[SoundEffect.LEVEL_UP] = soundPool.load(context, R.raw.sfx_level_up, 1)
    }

    fun play(effect: SoundEffect, volume: Float = 1.0f) {
        sounds[effect]?.let { soundId ->
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        }
    }

    fun release() {
        soundPool.release()
    }
}
```

#### Audio Asset Requirements

**Background Music** (2-3 minute loops, OGG/AAC format):
- `bg_music_street_day.ogg` - Mellow, upbeat marketplace ambience
- `bg_music_street_night.ogg` - Calm, slightly slower tempo
- `bg_music_celebration.ogg` - Festive, energetic
- `bg_music_menu.ogg` - Light, welcoming

**Sound Effects** (50-400ms, OGG format):
- `sfx_tap.ogg` - Crisp tap sound (50ms)
- `sfx_upgrade.ogg` - Satisfying "level up" chime (300ms)
- `sfx_unlock.ogg` - Epic unlock fanfare (400ms)
- `sfx_earn.ogg` - Coin collect jingle (150ms)
- `sfx_fail.ogg` - Error buzz (100ms)
- `sfx_marry.ogg` - Celebration bells (500ms)
- `sfx_baby.ogg` - Happy chime (600ms)
- `sfx_level_up.ogg` - Achievement sound (400ms)

**License**: All audio should be CC0 or permissive (royalty-free)

---

## Part 4: Asset Deliverables

### Character Art Assets

For each character type (Chef, Manager, Staff, Specialist), create:

1. **Avatar Images** (PNG, transparent background):
   - `character_chef_default_128.png` - 128x128px
   - `character_chef_default_64.png` - 64x64px
   - `character_chef_default_32.png` - 32x32px (UI list)

2. **Costume Variants**:
   - `character_chef_royal_128.png`
   - `character_chef_business_128.png`
   - `character_chef_festival_128.png`

3. **Monochrome Badges** (SVG):
   - `badge_chef.svg` - Single-color outline for UI elements

4. **Disabled State**:
   - Grayscale versions with 50% opacity

**Art Style**:
- Flat design, 2.5D isometric perspective
- Warm color palette: yellows, oranges, terracotta
- Indian cultural elements (dhoti, kurta, traditional headwear)
- Friendly, caricature-like proportions
- High contrast for visibility on mobile

**Total Assets**: ~48 PNGs + 4 SVGs

### UI Icon Assets

- `ic_rupee.xml` - Indian Rupee symbol (vector drawable)
- `ic_hand_tap.xml` - Tap gesture icon
- `ic_upgrade_arrow.xml` - Upgrade indicator
- `ic_lock.xml` - Locked map indicator
- `ic_family.xml` - Family/spending icon
- `ic_heart.xml` - Happiness indicator
- `ic_school.xml` - Education icon
- `ic_home.xml` - Housing icon
- `ic_car.xml` - Transportation icon
- `ic_health.xml` - Health/medical icon

---

## Part 5: Implementation Timeline

### Week 1: Foundation & Critical Fixes (5 days)
**Days 1-2**: Fix critical bugs
- JNI error handling
- Race condition in tick loop
- Memory leak fixes
- Add comprehensive logging

**Days 3-5**: Map Gate System
- C++ data structures
- JSON serialization
- Kotlin models
- UI components
- Integration tests

### Week 2: Character System (5 days)
**Days 1-3**: Core character system
- C++ character structs
- Database schema (Room)
- Character hiring logic
- Leveling/XP system

**Days 4-5**: Character UI
- Character cards
- Hiring screen
- Costume shop
- Integration with stalls

### Week 3: Family & UI (5 days)
**Days 1-3**: Family spending system
- C++ family state
- Spending categories
- Life events (marriage, baby)
- Financial health calculation

**Days 4-5**: UI enhancements
- Material3 theme
- Animated components
- Improved layouts
- Accessibility

### Week 4: Audio & Testing (5 days)
**Days 1-2**: Music integration
- Media3 setup
- MusicManager
- SoundEffectsManager
- Settings UI

**Days 3-5**: Testing & CI
- Unit tests (Kotlin)
- Native tests (C++ with Google Test)
- Integration tests
- GitHub Actions workflow

### Week 5: Polish & Documentation (5 days)
**Days 1-2**: Asset integration
- Character art
- Sound effects
- Music tracks
- UI icons

**Days 3-4**: Documentation
- Updated README
- API documentation
- Design spec
- Migration guide

**Day 5**: Final QA and PR preparation

**Total**: ~25 working days (5 weeks)

---

## Part 6: Testing Strategy

### Unit Tests (Kotlin)

```kotlin
class GameSimulationTest {
    private lateinit var simulation: GameSimulation

    @Before
    fun setup() {
        simulation = GameSimulation()
        simulation.initializeNewGame()
    }

    @Test
    fun `tap serve increases player cash`() {
        val initialCash = simulation.getSnapshot().playerCash

        val result = simulation.tapServe(stallId = 0)

        assertTrue(result.success)
        val finalCash = simulation.getSnapshot().playerCash
        assertTrue(finalCash > initialCash)
    }

    @Test
    fun `upgrade increases stall level and income`() {
        val stall = simulation.getSnapshot().findStall(0)!!
        val initialLevel = stall.level
        val initialIncome = stall.getTotalIncomePerSecond()

        // Give enough cash
        simulation.addCash(10000.0)

        val result = simulation.upgradeStall(0)

        assertTrue(result.success)
        val updatedStall = simulation.getSnapshot().findStall(0)!!
        assertEquals(initialLevel + 1, updatedStall.level)
        assertTrue(updatedStall.getTotalIncomePerSecond() > initialIncome)
    }

    @Test
    fun `map gates prevent premature unlock`() {
        val zone = simulation.getSnapshot().findZone(1)!!

        assertFalse(zone.isUnlocked)
        assertFalse(zone.allGatesComplete())

        // Try to unlock without completing gates
        val result = simulation.unlockZone(1)

        assertFalse(result.success)
        assertEquals("Gates not completed", result.message)
    }

    @Test
    fun `character hiring applies income bonus`() {
        val stall = simulation.getSnapshot().findStall(0)!!
        val initialIncome = stall.getTotalIncomePerSecond()

        // Hire a chef
        simulation.addCash(5000.0)
        val result = simulation.hireCharacter(
            type = CharacterType.CHEF,
            assignedStallId = 0
        )

        assertTrue(result.success)

        val updatedStall = simulation.getSnapshot().findStall(0)!!
        val newIncome = updatedStall.getTotalIncomePerSecond()

        // Chef provides +50% bonus
        assertTrue(newIncome > initialIncome * 1.4) // Allow for slight variance
    }

    @Test
    fun `family spending reduces disposable income`() {
        simulation.addCash(50000.0)

        val initialCash = simulation.getSnapshot().playerCash

        // Trigger marriage
        val result = simulation.triggerFamilyEvent(
            type = FamilyEventType.MARRIAGE,
            level = "simple" // ₹10,000
        )

        assertTrue(result.success)

        val finalCash = simulation.getSnapshot().playerCash
        assertEquals(initialCash - 10000, finalCash, 0.01)

        val family = simulation.getSnapshot().familyState
        assertEquals(1, family.members.size) // Spouse added
    }
}
```

### C++ Native Tests (Google Test)

```cpp
#include <gtest/gtest.h>
#include "game_simulation.h"

using namespace streettycoon;

class GameSimulationTest : public ::testing::Test {
protected:
    GameSimulation sim;

    void SetUp() override {
        sim.initializeNewGame();
    }
};

TEST_F(GameSimulationTest, TickIncreasesPassiveIncome) {
    // Hire a helper
    sim.applyAction("{\"action\":\"hire_helper\",\"stallId\":0}");

    GameState initialState = sim.getState();
    double initialCash = initialState.playerCash;

    // Simulate 1 second (1000ms)
    sim.tick(1000);

    GameState finalState = sim.getState();
    double finalCash = finalState.playerCash;

    EXPECT_GT(finalCash, initialCash);
}

TEST_F(GameSimulationTest, OfflineEarningsCappedAt4Hours) {
    // Hire helper with 10₹/sec income
    sim.applyAction("{\"action\":\"hire_helper\",\"stallId\":0}");

    GameState initialState = sim.getState();
    double initialCash = initialState.playerCash;

    // Simulate 6 hours offline (21600000ms)
    int64_t sixHoursMs = 6 * 60 * 60 * 1000;
    double offlineEarnings = sim.calculateOfflineEarnings(sixHoursMs);

    // Should cap at 4 hours with 70% efficiency
    // 10₹/sec * 4hr * 3600sec * 0.7 = 100,800₹
    EXPECT_LE(offlineEarnings, 100800.0);
    EXPECT_GT(offlineEarnings, 100000.0); // Allow small variance
}

TEST_F(GameSimulationTest, MapGateValidation) {
    GameState state = sim.getState();

    // Zone 1 should start locked
    EXPECT_FALSE(state.zones[1].isUnlocked);

    // Try to unlock without completing gates
    std::string result = sim.applyAction(
        "{\"action\":\"unlock_zone\",\"zoneId\":1}"
    );

    // Parse result JSON
    EXPECT_NE(result.find("\"success\":false"), std::string::npos);
}

TEST_F(GameSimulationTest, CharacterProductivityBonus) {
    // Get initial stall income
    GameState initialState = sim.getState();
    double initialIncome = initialState.stalls[0].getTotalIncomePerSecond();

    // Hire a chef (provides +50% income bonus)
    sim.applyAction(
        "{\"action\":\"hire_character\","
        "\"type\":\"CHEF\",\"stallId\":0}"
    );

    GameState finalState = sim.getState();
    double finalIncome = finalState.stalls[0].getTotalIncomePerSecond();

    // Income should increase by ~50%
    EXPECT_GT(finalIncome, initialIncome * 1.4);
}

TEST_F(GameSimulationTest, SerializationRoundTrip) {
    // Modify game state
    sim.applyAction("{\"action\":\"tap_serve\",\"stallId\":0}");
    sim.applyAction("{\"action\":\"upgrade_stall\",\"stallId\":0}");

    // Serialize
    std::string json = sim.getSnapshot();

    // Create new simulation and restore
    GameSimulation newSim;
    bool success = newSim.initializeFromJson(json);

    ASSERT_TRUE(success);

    // Verify state matches
    GameState originalState = sim.getState();
    GameState restoredState = newSim.getState();

    EXPECT_EQ(originalState.playerCash, restoredState.playerCash);
    EXPECT_EQ(originalState.stalls.size(), restoredState.stalls.size());
    EXPECT_EQ(originalState.stalls[0].level, restoredState.stalls[0].level);
}
```

### CI/CD Pipeline (GitHub Actions)

```yaml
# .github/workflows/build.yml
name: Android CI

on:
  push:
    branches: [ main, develop, 'claude/**' ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-

    - name: Install NDK
      run: echo "y" | sudo ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --install "ndk;25.1.8937393"

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Run Kotlin tests
      run: ./gradlew test --stacktrace

    - name: Build C++ native code
      run: ./gradlew assembleDebug --stacktrace

    - name: Run C++ tests (if available)
      run: |
        if [ -d "app/src/test/cpp" ]; then
          echo "Running C++ tests..."
          # Configure and run Google Test
        fi

    - name: Build APK
      run: ./gradlew assembleDebug

    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk

    - name: Run linter
      run: ./gradlew lint

    - name: Upload lint reports
      uses: actions/upload-artifact@v3
      with:
        name: lint-results
        path: app/build/reports/lint-results-debug.html
```

---

## Part 7: Verification Steps

### After Implementation, Test:

1. **Map Gate System**:
   ```
   ✓ Start new game
   ✓ Complete 0 upgrades → verify Map 2 locked
   ✓ Complete 10 upgrades → verify gate progress updates
   ✓ Complete all 4 gates → verify Map 2 unlocks
   ✓ Attempt to unlock Map 3 prematurely → verify rejection
   ```

2. **Character System**:
   ```
   ✓ Hire Chef → verify income increase
   ✓ Hire Manager → verify upgrade cost reduction
   ✓ Assign character to stall → verify bonus applied
   ✓ Level up character → verify productivity increase
   ✓ Change costume → verify visual update (no stat change)
   ```

3. **Family Spending**:
   ```
   ✓ Trigger marriage → verify ₹10k deduction, spouse added
   ✓ Upgrade housing → verify monthly expense increase
   ✓ Have baby → verify recurring expense added
   ✓ Check financial health → verify score calculation
   ✓ Spend >20% of income → verify happiness increase, growth slowdown
   ```

4. **Music & Sound**:
   ```
   ✓ App launch → background music starts
   ✓ Change map → theme switches
   ✓ Tap serve → tap sound plays
   ✓ Upgrade stall → upgrade chime plays
   ✓ Unlock map → fanfare plays
   ✓ Mute toggle → all audio stops
   ```

5. **Performance**:
   ```
   ✓ Test on low-end device (2GB RAM) → verify 30+ FPS
   ✓ Tick loop overhead → verify <5ms per tick
   ✓ Save/load time → verify <200ms
   ✓ APK size → verify <80MB
   ```

---

## Part 8: Prioritized Issue List

### 🔴 Critical (Fix Before Features)

1. **JNI Exception Handling** - Wrap all JNI calls in try-catch
2. **Memory Leak** - Ensure `nativeDestroy()` called in all paths
3. **Race Condition** - Add mutex for tick/save synchronization
4. **No Tests** - Add minimum 10 unit tests before adding features

### 🟠 High Priority (Implement First)

5. **Map Gate System** - Core retention mechanic
6. **Character System (Basic)** - Replace generic helpers
7. **Music Integration** - Minimum 1 background track + 3 SFX

### 🟡 Medium Priority (Phase 2)

8. **Family Spending (MVP)** - Housing + Marriage only
9. **UI Polish** - Animations, Material3 theme
10. **State Migrations** - Version handling for updates

### 🟢 Low Priority (Nice to Have)

11. **Advanced Family** - Education, health, full event system
12. **Character Costumes** - Cosmetic variants
13. **Advanced Analytics** - Detailed event tracking

---

## Conclusion

This brief provides a **complete roadmap** for transforming Street Tycoon from a solid MVP into a feature-rich, retention-focused game with educational value. The hybrid architecture is sound, but needs critical bug fixes and the 5 major feature additions to reach its full potential.

**Estimated Total Effort**: 5 weeks (1 developer)
**Estimated APK Size Increase**: +15-20MB (audio + assets)
**Target Outcome**: 2x user retention, educational money management lessons, richer gameplay

---

## Next Steps

1. **Immediate**: Fix critical bugs (JNI, memory, race condition)
2. **Week 1-2**: Implement Map Gates + Character System
3. **Week 3-4**: Add Family Spending + UI Polish + Music
4. **Week 5**: Testing, documentation, asset integration, PR

**Branch**: All work on `claude/street-tycoon-analysis-features-011CUpwiEqm5qLyPk3wcNaQh`
**Final Deliverable**: PR-ready branch with tests, docs, and assets
