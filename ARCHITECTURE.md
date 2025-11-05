# Street Tycoon - Technical Architecture

## Overview

Street Tycoon implements a hybrid architecture separating concerns between platform-specific UI code (Kotlin/Android) and platform-agnostic game logic (C++). This design enables:

1. **Deterministic simulation**: Game logic runs identically regardless of platform
2. **Optimal performance**: Heavy computation in optimized C++
3. **Easy porting**: Core game logic portable to iOS, web, etc.
4. **Clean separation**: UI and game logic communicate via well-defined API

## Architecture Layers

### Layer 1: UI (Jetpack Compose)

**Responsibility**: User interaction, visual presentation, navigation

**Key Components**:
- `MapScreen`: City zone overview, stall list
- `StallScreen`: Individual stall management, tap-to-serve
- `ShopScreen`: Monetization, daily rewards, IAP
- Navigation graph with bottom navigation

**State Management**:
- Observes `GameViewModel.gameState` Flow
- Reactive UI updates on state changes
- No direct game logic - only presentation

### Layer 2: ViewModel & Repository

**GameViewModel**:
```kotlin
class GameViewModel : AndroidViewModel {
    private val simulation: GameSimulation
    private val repository: GameRepository

    // Tick loop (100ms intervals)
    private fun startTickLoop()

    // Auto-save (30s intervals)
    private fun startAutoSave()

    // Game actions
    fun tapServe(stallId: Int)
    fun upgradeStall(stallId: Int)
    // ...
}
```

**Responsibilities**:
1. Owns native simulation instance (via GameSimulation wrapper)
2. Runs tick loop in coroutine (100ms updates)
3. Manages auto-save to Room database
4. Handles lifecycle (onCleared, etc.)
5. Exposes game state as StateFlow for UI

**GameRepository**:
```kotlin
class GameRepository(context: Context) {
    suspend fun saveGame(simulation: GameSimulation)
    suspend fun loadGame(): Pair<String, Long>?
    suspend fun deleteGame()
}
```

**Responsibilities**:
1. Persists game state to Room database
2. Loads saved games on app start
3. Manages player preferences

### Layer 3: JNI Bridge (Kotlin ↔ C++)

**GameSimulation** (Kotlin wrapper):
```kotlin
class GameSimulation {
    private var nativeHandle: Long

    init {
        System.loadLibrary("streettycoon")
        nativeHandle = nativeCreate()
    }

    fun tick(deltaTimeMs: Long)
    fun getSnapshot(): GameState
    fun applyAction(type: String, params: Map<String, Any>): ActionResult
}
```

**JNI API** (C++ side):
```cpp
// jni_bridge.cpp
JNIEXPORT jlong JNICALL
Java_..._nativeCreate(JNIEnv* env, jobject thiz)

JNIEXPORT void JNICALL
Java_..._nativeTick(JNIEnv* env, jobject thiz, jlong handle, jlong deltaMs)

JNIEXPORT jstring JNICALL
Java_..._nativeGetSnapshot(JNIEnv* env, jobject thiz, jlong handle)

JNIEXPORT jstring JNICALL
Java_..._nativeApplyAction(JNIEnv* env, jobject thiz, jlong handle, jstring actionJson)
```

**Design Principles**:
- Minimal JNI surface (7 methods total)
- String-based communication (JSON)
- Long-lived native pointer (single allocation)
- No frequent allocations crossing JNI boundary

### Layer 4: Native Simulation (C++)

**GameSimulation** (C++ core):
```cpp
class GameSimulation {
private:
    GameState state_;

public:
    void tick(int64_t deltaTimeMs);
    std::string getSnapshot() const;
    std::string applyAction(const std::string& actionJson);
    double calculateOfflineEarnings(int64_t offlineTimeMs);
};
```

**GameState** (authoritative state):
```cpp
struct GameState {
    int version;
    double playerCash;
    int playerTokens;
    int64_t lastUpdateTimestamp;
    std::vector<Stall> stalls;
    std::vector<Zone> zones;
    // ... stats
};
```

**Deterministic Tick**:
```cpp
void GameSimulation::tick(int64_t deltaTimeMs) {
    double deltaSeconds = deltaTimeMs / 1000.0;

    // Process passive income from all unlocked stalls
    for (const auto& stall : state_.stalls) {
        if (!stall.isUnlocked) continue;
        double income = stall.getTotalIncomePerSecond() * deltaSeconds;
        state_.playerCash += income;
    }

    state_.lastUpdateTimestamp = getCurrentTimestamp();
}
```

