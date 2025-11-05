# Street Tycoon: Code Implementation Snippets & Architecture

## A. MAP GATE SYSTEM - Detailed Implementation

### 1.1 Database Schema (Room)

```kotlin
// entities/MapGateEntity.kt
@Entity(tableName = "map_gates")
data class MapGateEntity(
    @PrimaryKey val gateId: String,
    val mapId: Int,
    val gateType: String,  // UPGRADES, HELPERS, EARNINGS, PLAYTIME
    val targetValue: Int,
    val currentValue: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// entities/MapProgressEntity.kt
@Entity(tableName = "map_progress")
data class MapProgressEntity(
    @PrimaryKey val mapId: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val allGatesComplete: Boolean = false
)

// Relation class
data class MapWithGates(
    @Embedded val map: MapProgressEntity,
    @Relation(
        parentColumn = "mapId",
        entityColumn = "mapId"
    )
    val gates: List<MapGateEntity>
)
```

### 1.2 DAO (Data Access Object)

```kotlin
// dao/MapProgressDao.kt
@Dao
interface MapProgressDao {
    
    @Query("SELECT * FROM map_progress WHERE mapId = :mapId")
    suspend fun getMapProgress(mapId: Int): MapProgressEntity?
    
    @Query("SELECT * FROM map_progress WHERE mapId = :mapId")
    fun observeMapProgress(mapId: Int): Flow<MapProgressEntity?>
    
    @Query("SELECT * FROM map_gates WHERE mapId = :mapId")
    suspend fun getMapGates(mapId: Int): List<MapGateEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapGate(gate: MapGateEntity)
    
    @Update
    suspend fun updateMapGate(gate: MapGateEntity)
    
    @Update
    suspend fun updateMapProgress(progress: MapProgressEntity)
    
    @Query("""
        SELECT * FROM map_progress 
        WHERE mapId = :mapId
    """)
    fun observeMapWithGates(mapId: Int): Flow<MapWithGates?>
}
```

### 1.3 Repository Pattern

```kotlin
// repository/MapProgressRepository.kt
class MapProgressRepository(
    private val mapProgressDao: MapProgressDao,
    private val gameRepository: GameRepository
) {
    
    suspend fun initializeMaps() {
        // Create initial gates for each map
        val mapGatesConfig = listOf(
            MapGateConfig(
                mapId = 1,
                gates = listOf(
                    GateConfig(GateType.UPGRADES_COMPLETED, 10, "Complete 10 upgrades"),
                    GateConfig(GateType.HELPERS_HIRED, 5, "Hire 5 helpers"),
                    GateConfig(GateType.EARNINGS_THRESHOLD, 5000, "Earn ₹5,000"),
                    GateConfig(GateType.PLAYTIME_HOURS, 24, "Play for 24 hours")
                )
            ),
            MapGateConfig(
                mapId = 2,
                gates = listOf(
                    GateConfig(GateType.UPGRADES_COMPLETED, 20, "Complete 20 upgrades"),
                    GateConfig(GateType.HELPERS_HIRED, 10, "Hire 10 helpers"),
                    GateConfig(GateType.EARNINGS_THRESHOLD, 25000, "Earn ₹25,000"),
                    GateConfig(GateType.PLAYTIME_HOURS, 48, "Play for 48 hours")
                )
            )
            // ... more maps
        )
        
        mapGatesConfig.forEach { config ->
            config.gates.forEach { gateConfig ->
                val entity = MapGateEntity(
                    gateId = "${config.mapId}_${gateConfig.type}",
                    mapId = config.mapId,
                    gateType = gateConfig.type.name,
                    targetValue = gateConfig.targetValue
                )
                mapProgressDao.insertMapGate(entity)
            }
        }
    }
    
    suspend fun updateGateProgress(
        mapId: Int,
        gateType: String,
        newValue: Int
    ) {
        val gate = mapProgressDao.getMapGates(mapId)
            .find { it.gateType == gateType } ?: return
        
        val updatedGate = gate.copy(
            currentValue = maxOf(gate.currentValue, newValue),
            isCompleted = newValue >= gate.targetValue
        )
        
        mapProgressDao.updateMapGate(updatedGate)
        
        // Check if all gates complete
        checkMapUnlock(mapId)
    }
    
    private suspend fun checkMapUnlock(mapId: Int) {
        val gates = mapProgressDao.getMapGates(mapId)
        val allComplete = gates.all { it.isCompleted }
        
        if (allComplete) {
            val progress = mapProgressDao.getMapProgress(mapId) ?: return
            mapProgressDao.updateMapProgress(
                progress.copy(
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis(),
                    allGatesComplete = true
                )
            )
        }
    }
    
    fun observeMapProgress(mapId: Int): Flow<MapWithGates?> {
        return mapProgressDao.observeMapWithGates(mapId)
    }
}
```

