package com.streettycoon.utils

import android.util.Log
import com.streettycoon.data.GameDatabase
import com.streettycoon.data.GameSnapshotEntity
import com.streettycoon.data.SaveMetadataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages the 3-version backup rotation system
 * Ensures game progress is never lost due to corruption
 */
class BackupManager(private val database: GameDatabase) {

    private val snapshotDao = database.gameSnapshotDao()
    private val metadataDao = database.saveMetadataDao()

    /**
     * Save game state with automatic backup rotation
     * Maintains 3 versions: current, backup1, backup2
     */
    suspend fun saveWithBackup(
        snapshotJson: String,
        playerCash: Double,
        playerLevel: Int
    ) = withContext(Dispatchers.IO) {
        try {
            // Validate before saving
            if (!SaveValidator.validateJsonStructure(snapshotJson)) {
                throw IllegalArgumentException("Invalid game state structure")
            }

            val checksum = SaveValidator.calculateChecksum(snapshotJson)

            // Get current backups
            val currentBackups = snapshotDao.getByOrder(limit = 3)

            // Rotate versions: v1→v2, v2→v3, v3→delete
            if (currentBackups.size >= 3) {
                // Delete oldest backup (version 3)
                currentBackups.find { it.version == 3 }?.let {
                    snapshotDao.delete(it)
                }
            }

            // Shift version numbers: v1→v2, v2→v3
            currentBackups.sortedBy { it.version }.forEach { snapshot ->
                val updated = snapshot.copy(version = snapshot.version + 1)
                snapshotDao.update(updated)
            }

            // Insert new save as v1 (current)
            val newSnapshot = GameSnapshotEntity(
                version = 1,
                snapshotJson = snapshotJson,
                checksum = checksum,
                timestamp = System.currentTimeMillis(),
                playerCash = playerCash,
                playerLevel = playerLevel,
                isCorrupted = false
            )
            snapshotDao.insert(newSnapshot)

            // Update metadata
            val metadata = metadataDao.getMetadata() ?: SaveMetadataEntity(
                lastSaveTime = System.currentTimeMillis(),
                lastSaveChecksum = checksum,
                lastSuccessfulLoad = System.currentTimeMillis(),
                totalSaves = 1
            )

            metadataDao.insert(
                metadata.copy(
                    lastSaveTime = System.currentTimeMillis(),
                    lastSaveChecksum = checksum,
                    totalSaves = metadata.totalSaves + 1
                )
            )

            Log.d(TAG, "Game saved successfully with backup rotation. Total saves: ${metadata.totalSaves + 1}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving game with backup", e)
            throw e
        }
    }

    /**
     * Load game state with automatic corruption recovery
     * Tries v1, then v2, then v3 if corrupted
     */
    suspend fun loadWithRecovery(): Pair<String, Long>? = withContext(Dispatchers.IO) {
        // Try v1 (current)
        val current = snapshotDao.getByVersion(1)
        if (current != null && validateSnapshot(current)) {
            metadataDao.updateLastSuccessfulLoad(System.currentTimeMillis())
            Log.d(TAG, "Loaded current save (v1)")
            return@withContext Pair(current.snapshotJson, current.timestamp)
        }

        // If v1 corrupted, try backups
        Log.w(TAG, "Current save corrupted, trying backups...")
        return@withContext tryBackups()
    }

    /**
     * Validate a snapshot is not corrupted
     */
    private fun validateSnapshot(snapshot: GameSnapshotEntity): Boolean {
        return SaveValidator.validateSnapshot(snapshot.snapshotJson, snapshot.checksum)
    }

    /**
     * Try loading from backup versions
     */
    private suspend fun tryBackups(): Pair<String, Long>? {
        val backups = snapshotDao.getByOrder(limit = 3)

        for (backup in backups.sortedBy { it.version }) {
            if (validateSnapshot(backup)) {
                Log.i(TAG, "Recovered from backup v${backup.version}")

                // Promote backup to v1
                val promoted = backup.copy(version = 1)
                snapshotDao.insert(promoted)

                // Update metadata
                val metadata = metadataDao.getMetadata()
                metadata?.let {
                    metadataDao.update(
                        it.copy(
                            lastSuccessfulLoad = System.currentTimeMillis(),
                            corruptionCount = it.corruptionCount + 1
                        )
                    )
                }

                return Pair(backup.snapshotJson, backup.timestamp)
            }
        }

        Log.e(TAG, "All saves corrupted!")
        return null
    }

    /**
     * Get list of corrupted saves for debugging
     */
    suspend fun getCorruptedSaves(): List<GameSnapshotEntity> = withContext(Dispatchers.IO) {
        snapshotDao.getAll().filter { snapshot ->
            !validateSnapshot(snapshot)
        }
    }

    /**
     * Get all available backups with their metadata
     */
    suspend fun getAllBackups(): List<GameSnapshotEntity> = withContext(Dispatchers.IO) {
        snapshotDao.getByOrder(limit = 3)
    }

    /**
     * Recover from a specific backup version
     */
    suspend fun recoverFromBackup(version: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val backup = snapshotDao.getByVersion(version)
            if (backup != null && validateSnapshot(backup)) {
                // Promote to v1
                val promoted = backup.copy(version = 1)
                snapshotDao.insert(promoted)

                metadataDao.updateLastSuccessfulLoad(System.currentTimeMillis())
                Log.i(TAG, "Successfully recovered from backup v$version")
                true
            } else {
                Log.e(TAG, "Cannot recover from backup v$version - corrupted or not found")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error recovering from backup v$version", e)
            false
        }
    }

    companion object {
        private const val TAG = "BackupManager"
    }
}
