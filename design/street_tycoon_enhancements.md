# Street Tycoon: Enhanced UI & Features Implementation Guide

## 1. MAP UNLOCK GATE SYSTEM (Progression Lockdown)

### Requirement
Restrict map/zone progression until players complete mandatory actions on previous maps.

### Implementation Strategy

#### Data Model Enhancement (Kotlin)

```kotlin
// Updated GameModels.kt
data class MapProgressData(
    val mapId: Int,
    val isUnlocked: Boolean = false,
    val gateName: String,
    val gateRequirements: List<GateRequirement> = emptyList(),
    val gateProgress: Map<String, Int> = emptyMap()
)

data class GateRequirement(
    val type: GateType,  // UPGRADES, HELPERS, EARNINGS, PLAYTIME
    val targetValue: Int,
    val currentValue: Int = 0,
    val isCompleted: Boolean = false,
    val description: String
)

enum class GateType {
    UPGRADES_COMPLETED,
    HELPERS_HIRED,
    EARNINGS_THRESHOLD,
    PLAYTIME_HOURS,
    STALL_MASTERED
}
```

#### GameViewModel Implementation

```kotlin
class GameViewModel(repository: GameRepository) : ViewModel() {
    
    private val _mapProgressState = MutableStateFlow<MapProgressData?>(null)
    val mapProgressState: StateFlow<MapProgressData?> = _mapProgressState.asStateFlow()
    
    fun checkMapGates(mapId: Int) {
        viewModelScope.launch {
            val gates = getMapGates(mapId)
            val allGatesComplete = gates.all { gate ->
                gate.isCompleted
            }
            
            if (allGatesComplete) {
                unlockMap(mapId)
            }
        }
    }
    
    private suspend fun unlockMap(mapId: Int) {
        gameRepository.updateMapUnlockStatus(mapId, true)
        _mapProgressState.value = _mapProgressState.value?.copy(
            isUnlocked = true
        )
    }
}
```

#### C++ Native Implementation (Deterministic Check)

```cpp
// game_state.h
struct MapGate {
    GateType type;
    int targetValue;
    int currentValue;
    bool isCompleted;
};

// game_simulation.cpp
bool GameState::checkMapGates(int mapId) {
    std::vector<MapGate>& gates = mapGates[mapId];
    
    for (const auto& gate : gates) {
        if (!gate.isCompleted) {
            return false;  // At least one gate incomplete
        }
    }
    return true;  // All gates completed
}

void GameState::updateGateProgress(const std::string& actionJson) {
    // Parse action (upgrade completed, helper hired, etc.)
    // Update corresponding gate progress
    // Check if any map gates become completed
}
```

---

## 2. CHARACTER SYSTEM: CHEF, MANAGER, STAFF

### Requirements
- Visual avatar representation for hired staff
- Character progression (leveling, uniforms, abilities)
- Staff management interface

### Architecture

#### Character Data Model

```kotlin
// CharacterModels.kt
data class Character(
    val characterId: String,
    val type: CharacterType,  // CHEF, MANAGER, STAFF
    val name: String,
    val avatarUrl: String,
    val level: Int = 1,
    val experience: Int = 0,
    val assignedStall: Int? = null,
    val productivity: Float = 1.0f,  // 0.5f to 2.0f multiplier
    val unlockCost: Int,
    val costume: String = "default"
)

enum class CharacterType {
    CHEF,           // Increases efficiency + tap income
    MANAGER,        // Reduces upgrade cost, manages multiple stalls
    STAFF,          // Basic helper, lower cost
    SPECIALIST      // Zone-specific bonuses
}

// Character assets stored as
data class CharacterAsset(
    val type: CharacterType,
    val avatarDrawable: Int,  // R.drawable.chef_default, etc.
    val costumes: List<CostumeVariant> = emptyList()
)

data class CostumeVariant(
    val costumeId: String,
    val name: String,
    val drawableRes: Int,
    val tokenCost: Int = 100,
    val rarity: String  // "common", "rare", "epic"
)
```