**Action Processing**:
```cpp
std::string GameSimulation::applyAction(const std::string& actionJson) {
    std::string actionType = JsonSerializer::extractString(actionJson, "action");

    if (actionType == "tap_serve") {
        int stallId = JsonSerializer::extractInt(actionJson, "stallId");
        bool success = handleTapServe(stallId);
        return JsonSerializer::serializeActionResult(success, message);
    }
    // ... other actions
}
```

**Responsibilities**:
1. Maintain authoritative game state
2. Process tick updates (passive income)
3. Execute player actions (tap, upgrade, hire, etc.)
4. Calculate offline earnings
5. Serialize/deserialize state to JSON

## Data Flow Diagrams

### Tick Loop Flow
```
┌─────────────────────────────────────────┐
│  GameViewModel (100ms coroutine)        │
│                                          │
│  while (isActive) {                     │
│    deltaTime = now - lastTickTime       │
│    simulation.tick(deltaTime)    ───────┼──┐
│    updateGameState()             ◄──────┼──┘
│    delay(100)                            │  (JNI call)
│  }                                       │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  Native GameSimulation (C++)             │
│                                          │
│  void tick(deltaMs) {                   │
│    processPassiveIncome(deltaMs)        │
│    updateTimestamp()                    │
│  }                                       │
└─────────────────────────────────────────┘
```

### Action Flow (User Tap)
```
User taps "Serve Customer" button
    │
    ▼
┌────────────────────────────┐
│  StallScreen (Compose)      │
│  onClick = {                │
│    viewModel.tapServe(id)   │
│  }                          │
└────────┬───────────────────┘
         │
         ▼
┌────────────────────────────┐
│  GameViewModel              │
│  fun tapServe(stallId) {   │
│    result = simulation      │
│      .tapServe(stallId)     │
│    if (result.success)      │
│      updateGameState()      │
│  }                          │
└────────┬───────────────────┘
         │ JNI
         ▼
┌────────────────────────────┐
│  GameSimulation (Kotlin)    │
│  fun tapServe(id):          │
│    ActionResult {           │
│    applyAction("tap_serve", │
│      mapOf("stallId" to id))│
│  }                          │
└────────┬───────────────────┘
         │ JNI call
         ▼
┌────────────────────────────┐
│  game_simulation.cpp        │
│  applyAction(actionJson) {  │
│    handleTapServe(stallId)  │
│    state_.playerCash += X   │
│    return success JSON      │
│  }                          │
└────────────────────────────┘
```

### Save/Load Flow
```
App Pause or Auto-save timer
    │
    ▼
┌────────────────────────────┐
│  GameViewModel              │
│  saveGame() {               │
│    repository.saveGame(     │
│      simulation)            │
│  }                          │
└────────┬───────────────────┘
         │
         ▼
┌────────────────────────────┐
│  GameRepository             │
│  saveGame(sim) {            │
│    json = sim.getSnapshot() │─┐
│    entity = GameSnapshot(   │ │
│      snapshotJson = json    │ │
│    )                        │ │
│    dao.saveSnapshot(entity) │ │
│  }                          │ │
└────────┬───────────────────┘ │
         │                      │
         ▼                      │ JNI
┌────────────────────────────┐ │
│  Room Database              │ │
│  INSERT game_snapshots      │ │
│    (id=1, snapshotJson,     │ │
│     timestamp, ...)         │ │
└─────────────────────────────┘ │
                                 │
┌────────────────────────────┐  │
│  game_simulation.cpp        │◄─┘
│  getSnapshot() {            │
│    return JsonSerializer::  │
│      serialize(state_)      │
│  }                          │
└─────────────────────────────┘
```

## State Management

### Single Source of Truth

The **C++ GameState** is the single source of truth. All game state lives here:

```cpp
struct GameState {
    int version;               // Schema version for migrations
    double playerCash;         // Current cash
    int playerTokens;          // Premium currency
    int64_t lastUpdateTimestamp;
    std::vector<Stall> stalls; // All stalls
    std::vector<Zone> zones;   // All zones
    int64_t totalCustomersServed;
    double totalEarnings;
    int currentDay;            // Daily reward streak
    int64_t lastDailyRewardTimestamp;
};
```

### State Snapshots

State is exported as JSON snapshots:

```json
{
  "version": 1,
  "playerCash": 12450.50,
  "playerTokens": 25,
  "lastUpdateTimestamp": 1699564832000,
  "totalCustomersServed": 1523,
  "totalEarnings": 50340.25,
  "currentDay": 7,
  "lastDailyRewardTimestamp": 1699564800000,
  "zones": [
    {
      "id": 0,
      "name": "Marketplace",
      "isUnlocked": true,
      "unlockCost": 0
    }
  ],
  "stalls": [
    {
      "id": 0,
      "type": "TEA",
      "level": 5,
      "zoneId": 0,
      "baseIncome": 10.0,
      "tapIncome": 1.0,
      "lastServedTimestamp": 1699564830000,
      "isUnlocked": true,
      "helpers": [
        {"id": 0, "level": 1, "incomePerSecond": 5.0},
        {"id": 1, "level": 1, "incomePerSecond": 5.0}
      ]
    }
  ]
}
```

