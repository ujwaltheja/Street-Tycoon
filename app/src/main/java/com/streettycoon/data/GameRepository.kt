package com.streettycoon.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.streettycoon.game.model.GameState
import com.streettycoon.game.native.GameSimulation
import com.streettycoon.utils.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing game data persistence
 * Enhanced with 3-version backup system, corruption recovery, and analytics
 */
class GameRepository(context: Context) {

    private val database = GameDatabase.getDatabase(context)
    private val snapshotDao = database.gameSnapshotDao()
    private val metadataDao = database.saveMetadataDao()
    private val eventDao = database.gameEventDao()
    private val prefsDao = database.playerPrefsDao()
    private val gson = Gson()

    // Backup manager for 3-version rotation
    private val backupManager = BackupManager(database)

    /**
     * Save current game state to database with automatic backup rotation
     * Implements 3-version backup system for corruption recovery
     */
    suspend fun saveGame(simulation: GameSimulation) = withContext(Dispatchers.IO) {
        try {
            val snapshotJson = simulation.getSnapshotJson()
            val state = simulation.getSnapshot()

            if (state == null) {
                Log.e(TAG, "Cannot save game: state is null")
                return@withContext
            }

            // Use backup manager to save with rotation
            backupManager.saveWithBackup(
                snapshotJson = snapshotJson,
                playerCash = state.playerCash,
                playerLevel = state.currentDay
            )

            Log.d(TAG, "Game saved successfully with backup. Cash: ${state.playerCash}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving game", e)
            throw e
        }
    }

    /**
     * Load game state from database with automatic corruption recovery
     * Tries current save, then backups if corrupted
     * @return Pair of (snapshotJson, lastPlayedTimestamp) or null if no save exists
     */
    suspend fun loadGame(): Pair<String, Long>? = withContext(Dispatchers.IO) {
        try {
            val result = backupManager.loadWithRecovery()
            if (result != null) {
                Log.d(TAG, "Game loaded successfully")
                result
            } else {
                Log.d(TAG, "No valid saved game found")
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
            metadataDao.getMetadata()?.let { metadata ->
                metadataDao.insert(
                    metadata.copy(
                        lastSaveTime = System.currentTimeMillis(),
                        lastSaveChecksum = "",
                        corruptionCount = 0,
                        totalSaves = 0
                    )
                )
            }
            Log.d(TAG, "Game data deleted")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting game", e)
        }
    }

    /**
     * Get all available backups for recovery UI
     */
    suspend fun getBackups(): List<GameSnapshotEntity> = withContext(Dispatchers.IO) {
        backupManager.getAllBackups()
    }

    /**
     * Recover from specific backup version
     */
    suspend fun recoverFromBackup(version: Int): Boolean = withContext(Dispatchers.IO) {
        backupManager.recoverFromBackup(version)
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

    /**
     * Update specific preference settings
     */
    suspend fun updateMusicVolume(volume: Float) = withContext(Dispatchers.IO) {
        val prefs = loadPreferences()
        savePreferences(prefs.copy(musicVolume = volume))
    }

    suspend fun updateSfxVolume(volume: Float) = withContext(Dispatchers.IO) {
        val prefs = loadPreferences()
        savePreferences(prefs.copy(sfxVolume = volume))
    }

    suspend fun updateLanguage(languageCode: String) = withContext(Dispatchers.IO) {
        val prefs = loadPreferences()
        savePreferences(prefs.copy(languageCode = languageCode))
    }

    suspend fun updateVibration(enabled: Boolean) = withContext(Dispatchers.IO) {
        val prefs = loadPreferences()
        savePreferences(prefs.copy(vibrationEnabled = enabled))
    }

    suspend fun markTutorialCompleted() = withContext(Dispatchers.IO) {
        val prefs = loadPreferences()
        savePreferences(prefs.copy(tutorialCompleted = true))
    }

    /**
     * Log analytics event
     */
    suspend fun logEvent(event: GameEventEntity) = withContext(Dispatchers.IO) {
        try {
            eventDao.insertEvent(event)
        } catch (e: Exception) {
            Log.e(TAG, "Error logging event", e)
        }
    }

    /**
     * Get unsynced events for Firebase
     */
    suspend fun getUnsyncedEvents(limit: Int = 50): List<GameEventEntity> =
        withContext(Dispatchers.IO) {
            eventDao.getUnsyncedEvents(limit)
        }

    /**
     * Mark events as synced to Firebase
     */
    suspend fun markEventsSynced(eventIds: List<String>) = withContext(Dispatchers.IO) {
        eventDao.markSynced(eventIds)
    }

    /**
     * Get recent events for analytics screen
     */
    suspend fun getRecentEvents(limit: Int = 100): List<GameEventEntity> =
        withContext(Dispatchers.IO) {
            eventDao.getRecentEvents(limit)
        }

    companion object {
        private const val TAG = "GameRepository"
    }
}