#### UI Component: Character Card

```kotlin
// CharacterCard.kt (Jetpack Compose)
@Composable
fun CharacterCard(
    character: Character,
    onUpgradeClick: () -> Unit,
    onCostumeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Image
            Image(
                painter = painterResource(
                    id = getCharacterDrawable(character.type, character.costume)
                ),
                contentDescription = "${character.type.name} Avatar",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Character Info
            Text(
                text = character.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Text(
                text = "${character.type.name.lowercase()} • Level ${character.level}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            // Productivity Bar
            LinearProgressIndicator(
                progress = { character.productivity / 2.0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.LightGray
            )
            
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onUpgradeClick,
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) {
                    Text("Upgrade")
                }
                
                Button(
                    onClick = onCostumeClick,
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) {
                    Text("Costume")
                }
            }
        }
    }
}
```

---

## 3. REAL-LIFE SPENDING SYSTEM (Family Needs)

### Requirements
- Simulate real-world expenses (housing, car, education, health, marriage)
- Family member management
- Spending affects business expansion speed
- Educational value: money management simulation

### Architecture

#### Family & Spending Model

```kotlin
// FamilyModels.kt
data class FamilyMember(
    val memberId: String,
    val name: String,
    val relation: FamilyRelation,  // SPOUSE, CHILD, PARENT
    val age: Int,
    val expenses: Int = 0,
    val happiness: Float = 100f,
    val status: String = "healthy"  // "healthy", "sick", "studying"
)

enum class FamilyRelation {
    SPOUSE, CHILD, PARENT, SIBLING
}

data class SpendingCategory(
    val categoryId: String,
    val name: String,  // Housing, Transportation, Food, Education, Health, Marriage
    val icon: Int,     // drawable resource
    val monthlyExpense: Int,
    val nextUpgrade: Int? = null,
    val nextUpgradeDescription: String = "",
    val isUpgraded: Boolean = false,
    val lastUpgradeTime: Long = 0L
)

data class FamilyState(
    val members: List<FamilyMember> = emptyList(),
    val spendingCategories: List<SpendingCategory> = emptyList(),
    val totalMonthlyExpense: Int = 0,
    val totalHappiness: Float = 100f,
    val savingsBalance: Int = 0,
    val debtStatus: Float = 0f  // 0-1 scale
)
```

#### Real-Life Event System

```kotlin
// FamilyEventModels.kt
sealed class FamilyEvent {
    data class Marriage(val spouseLevel: String) : FamilyEvent()
    data class BabyBorn(val babyName: String) : FamilyEvent()
    data class HouseUpgrade(val beds: Int, val cost: Int) : FamilyEvent()
    data class CarPurchase(val carType: String, val cost: Int) : FamilyEvent()
    data class EducationMilestone(val childName: String, val level: String) : FamilyEvent()
    data class HealthCrisis(val memberName: String, val cost: Int) : FamilyEvent()
    data class BusinessExpansion(val newZone: Int) : FamilyEvent()
}

// FamilyViewModel.kt - manages family spending alongside business
class FamilyViewModel(private val gameRepo: GameRepository) : ViewModel() {
    
    private val _familyState = MutableStateFlow<FamilyState>(FamilyState())
    val familyState: StateFlow<FamilyState> = _familyState.asStateFlow()
    
    fun spendMoneyOnCategory(category: SpendingCategory, amount: Int) {
        viewModelScope.launch {
            val currentBusiness = gameRepo.getGameState()
            
            if (currentBusiness.playerCash >= amount) {
                // Deduct from business cash
                gameRepo.deductFromCash(amount)
                
                // Update family state
                val updatedExpenses = category.monthlyExpense + amount
                _familyState.value = _familyState.value.copy(
                    spendingCategories = _familyState.value.spendingCategories.map {
                        if (it.categoryId == category.categoryId) {
                            it.copy(monthlyExpense = updatedExpenses)
                        } else it
                    }
                )
                
                // Trigger family happiness increase
                increaseHappiness(amount / 100)
            }
        }
    }
    
    fun triggerMarriage(spouseLevel: String) {
        val event = FamilyEvent.Marriage(spouseLevel)
        // Cost: ₹10,000 - ₹50,000 depending on level
        val cost = when (spouseLevel) {
            "simple" -> 10000
            "elegant" -> 25000
            "royal" -> 50000
            else -> 15000
        }
        // Apply to game state
    }
}
```

