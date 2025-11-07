package com.streettycoon.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.streettycoon.audio.AudioManager
import com.streettycoon.data.GameRepository
import com.streettycoon.game.model.ActionResult
import com.streettycoon.game.model.CharacterType
import com.streettycoon.game.model.GameState
import com.streettycoon.game.native.GameSimulation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.abs

/**
 * ViewModel managing the game simulation and state
 * Handles tick loop, save/load, and action execution
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)
    private val simulation: GameSimulation?
    val audioManager = AudioManager.getInstance(application)

    // Mutex for thread-safe simulation access
    private val simulationMutex = Mutex()

    // Game state flow
    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    // Separate state flows for frequently changing values (optimization)
    private val _money = MutableStateFlow(0.0)
    val money: StateFlow<Double> = _money.asStateFlow()

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state for native library loading
    private val _nativeLibraryError = MutableStateFlow<String?>(null)
    val nativeLibraryError: StateFlow<String?> = _nativeLibraryError.asStateFlow()

    // Offline earnings info
    private val _offlineEarnings = MutableStateFlow(0.0)
    val offlineEarnings: StateFlow<Double> = _offlineEarnings.asStateFlow()

    // Throttle threshold for money updates (avoid micro-updates)
    private val moneyUpdateThreshold = 0.01
    private var lastMoneyUpdate = 0.0

    // Track last snapshot hash to detect actual changes
    private var lastSnapshotHash = 0

    // Tick loop job
    private var tickJob: Job? = null

    // Auto-save job
    private var autoSaveJob: Job? = null

    private var lastTickTime = System.currentTimeMillis()

    init {
        Log.d(TAG, "GameViewModel initialized")

        // Try to initialize the native simulation
        var simTemp: GameSimulation? = null
        try {
            simTemp = GameSimulation()
            Log.d(TAG, "Native library loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize native library", e)
            _nativeLibraryError.value = "Failed to load game engine: ${e.message}"
            _isLoading.value = false
        }
        simulation = simTemp

        // Initialize audio system with error handling
        try {
            audioManager.initialize()
            audioManager.startMusic()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize audio system", e)
            // Continue even if audio fails - it's not critical for gameplay
        }

        // Only proceed if simulation was initialized successfully
        if (simulation != null) {
            loadOrCreateGame()
        }
    }

    /**
     * Load existing game or create new one
     */
    private fun loadOrCreateGame() {
        viewModelScope.launch {
            try {
                if (simulation == null) {
                    Log.e(TAG, "Cannot load game: simulation is null")
                    return@launch
                }

                val savedGame = repository.loadGame()

                if (savedGame != null) {
                    val (snapshotJson, lastPlayedTimestamp) = savedGame

                    // Initialize simulation from saved state
                    val success = simulation.initializeFromJson(snapshotJson)

                    if (success) {
                        // Calculate offline earnings and expenses
                        val offlineTimeMs = System.currentTimeMillis() - lastPlayedTimestamp
                        if (offlineTimeMs > 0) {
                            val earnings = simulation.calculateOfflineEarnings(offlineTimeMs)
                            val expenses = simulation.calculateOfflineExpenses(offlineTimeMs)
                            _offlineEarnings.value = earnings

                            // Apply offline expenses first (to reduce cash)
                            if (expenses > 0) {
                                simulation.applyOfflineExpenses(offlineTimeMs)
                                Log.d(TAG, "Applied offline expenses: $expenses")
                            }

                            // Apply offline earnings after expenses
                            if (earnings > 0) {
                                simulation.applyOfflineEarnings(offlineTimeMs)
                                Log.d(TAG, "Applied offline earnings: $earnings")
                            }
                        }

                        Log.d(TAG, "Game loaded from save")
                    } else {
                        Log.w(TAG, "Failed to load saved game, starting new game")
                        simulation.initializeNewGame()
                    }
                } else {
                    // No saved game, start new
                    simulation.initializeNewGame()
                    Log.d(TAG, "New game started")
                }

                // Update state
                updateGameState()

                // Check if game state is valid
                if (_gameState.value == null) {
                    Log.e(TAG, "Failed to get game state after initialization")
                    _isLoading.value = false
                    return@launch
                }

                _isLoading.value = false

                // Start tick loop
                startTickLoop()

                // Start auto-save
                startAutoSave()

            } catch (e: Exception) {
                Log.e(TAG, "Error loading game", e)
                _isLoading.value = false
            }
        }
    }

    /**
     * Start the game tick loop
     * Optimized to reduce unnecessary recompositions
     */
    private fun startTickLoop() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            lastTickTime = System.currentTimeMillis()
            var tickCount = 0

            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = currentTime - lastTickTime
                lastTickTime = currentTime

                // Tick simulation with thread-safety
                simulationMutex.withLock {
                    // Safety check: only tick if simulation is not null
                    if (simulation != null) {
                        simulation.tick(deltaTime)

                        // Get snapshot but be selective about updates
                        val snapshot = simulation.getSnapshot()

                        if (snapshot != null) {
                            // Update money and income more frequently but with throttling
                            val newMoney = snapshot.money
                            if (kotlin.math.abs(newMoney - lastMoneyUpdate) > moneyUpdateThreshold) {
                                _money.value = newMoney
                                lastMoneyUpdate = newMoney
                            }

                            val newIncome = snapshot.getTotalIncomePerSecond()
                            if (newIncome != _totalIncome.value) {
                                _totalIncome.value = newIncome
                            }

                            // Only update full game state every 5 ticks (500ms) or when hash changes
                            // This reduces recomposition frequency significantly
                            tickCount++
                            if (tickCount >= 5) {
                                val newHash = snapshot.hashCode()
                                if (newHash != lastSnapshotHash) {
                                    _gameState.value = snapshot
                                    lastSnapshotHash = newHash
                                }
                                tickCount = 0
                            }
                        }
                    }
                }

                // Tick every 100ms for smooth updates
                delay(100)
            }
        }
        Log.d(TAG, "Tick loop started with optimized recomposition")
    }

    /**
     * Start auto-save (every 30 seconds)
     */
    private fun startAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000) // 30 seconds
                saveGame()
            }
        }
        Log.d(TAG, "Auto-save started")
    }

    /**
     * Update the game state flow from simulation
     * Also updates separate money and income flows
     */
    private fun updateGameState() {
        val snapshot = simulation?.getSnapshot()
        _gameState.value = snapshot

        // Also update separate flows
        snapshot?.let {
            _money.value = it.money
            lastMoneyUpdate = it.money
            _totalIncome.value = it.getTotalIncomePerSecond()
            lastSnapshotHash = it.hashCode()
        }
    }

    /**
     * Save game to database
     */
    fun saveGame() {
        viewModelScope.launch {
            try {
                if (simulation == null) {
                    Log.w(TAG, "Cannot save game: simulation is null")
                    return@launch
                }
                simulationMutex.withLock {
                    repository.saveGame(simulation)
                }
                repository.updateLastPlayed()
                Log.d(TAG, "Game saved")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving game", e)
            }
        }
    }

    /**
     * Start a new game (resets everything)
     */
    fun startNewGame() {
        viewModelScope.launch {
            if (simulation == null) {
                Log.w(TAG, "Cannot start new game: simulation is null")
                return@launch
            }
            repository.deleteGame()
            simulation.initializeNewGame()
            updateGameState()
            saveGame()
            Log.d(TAG, "New game started")
        }
    }

    /**
     * Dismiss offline earnings notification
     */
    fun dismissOfflineEarnings() {
        _offlineEarnings.value = 0.0
    }

    // ===== GAME ACTIONS =====

    /**
     * Tap to serve a customer
     */
    fun tapServe(stallId: Int) {
        if (simulation == null) return
        val result = simulation.tapServe(stallId)
        if (result != null) {
            if (result.success) {
                // Ensure audio calls happen on main thread
                viewModelScope.launch(Dispatchers.Main) {
                    try {
                        audioManager.playTapServe()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error playing tap serve sound", e)
                    }
                }
            }
            handleActionResult(result)
        } else {
            Log.e(TAG, "tapServe returned null for stallId: $stallId")
        }
    }

    /**
     * Upgrade a stall
     */
    fun upgradeStall(stallId: Int) {
        if (simulation == null) return
        val result = simulation.upgradeStall(stallId)
        if (result != null) {
            try {
                if (result.success) {
                    audioManager.playUpgrade()
                } else {
                    audioManager.playError()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing audio", e)
            }
            handleActionResult(result)
            if (result.success) {
                saveGame()
            }
        } else {
            Log.e(TAG, "upgradeStall returned null for stallId: $stallId")
        }
    }

    /**
     * Hire a helper
     */
    fun hireHelper(stallId: Int) {
        if (simulation == null) return
        val result = simulation.hireHelper(stallId)
        if (result != null) {
            try {
                if (result.success) {
                    audioManager.playPurchase()
                } else {
                    audioManager.playError()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing audio", e)
            }
            handleActionResult(result)
            if (result.success) {
                saveGame()
            }
        } else {
            Log.e(TAG, "hireHelper returned null for stallId: $stallId")
        }
    }

    /**
     * Unlock a stall
     */
    fun unlockStall(stallId: Int) {
        if (simulation == null) return
        val result = simulation.unlockStall(stallId)
        if (result != null) {
            try {
                if (result.success) {
                    audioManager.playUnlock()
                } else {
                    audioManager.playError()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing audio", e)
            }
            handleActionResult(result)
            if (result.success) {
                saveGame()
            }
        } else {
            Log.e(TAG, "unlockStall returned null for stallId: $stallId")
        }
    }

    /**
     * Unlock a zone
     */
    fun unlockZone(zoneId: Int) {
        if (simulation == null) return
        val result = simulation.unlockZone(zoneId)
        if (result != null) {
            try {
                if (result.success) {
                    audioManager.playUnlock()
                } else {
                    audioManager.playError()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing audio", e)
            }
            handleActionResult(result)
            if (result.success) {
                saveGame()
            }
        } else {
            Log.e(TAG, "unlockZone returned null for zoneId: $zoneId")
        }
    }

    /**
     * Claim daily reward
     */
    fun claimDailyReward() {
        if (simulation == null) return
        val result = simulation.claimDailyReward()
        if (result != null) {
            handleActionResult(result)
            if (result.success) {
                saveGame()
            }
        } else {
            Log.e(TAG, "claimDailyReward returned null")
        }
    }

    /**
     * Hire a character
     */
    fun hireCharacter(type: CharacterType, name: String, stallId: Int) {
        viewModelScope.launch {
            if (simulation == null) return@launch
            simulationMutex.withLock {
                val result = simulation.applyAction(
                    "hire_character",
                    mapOf(
                        "characterType" to type.name,
                        "name" to name,
                        "stallId" to stallId
                    )
                )
                if (result != null) {
                    try {
                        if (result.success) {
                            audioManager.playPurchase()
                        } else {
                            audioManager.playError()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error playing audio", e)
                    }
                    handleActionResult(result)
                    if (result.success) {
                        saveGame()
                    }
                } else {
                    Log.e(TAG, "hireCharacter returned null for type: $type")
                }
            }
        }
    }

    /**
     * Level up a character
     */
    fun levelUpCharacter(characterId: String) {
        viewModelScope.launch {
            if (simulation == null) return@launch
            simulationMutex.withLock {
                val result = simulation.applyAction(
                    "level_up_character",
                    mapOf("characterId" to characterId)
                )
                if (result != null) {
                    try {
                        if (result.success) {
                            audioManager.playLevelUp()
                        } else {
                            audioManager.playError()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error playing audio", e)
                    }
                    handleActionResult(result)
                } else {
                    Log.e(TAG, "levelUpCharacter returned null for characterId: $characterId")
                }
            }
        }
    }

    /**
     * Assign a character to a different stall
     */
    fun assignCharacter(characterId: String, stallId: Int) {
        viewModelScope.launch {
            if (simulation == null) return@launch
            simulationMutex.withLock {
                val result = simulation.applyAction(
                    "assign_character",
                    mapOf(
                        "characterId" to characterId,
                        "stallId" to stallId
                    )
                )
                if (result != null) {
                    handleActionResult(result)
                    if (result.success) {
                        saveGame()
                    }
                } else {
                    Log.e(TAG, "assignCharacter returned null for characterId: $characterId")
                }
            }
        }
    }

    /**
     * Upgrade a spending category
     */
    fun upgradeCategory(categoryId: String) {
        viewModelScope.launch {
            if (simulation == null) return@launch
            simulationMutex.withLock {
                val result = simulation.applyAction(
                    "upgrade_category",
                    mapOf("categoryId" to categoryId)
                )
                if (result != null) {
                    try {
                        if (result.success) {
                            audioManager.playUpgrade()
                        } else {
                            audioManager.playError()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error playing audio", e)
                    }
                    handleActionResult(result)
                    if (result.success) {
                        saveGame()
                    }
                } else {
                    Log.e(TAG, "upgradeCategory returned null for categoryId: $categoryId")
                }
            }
        }
    }

    /**
     * Get married
     */
    fun getMarried(spouseName: String) {
        viewModelScope.launch {
            if (simulation == null) return@launch
            simulationMutex.withLock {
                val result = simulation.applyAction(
                    "get_married",
                    mapOf("spouseName" to spouseName)
                )
                if (result != null) {
                    try {
                        if (result.success) {
                            audioManager.playPurchase()
                        } else {
                            audioManager.playError()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error playing audio", e)
                    }
                    handleActionResult(result)
                    if (result.success) {
                        saveGame()
                    }
                } else {
                    Log.e(TAG, "getMarried returned null")
                }
            }
        }
    }

    /**
     * Have a baby
     */
    fun haveBaby(babyName: String) {
        viewModelScope.launch {
            if (simulation == null) return@launch
            simulationMutex.withLock {
                val result = simulation.applyAction(
                    "have_baby",
                    mapOf("babyName" to babyName)
                )
                if (result != null) {
                    try {
                        if (result.success) {
                            audioManager.playPurchase()
                        } else {
                            audioManager.playError()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error playing audio", e)
                    }
                    handleActionResult(result)
                    if (result.success) {
                        saveGame()
                    }
                } else {
                    Log.e(TAG, "haveBaby returned null")
                }
            }
        }
    }

    private fun handleActionResult(result: ActionResult) {
        if (result.success) {
            updateGameState()
        }
        Log.d(TAG, "Action result: ${result.message} (success=${result.success})")
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel background jobs first
        tickJob?.cancel()
        autoSaveJob?.cancel()

        // Release audio resources with error handling
        try {
            audioManager.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio resources", e)
        }

        // Save game with mutex protection - use runBlocking to ensure completion
        // before ViewModel is destroyed
        runBlocking {
            try {
                if (simulation != null) {
                    simulationMutex.withLock {
                        repository.saveGame(simulation)
                        Log.d(TAG, "Final save before clearing")
                    }
                    // Destroy simulation
                    simulation.destroy()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in final save", e)
            }
        }
        Log.d(TAG, "GameViewModel cleared")
    }

    companion object {
        private const val TAG = "GameViewModel"
    }
}