### 1.4 ViewModel Implementation

```kotlin
// viewmodel/MapProgressViewModel.kt
class MapProgressViewModel(
    private val mapRepository: MapProgressRepository,
    private val gameRepository: GameRepository
) : ViewModel() {
    
    fun getCurrentMapProgress(mapId: Int): Flow<MapProgressUIState> = 
        combine(
            mapRepository.observeMapProgress(mapId),
            gameRepository.observeGameState()
        ) { mapWithGates, gameState ->
            if (mapWithGates == null) {
                return@combine MapProgressUIState.Loading
            }
            
            val gates = mapWithGates.gates.map { gate ->
                GateUIModel(
                    type = gate.gateType,
                    description = getGateDescription(gate.gateType),
                    currentValue = gate.currentValue,
                    targetValue = gate.targetValue,
                    progress = (gate.currentValue.toFloat() / gate.targetValue),
                    isCompleted = gate.isCompleted,
                    icon = getGateIcon(gate.gateType)
                )
            }
            
            MapProgressUIState.Success(
                mapId = mapWithGates.map.mapId,
                isUnlocked = mapWithGates.map.isUnlocked,
                gates = gates,
                allGatesComplete = mapWithGates.map.allGatesComplete
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MapProgressUIState.Loading
        )
    
    fun onActionCompleted(actionType: String, value: Int) {
        viewModelScope.launch {
            when (actionType) {
                "UPGRADE" -> mapRepository.updateGateProgress(
                    getCurrentMapId(),
                    "UPGRADES_COMPLETED",
                    value
                )
                "HELPER_HIRED" -> mapRepository.updateGateProgress(
                    getCurrentMapId(),
                    "HELPERS_HIRED",
                    value
                )
                "EARNED" -> mapRepository.updateGateProgress(
                    getCurrentMapId(),
                    "EARNINGS_THRESHOLD",
                    value
                )
            }
        }
    }
}

// UI State
sealed class MapProgressUIState {
    object Loading : MapProgressUIState()
    data class Success(
        val mapId: Int,
        val isUnlocked: Boolean,
        val gates: List<GateUIModel>,
        val allGatesComplete: Boolean
    ) : MapProgressUIState()
    data class Error(val message: String) : MapProgressUIState()
}

data class GateUIModel(
    val type: String,
    val description: String,
    val currentValue: Int,
    val targetValue: Int,
    val progress: Float,
    val isCompleted: Boolean,
    val icon: Int
)
```

### 1.5 Jetpack Compose UI

```kotlin
// screens/MapGateProgressScreen.kt
@Composable
fun MapGateProgressScreen(
    mapId: Int,
    viewModel: MapProgressViewModel = hiltViewModel(),
    onMapUnlocked: () -> Unit = {}
) {
    val progressState by viewModel.getCurrentMapProgress(mapId)
        .collectAsState(MapProgressUIState.Loading)
    
    when (val state = progressState) {
        is MapProgressUIState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAFAFA))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header
                MapHeaderCard(
                    mapId = state.mapId,
                    allComplete = state.allGatesComplete
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Gates List
                Text(
                    "Complete to Unlock Next Zone",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                state.gates.forEach { gate ->
                    GateProgressCard(
                        gate = gate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Unlock Button
                if (state.allGatesComplete && !state.isUnlocked) {
                    Button(
                        onClick = {
                            onMapUnlocked()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text(
                            "Unlock New Zone!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        MapProgressUIState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is MapProgressUIState.Error -> {
            // Error UI
        }
    }
}

@Composable
fun GateProgressCard(gate: GateUIModel, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gate.isCompleted) Color(0xFFE8F5E9) else Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    gate.description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (gate.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { gate.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF4CAF50),
                trackColor = Color.LightGray
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                "${gate.currentValue} / ${gate.targetValue}",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
```