#### UI: Spending Dashboard

```kotlin
// SpendingDashboard.kt (Jetpack Compose)
@Composable
fun FamilySpendingScreen(
    familyState: FamilyState,
    onSpendClick: (SpendingCategory) -> Unit,
    onMarriageClick: () -> Unit,
    onHaveBabyClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Family Summary
        item {
            FamilySummaryCard(
                totalMembers = familyState.members.size,
                monthlyExpense = familyState.totalMonthlyExpense,
                averageHappiness = familyState.totalHappiness,
                savingsBalance = familyState.savingsBalance
            )
        }
        
        // Spending Categories Grid
        items(familyState.spendingCategories.chunked(2)) { categoryRow ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categoryRow.forEach { category ->
                    SpendingCategoryCard(
                        category = category,
                        onClick = { onSpendClick(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Major Life Events
        item {
            Text(
                "Life Events",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
        
        item {
            LifeEventButton(
                icon = "💍",
                title = "Marriage",
                description = "Find your soulmate",
                onClick = onMarriageClick
            )
        }
        
        item {
            LifeEventButton(
                icon = "👶",
                title = "Have a Baby",
                description = "Start a family",
                onClick = onHaveBabyClick
            )
        }
    }
}

@Composable
fun SpendingCategoryCard(
    category: SpendingCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = category.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "₹${category.monthlyExpense}/month",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            if (category.nextUpgrade != null) {
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Upgrade: ₹${category.nextUpgrade}")
                }
            }
        }
    }
}
```

---

## 4. MUSIC & SOUND INTEGRATION

### Implementation Options

#### Option A: MediaPlayer (Simpler, Kotlin-Only)

```kotlin
// MusicManager.kt
class MusicManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    
    fun initializeBackgroundMusic() {
        mediaPlayer = MediaPlayer.create(context, R.raw.background_music)
        mediaPlayer?.isLooping = true
        mediaPlayer?.setVolume(0.5f, 0.5f)  // 50% volume
    }
    
    fun startMusic() {
        if (!isPlaying) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }
    
    fun pauseMusic() {
        if (isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
        }
    }
    
    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
    }
    
    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }
}

// Usage in GameViewModel
class GameViewModel(repository: GameRepository) : ViewModel() {
    private val musicManager = MusicManager(context)
    
    init {
        musicManager.initializeBackgroundMusic()
        musicManager.startMusic()
    }
    
    override fun onCleared() {
        musicManager.stopMusic()
        super.onCleared()
    }
}
```

#### Option B: Media3 (Recommended, More Robust)

```kotlin
// Media3 setup in build.gradle.kts
dependencies {
    implementation("androidx.media3:media3-exoplayer:1.1.1")
    implementation("androidx.media3:media3-ui:1.1.1")
}

// MusicPlayer.kt using Media3
class MusicPlayer(context: Context) {
    private val player = ExoPlayer.Builder(context).build()
    
    fun initializeMusic(musicResId: Int) {
        val mediaItem = MediaItem.fromUri(
            RawResourceDataSource.buildRawResourceUri(musicResId)
        )
        player.setMediaItem(mediaItem)
        player.prepare()
        player.isLooping = true
    }
    
    fun play() {
        player.playWhenReady = true
    }
    
    fun pause() {
        player.playWhenReady = false
    }
}
```

#### Option C: C++ Audio via Android NDK + AAudio

