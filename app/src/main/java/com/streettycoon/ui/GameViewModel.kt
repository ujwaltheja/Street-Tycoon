package com.streettycoon.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.streettycoon.data.GameRepository
import com.streettycoon.game.model.ActionResult
import com.streettycoon.game.model.GameState
import com.streettycoon.game.native.GameSimulation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ViewModel managing the game simulation and state
 * Handles tick loop, save/load, and action execution
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)
    private val simulation = GameSimulation()

    // Game state flow
    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Offline earnings info
    private val _offlineEarnings = MutableStateFlow(0.0)
    val offlineEarnings: StateFlow<Double> = _offlineEarnings.asStateFlow()

    // Tick loop job
    private var tickJob: Job? = null

    // Auto-save job
    private var autoSaveJob: Job? = null

    private var lastTickTime = System.currentTimeMillis()

    init {
        Log.d(TAG, "GameViewModel initialized")
        loadOrCreateGame()
    }

    /**
     * Load existing game or create new one
     */
    private fun loadOrCreateGame() {
        viewModelScope.launch {
            try {
                val savedGame = repository.loadGame()

                if (savedGame != null) {
                    val (snapshotJson, lastPlayedTimestamp) = savedGame

                    // Initialize simulation from saved state
                    val success = simulation.initializeFromJson(snapshotJson)

                    if (success) {
                        // Calculate offline earnings
                        val offlineTimeMs = System.currentTimeMillis() - lastPlayedTimestamp
                        if (offlineTimeMs > 0) {
                            val earnings = simulation.calculateOfflineEarnings(offlineTimeMs)
                            _offlineEarnings.value = earnings

                            // Apply offline earnings
                            if (earnings > 0) {
                                simulation.applyOfflineEarnings(offlineTimeMs)
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
     */
    private fun startTickLoop() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            lastTickTime = System.currentTimeMillis()

            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = currentTime - lastTickTime
                lastTickTime = currentTime

                // Tick simulation
                simulation.tick(deltaTime)

                // Update UI state
                updateGameState()

                // Tick every 100ms for smooth updates
                delay(100)
            }
        }
        Log.d(TAG, "Tick loop started")
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
     */
    private fun updateGameState() {
        _gameState.value = simulation.getSnapshot()
    }

    /**
     * Save game to database
     */
    fun saveGame() {
        viewModelScope.launch {
            try {
                repository.saveGame(simulation)
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
        val result = simulation.tapServe(stallId)
        if (result != null) {
            handleActionResult(result)
        } else {
            Log.e(TAG, "tapServe returned null for stallId: $stallId")
        }
    }

    /**
     * Upgrade a stall
     */
    fun upgradeStall(stallId: Int) {
        val result = simulation.upgradeStall(stallId)
        if (result != null) {
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
        val result = simulation.hireHelper(stallId)
        if (result != null) {
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
        val result = simulation.unlockStall(stallId)
        if (result != null) {
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
        val result = simulation.unlockZone(zoneId)
        if (result != null) {
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

    private fun handleActionResult(result: ActionResult) {
        if (result.success) {
            updateGameState()
        }
        Log.d(TAG, "Action result: ${result.message} (success=${result.success})")
    }

    override fun onCleared() {
        super.onCleared()
        tickJob?.cancel()
        autoSaveJob?.cancel()
        saveGame()
        simulation.destroy()
        Log.d(TAG, "GameViewModel cleared")
    }

    companion object {
        private const val TAG = "GameViewModel"
    }
}