---

## B. CHARACTER SYSTEM - Complete Implementation

### 2.1 Character Data Models

```kotlin
// models/CharacterModels.kt
@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val characterId: String,
    val type: String,  // CHEF, MANAGER, STAFF, SPECIALIST
    val name: String,
    val level: Int = 1,
    val experience: Int = 0,
    val productivityMultiplier: Float = 1.0f,
    val assignedStallId: String? = null,
    val costume: String = "default",
    val unlockCost: Int,
    val isUnlocked: Boolean = false,
    val acquiredAt: Long? = null
)

enum class CharacterType {
    CHEF,
    MANAGER,
    STAFF,
    SPECIALIST
}

data class CharacterStats(
    val type: CharacterType,
    val baseCost: Int,
    val incomeBonus: Float,     // 1.5f = 50% bonus
    val tapIncomeBonus: Float,  // 0.5f = 50% bonus
    val upgradeCostReduction: Float,  // 0.2f = 20% reduction
    val maxLevel: Int = 5,
    val experiencePerLevel: Int = 100
)

// Character progression formula
val CHARACTER_STATS_MAP = mapOf(
    CharacterType.CHEF to CharacterStats(
        type = CharacterType.CHEF,
        baseCost = 1500,
        incomeBonus = 0.5f,
        tapIncomeBonus = 0.5f,
        upgradeCostReduction = 0f,
        maxLevel = 5
    ),
    CharacterType.MANAGER to CharacterStats(
        type = CharacterType.MANAGER,
        baseCost = 2500,
        incomeBonus = 0.3f,
        tapIncomeBonus = 0f,
        upgradeCostReduction = 0.2f,
        maxLevel = 5
    ),
    CharacterType.STAFF to CharacterStats(
        type = CharacterType.STAFF,
        baseCost = 800,
        incomeBonus = 0.4f,
        tapIncomeBonus = 0f,
        upgradeCostReduction = 0f,
        maxLevel = 3
    ),
    CharacterType.SPECIALIST to CharacterStats(
        type = CharacterType.SPECIALIST,
        baseCost = 3500,
        incomeBonus = 0.6f,
        tapIncomeBonus = 0.3f,
        upgradeCostReduction = 0.1f,
        maxLevel = 5
    )
)
```

### 2.2 Character Repository