```cpp
// audio_engine.cpp (C++)
#include <aaudio/AAudio.h>

class AudioEngine {
private:
    AAudioStream* stream_ = nullptr;
    
public:
    bool Initialize() {
        AAudioStreamBuilder* builder = nullptr;
        AAudio_createStreamBuilder(&builder);
        
        AAudioStreamBuilder_setChannelCount(builder, 2);
        AAudioStreamBuilder_setSampleRate(builder, 48000);
        AAudioStreamBuilder_setFormat(builder, AAUDIO_FORMAT_PCM_I16);
        
        AAudioStreamBuilder_openStream(builder, &stream_);
        AAudioStreamBuilder_delete(builder);
        
        return stream_ != nullptr;
    }
    
    void Play() {
        if (stream_) {
            AAudioStream_requestStart(stream_);
        }
    }
};
```

**Recommendation:** Use **Option B (Media3)** as it provides best balance of performance, features, and maintainability for games.

---

## 5. UI ENHANCEMENTS: RICH INTERFACE

### Jetpack Compose Best Practices for Game UI

```kotlin
// Enhanced Theme with Game Colors
@Composable
fun StreetTycoonTheme(content: @Composable () -> Unit) {
    val colorScheme = ColorScheme(
        primary = Color(0xFF4CAF50),           // Green for money
        secondary = Color(0xFFFF9800),         // Orange for upgrades
        tertiary = Color(0xFF2196F3),          // Blue for special
        error = Color(0xFFF44336),
        background = Color(0xFFFAFAFA),
        surface = Color(0xFFFFFFFF)
    )
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            displayLarge = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            labelMedium = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        ),
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp)
        ),
        content = content
    )
}

// Animated Money Counter
@Composable
fun AnimatedMoneyCounter(amount: Int) {
    val animatedAmount by animateIntAsState(
        targetValue = amount,
        animationSpec = tween(durationMillis = 600, easing = EaseOutQuad),
        label = "MoneyCounter"
    )
    
    Text(
        text = "₹${animatedAmount.toFormattedString()}",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4CAF50),
        modifier = Modifier.padding(8.dp)
    )
}

// Gesture-Based Game Interactions
@Composable
fun InteractiveStallView(
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Press -> onTap()
                            PointerEventType.Release -> {}
                        }
                    }
                }
            }
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            ),
        contentAlignment = Alignment.Center
    ) {
        Text("Tap to Serve!", fontSize = 14.sp)
    }
}
```

---

## Summary: File Structure for Enhanced Project

```
Street-Tycoon/
├── app/src/main/
│   ├── cpp/
│   │   ├── audio_engine.cpp/h          // C++ audio (optional)
│   │   ├── game_state.cpp/h             // Enhanced with map gates
│   │   └── ...
│   ├── java/com/streettycoon/
│   │   ├── family/
│   │   │   ├── FamilyModels.kt
│   │   │   ├── FamilyViewModel.kt
│   │   │   └── FamilyRepository.kt
│   │   ├── character/
│   │   │   ├── CharacterModels.kt
│   │   │   ├── CharacterScreen.kt
│   │   │   └── CharacterViewModel.kt
│   │   ├── game/
│   │   │   ├── model/GameModels.kt      // Updated with MapProgressData
│   │   │   └── ...
│   │   ├── music/
│   │   │   ├── MusicManager.kt
│   │   │   └── SoundEffects.kt
│   │   ├── ui/screens/
│   │   │   ├── FamilySpendingScreen.kt
│   │   │   ├── CharacterManagementScreen.kt
│   │   │   ├── MapGateProgressScreen.kt
│   │   │   └── ...
│   │   └── ...
│   └── res/
│       ├── raw/
│       │   ├── background_music.mp3
│       │   ├── tap_sfx.mp3
│       │   └── ...
│       ├── drawable/
│       │   ├── chef_default.png
│       │   ├── chef_royal.png
│       │   ├── manager_avatar.png
│       │   └── ...
│       └── ...
└── ...
```