### State Updates

State updates happen in two ways:

1. **Tick updates** (passive): Run every 100ms, update cash based on helpers
2. **Action updates** (active): User actions (tap, upgrade, hire, etc.)

Both are processed in C++ and result in state changes.

## Performance Considerations

### JNI Overhead

JNI calls have overhead (~microseconds). To minimize:

1. **Batch updates**: Single tick() call processes all passive income
2. **Snapshot caching**: Only serialize when UI needs update (100ms intervals)
3. **Long-lived objects**: Native simulation pointer lives for app lifetime
4. **String reuse**: JSON strings are only allocated when needed

### Memory Management

- **Native side**: Manual memory management, no leaks
- **Kotlin side**: Garbage collected, minimal allocations in hot path
- **JNI strings**: Released immediately after use

### Threading

```
Main Thread (UI)
  │
  └─► GameViewModel
       │
       ├─► Tick Loop (Dispatchers.Default)
       │    └─► Native simulation (background thread)
       │
       └─► Auto-save (Dispatchers.IO)
            └─► Room database
```

- UI thread: Only Compose rendering
- Default dispatcher: Tick loop and JNI calls
- IO dispatcher: Database operations

## Testing Strategy

### Unit Tests (Kotlin)

```kotlin
@Test
fun `tapServe increases cash`() {
    val viewModel = GameViewModel(application)
    val initialCash = viewModel.gameState.value?.playerCash ?: 0.0

    viewModel.tapServe(stallId = 0)

    val newCash = viewModel.gameState.value?.playerCash ?: 0.0
    assertTrue(newCash > initialCash)
}
```

### Native Tests (C++)

```cpp
TEST(GameSimulation, TickIncreasesPassiveIncome) {
    GameSimulation sim;
    sim.initializeNewGame();

    // Hire a helper
    sim.applyAction("{\"action\":\"hire_helper\",\"stallId\":0}");

    double initialCash = sim.getSnapshot().playerCash;
    sim.tick(1000); // 1 second
    double finalCash = sim.getSnapshot().playerCash;

    EXPECT_GT(finalCash, initialCash);
}
```

### Integration Tests

```kotlin
@Test
fun `save and load preserves state`() = runTest {
    val repo = GameRepository(context)
    val sim = GameSimulation()
    sim.initializeNewGame()
    sim.tapServe(0)

    repo.saveGame(sim)

    val (json, timestamp) = repo.loadGame()!!
    val newSim = GameSimulation()
    newSim.initializeFromJson(json)

    assertEquals(sim.getSnapshot(), newSim.getSnapshot())
}
```

## Security & Anti-Cheat

### Client-Side Protection

1. **Deterministic simulation**: No randomness, predictable outcomes
2. **State validation**: Check invariants on load
3. **Offline earnings cap**: Max 4 hours, prevents time manipulation
4. **ProGuard obfuscation**: Release builds obfuscated

### Server-Side (Future)

For leaderboards and competitive features:
1. Server validates all major transactions
2. Replay validation: Server re-runs game from checkpoint
3. Anomaly detection: Flag suspicious progression rates

## Extensibility

### Adding New Stall Types

1. **C++ side**: Add enum value to `StallType`
2. **C++ side**: Update `Stall` constructor with base values
3. **Kotlin side**: Add enum value to `StallType`
4. **UI side**: Add name/icon mapping in `getStallTypeName()`

### Adding New Actions

1. **C++ side**: Add handler in `GameSimulation::applyAction()`
2. **Kotlin wrapper**: Add convenience method in `GameSimulation.kt`
3. **ViewModel**: Add public method for UI
4. **UI**: Wire up button/action to ViewModel method

### Schema Migrations

Version field in GameState enables migrations:

```cpp
bool GameState::migrate(int fromVersion, int toVersion) {
    if (fromVersion == 1 && toVersion == 2) {
        // Add new field, set default value
        this->newField = defaultValue;
        this->version = 2;
        return true;
    }
    return false;
}
```

## Conclusion

This architecture provides:

✅ **Performance**: C++ simulation, optimized JNI boundary
✅ **Maintainability**: Clear separation of concerns
✅ **Testability**: Each layer independently testable
✅ **Portability**: Core logic portable to other platforms
✅ **Scalability**: Easy to extend with new features

The hybrid approach balances the strengths of each technology while minimizing their weaknesses.
