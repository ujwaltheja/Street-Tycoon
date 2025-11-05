package com.streettycoon.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.streettycoon.game.model.GameState
import com.streettycoon.game.native.GameSimulation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing game data persistence
 * Handles save/load operations between Room database and native simulation
 */
class GameRepository(context: Context) {

    private val database = GameDatabase.getDatabase(context)
    private val snapshotDao = database.gameSnapshotDao()
    private val prefsDao = database.playerPrefsDao()
    private val gson = Gson()

    /**
     * Save current game state to database
     */
    suspend fun saveGame(simulation: GameSimulation) = withContext(Dispatchers.IO) {
        try {
            val snapshotJson = simulation.getSnapshotJson()
            val state = simulation.getSnapshot()

            if (state == null) {
                Log.e(TAG, "Cannot save game: state is null")
                return@withContext
            }

            val entity = GameSnapshotEntity(
                id = 1,
                snapshotJson = snapshotJson,
                timestamp = System.currentTimeMillis(),
                playerCash = state.playerCash,
                playerLevel = state.currentDay
            )

            snapshotDao.saveSnapshot(entity)
            Log.d(TAG, "Game saved successfully. Cash: ${state.playerCash}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving game", e)
            throw e
        }
    }

    /**
     * Load game state from database
     * @return Pair of (snapshotJson, lastPlayedTimestamp) or null if no save exists
     */
    suspend fun loadGame(): Pair<String, Long>? = withContext(Dispatchers.IO) {
        try {
            val entity = snapshotDao.getSnapshot()
            if (entity != null) {
                Log.d(TAG, "Game loaded. Cash: ${entity.playerCash}")
                Pair(entity.snapshotJson, entity.timestamp)
            } else {
                Log.d(TAG, "No saved game found")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading game", e)
            null
        }
    }

    /**
     * Delete all saved game data (for new game)
     */
    suspend fun deleteGame() = withContext(Dispatchers.IO) {
        try {
            snapshotDao.deleteAll()
            Log.d(TAG, "Game data deleted")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting game", e)
        }
    }

    /**
     * Save player preferences
     */
    suspend fun savePreferences(prefs: PlayerPrefsEntity) = withContext(Dispatchers.IO) {
        prefsDao.savePrefs(prefs)
    }

    /**
     * Load player preferences
     */
    suspend fun loadPreferences(): PlayerPrefsEntity = withContext(Dispatchers.IO) {
        prefsDao.getPrefs() ?: PlayerPrefsEntity()
    }

    /**
     * Update last played timestamp
     */
    suspend fun updateLastPlayed() = withContext(Dispatchers.IO) {
        val prefs = loadPreferences()
        savePreferences(prefs.copy(lastPlayedTimestamp = System.currentTimeMillis()))
    }

    companion object {
        private const val TAG = "GameRepository"
    }
}