```kotlin
// repository/CharacterRepository.kt
class CharacterRepository(
    private val characterDao: CharacterDao,
    private val gameRepository: GameRepository
) {
    
    suspend fun hireCharacter(
        characterType: CharacterType,
        assignedStallId: String? = null
    ): Boolean {
        val gameState = gameRepository.getGameState()
        val stats = CHARACTER_STATS_MAP[characterType] ?: return false
        
        // Calculate hire cost (increases with each hire)
        val existingCount = characterDao.countCharactersByType(characterType.name)
        val hireCost = stats.baseCost * (1.3f.pow(existingCount.toFloat())).toInt()
        
        if (gameState.playerCash < hireCost) {
            return false  // Not enough money
        }
        
        // Deduct cost
        gameRepository.deductFromCash(hireCost)
        
        // Create character
        val character = CharacterEntity(
            characterId = UUID.randomUUID().toString(),
            type = characterType.name,
            name = generateCharacterName(characterType),
            level = 1,
            unlockCost = hireCost,
            isUnlocked = true,
            acquiredAt = System.currentTimeMillis(),
            assignedStallId = assignedStallId
        )
        
        characterDao.insertCharacter(character)
        return true
    }
    
    suspend fun upgradeCharacterLevel(characterId: String): Boolean {
        val character = characterDao.getCharacter(characterId) ?: return false
        val stats = CHARACTER_STATS_MAP[CharacterType.valueOf(character.type)] ?: return false
        
        if (character.level >= stats.maxLevel) return false
        
        val experienceNeeded = stats.experiencePerLevel
        val gameState = gameRepository.getGameState()
        
        val upgradeCost = (character.level * 500)  // Scales with level
        if (gameState.playerCash < upgradeCost) return false
        
        gameRepository.deductFromCash(upgradeCost)
        
        val upgraded = character.copy(
            level = character.level + 1,
            experience = 0,
            productivityMultiplier = 1.0f + (character.level * 0.1f)
        )
        
        characterDao.updateCharacter(upgraded)
        return true
    }
    
    suspend fun changeCostume(
        characterId: String,
        costumeId: String,
        tokenCost: Int
    ): Boolean {
        val userTokens = gameRepository.getUserTokens()
        if (userTokens < tokenCost) return false
        
        gameRepository.deductTokens(tokenCost)
        
        val character = characterDao.getCharacter(characterId) ?: return false
        characterDao.updateCharacter(character.copy(costume = costumeId))
        
        return true
    }
    
    fun observeCharacters(): Flow<List<CharacterEntity>> {
        return characterDao.observeAllCharacters()
    }
    
    fun observeCharactersByStall(stallId: String): Flow<List<CharacterEntity>> {
        return characterDao.observeCharactersByStall(stallId)
    }
    
    suspend fun getCharacterIncomeBonus(characterId: String): Float {
        val character = characterDao.getCharacter(characterId) ?: return 1.0f
        val stats = CHARACTER_STATS_MAP[CharacterType.valueOf(character.type)] ?: return 1.0f
        
        return 1.0f + (stats.incomeBonus * character.productivityMultiplier)
    }
    
    private fun generateCharacterName(type: CharacterType): String {
        val names = when (type) {
            CharacterType.CHEF -> listOf(
                "रजत (Rajat)", "संजय (Sanjay)", "विक्रम (Vikram)", "अर्जुन (Arjun)"
            )
            CharacterType.MANAGER -> listOf(
                "प्रिया (Priya)", "नीता (Neeta)", "राज (Raj)", "अमित (Amit)"
            )
            CharacterType.STAFF -> listOf(
                "मोहन (Mohan)", "सुरेश (Suresh)", "पवन (Pawan)", "कमल (Kamal)"
            )
            CharacterType.SPECIALIST -> listOf(
                "बलजीत (Baljit)", "गुरप्रीत (Gurpreet)", "माया (Maya)", "निशा (Nisha)"
            )
        }
        return names.random()
    }
}
```

### 2.3 Character UI Components

```kotlin
// ui/components/CharacterCard.kt
@Composable
fun CharacterCard(
    character: CharacterEntity,
    onClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    onCostumeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Image(
                painter = painterResource(
                    id = getCharacterAvatarResource(
                        character.type,
                        character.costume
                    )
                ),
                contentDescription = character.name,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Name & Type
            Text(
                character.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Text(
                "${character.type} · Level ${character.level}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Productivity Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Productivity: ${(character.productivityMultiplier * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                LinearProgressIndicator(
                    progress = { character.productivityMultiplier / 2.0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF4CAF50),
                    trackColor = Color.LightGray
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onUpgradeClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("Level Up", fontSize = 12.sp)
                }
                
                Button(
                    onClick = onCostumeClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    )
                ) {
                    Text("Costume", fontSize = 12.sp)
                }
            }
        }
    }
}

// ui/screens/CharacterManagementScreen.kt
@Composable
fun CharacterManagementScreen(
    viewModel: CharacterViewModel = hiltViewModel()
) {
    val characters by viewModel.allCharacters.collectAsState(initial = emptyList())
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        item {
            Text(
                "My Team",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        items(characters) { character ->
            CharacterCard(
                character = character,
                onClick = {},
                onUpgradeClick = {
                    viewModel.upgradeCharacter(character.characterId)
                },
                onCostumeClick = {
                    // Show costume picker
                }
            )
        }
    }
}
```

---

## C. MUSIC IMPLEMENTATION WITH MEDIA3

### 3.1 Setup & Initialization

```kotlin
// managers/MusicManager.kt
class MusicManager(
    private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope
) {
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private var currentTheme: String = THEME_DAY
    
    companion object {
        private const val THEME_DAY = "day"
        private const val THEME_NIGHT = "night"
        private const val THEME_CELEBRATION = "celebration"
    }
    
    fun initialize() {
        exoPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_GAME)
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .build(),
            true  // handleAudioFocus
        )
        
        exoPlayer.addListener(PlayerListener())
    }
    
    fun playTheme(themeId: String) {
        if (currentTheme == themeId) return  // Already playing
        
        val mediaItem = when (themeId) {
            THEME_DAY -> MediaItem.fromUri(
                RawResourceDataSource.buildRawResourceUri(
                    context.resources.getIdentifier(
                        "bg_music_street_day",
                        "raw",
                        context.packageName
                    )
                )
            )
            THEME_NIGHT -> MediaItem.fromUri(
                RawResourceDataSource.buildRawResourceUri(
                    context.resources.getIdentifier(
                        "bg_music_street_night",
                        "raw",
                        context.packageName
                    )
                )
            )
            else -> return
        }
        
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        exoPlayer.isLooping = true
        
        currentTheme = themeId
    }
    
    fun pause() {
        exoPlayer.pause()
    }
    
    fun resume() {
        exoPlayer.play()
    }
    
    fun setVolume(volume: Float) {
        exoPlayer.volume = volume.coerceIn(0f, 1f)
    }
    
    fun release() {
        exoPlayer.release()
    }
    
    private inner class PlayerListener : Player.Listener {
        override fun onPlaybackStateChanged(@Player.State state: Int) {
            when (state) {
                Player.STATE_READY -> {}  // Ready to play
                Player.STATE_ENDED -> {
                    exoPlayer.seekToDefaultPosition()
                    exoPlayer.playWhenReady = true  // Restart
                }
            }
        }
    }
}

// managers/SoundEffectsManager.kt
class SoundEffectsManager(private val context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_GAME)
                .setContentType(C.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    
    private val sounds = mutableMapOf<String, Int>()
    
    fun loadSounds() {
        val soundsToLoad = mapOf(
            "tap" to R.raw.sfx_tap,
            "upgrade" to R.raw.sfx_upgrade,
            "unlock" to R.raw.sfx_unlock,
            "earn" to R.raw.sfx_earn,
            "fail" to R.raw.sfx_fail,
            "marry" to R.raw.sfx_marry,
            "baby" to R.raw.sfx_baby,
            "level_up" to R.raw.sfx_level_up
        )
        
        soundsToLoad.forEach { (key, resId) ->
            sounds[key] = soundPool.load(context, resId, 1)
        }
    }
    
    fun play(soundKey: String, volume: Float = 1.0f) {
        val soundId = sounds[soundKey] ?: return
        soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
    }
    
    fun playWithDelay(soundKey: String, delayMs: Long) {
        Handler(Looper.getMainLooper()).postDelayed(
            { play(soundKey) },
            delayMs
        )
    }
    
    fun release() {
        soundPool.release()
    }
}
```

### 3.2 Integration in GameViewModel

```kotlin
// viewmodel/GameViewModel.kt (Audio Integration)
class GameViewModel(
    repository: GameRepository,
    private val musicManager: MusicManager,
    private val soundEffects: SoundEffectsManager,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {
    
    init {
        // Initialize audio
        musicManager.initialize()
        soundEffects.loadSounds()
        musicManager.playTheme(MusicManager.THEME_DAY)
    }
    
    fun onTapServe() {
        soundEffects.play("tap", volume = 0.8f)
        // Game logic
    }
    
    fun onUpgradeComplete() {
        soundEffects.play("upgrade")
        // Show celebration UI
    }
    
    fun onMapUnlock() {
        soundEffects.play("unlock")
        // Play unlock animation
    }
    
    fun onMarriage() {
        soundEffects.playWithDelay("marry", 500)
        // Marriage event
    }
    
    fun onBabyBorn() {
        soundEffects.play("baby")
        // Baby event
    }
    
    fun setMusicVolume(volume: Float) {
        musicManager.setVolume(volume)
    }
    
    override fun onCleared() {
        musicManager.release()
        soundEffects.release()
        super.onCleared()
    }
}
```

---

## D. Native C++ Integration for Simulation Updates

### 4.1 C++ Tick Handler with Character Effects

```cpp
// game_simulation.cpp
void GameState::tick(long deltaTimeMs) {
    // Existing tick logic...
    
    // Character productivity bonuses
    applyCharacterBonuses();
    
    // Offline earnings calculation
    applyOfflineEarnings();
    
    // Check gates completion
    checkMapGateCompletion();
}

void GameState::applyCharacterBonuses() {
    for (auto& stall : stalls) {
        float incomeBonus = 1.0f;
        
        for (auto& helper : stall.helpers) {
            // Each helper adds productivity
            incomeBonus += helper.productivityMultiplier;
        }
        
        // Apply bonuses to stall income
        stall.incomePerSecond *= incomeBonus;
    }
}

void GameState::checkMapGateCompletion() {
    for (int mapId = 0; mapId < MAX_MAPS; mapId++) {
        if (maps[mapId].isUnlocked) continue;
        
        bool allGatesComplete = true;
        
        for (auto& gate : maps[mapId].gates) {
            // Update current values based on game state
            if (gate.type == GATE_TYPE_UPGRADES) {
                gate.currentValue = calculateTotalUpgrades();
            } else if (gate.type == GATE_TYPE_HELPERS) {
                gate.currentValue = calculateTotalHelpers();
            } else if (gate.type == GATE_TYPE_EARNINGS) {
                gate.currentValue = totalEarnings;
            } else if (gate.type == GATE_TYPE_PLAYTIME) {
                gate.currentValue = totalPlaytimeSeconds / 3600;  // Convert to hours
            }
            
            gate.isCompleted = (gate.currentValue >= gate.targetValue);
            
            if (!gate.isCompleted) {
                allGatesComplete = false;
            }
        }
        
        if (allGatesComplete && !maps[mapId].isUnlocked) {
            maps[mapId].isUnlocked = true;
            maps[mapId].unlockedAt = getCurrentTimeMillis();
            
            // Trigger unlock event
            triggerMapUnlockEvent(mapId);
        }
    }
}
```

---

## Summary: All Files to Create/Modify

```
✅ NEW FILES:
├── models/
│   ├── MapProgressModels.kt
│   ├── CharacterModels.kt
│   └── FamilyModels.kt
├── entities/
│   ├── MapGateEntity.kt
│   ├── MapProgressEntity.kt
│   └── CharacterEntity.kt
├── dao/
│   ├── MapProgressDao.kt
│   └── CharacterDao.kt
├── repository/
│   ├── MapProgressRepository.kt
│   ├── CharacterRepository.kt
│   └── FamilyRepository.kt
├── viewmodel/
│   ├── MapProgressViewModel.kt
│   ├── CharacterViewModel.kt
│   └── FamilyViewModel.kt
├── ui/screens/
│   ├── MapGateProgressScreen.kt
│   ├── CharacterManagementScreen.kt
│   └── FamilySpendingScreen.kt
├── ui/components/
│   ├── CharacterCard.kt
│   ├── GateProgressCard.kt
│   └── SpendingCategoryCard.kt
├── managers/
│   ├── MusicManager.kt
│   └── SoundEffectsManager.kt
├── cpp/
│   ├── character_system.cpp/h
│   └── family_system.cpp/h
└── res/raw/
    ├── bg_music_*.mp3
    └── sfx_*.mp3

✅ MODIFIED FILES:
├── GameViewModel.kt (add music, character integration)
├── GameDatabase.kt (add new DAOs)
├── build.gradle.kts (add Media3, Hilt dependencies)
└── AndroidManifest.xml (add RECORD_AUDIO permission for potential future)
```

