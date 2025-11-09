package com.streettycoon.data

import android.content.Context
import androidx.room.*
import com.google.gson.Gson
import java.util.UUID

/**
 * Room entity for storing game snapshots with version control
 * Supports 3-version backup system for corruption recovery
 */
@Entity(tableName = "game_snapshots")
data class GameSnapshotEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val version: Int, // 1 (current), 2 (backup1), 3 (backup2)
    val snapshotJson: String,
    val checksum: String, // SHA-256 for validation
    val timestamp: Long,
    val playerCash: Double, // Denormalized for quick access
    val playerLevel: Int,
    @ColumnInfo(name = "is_corrupted") val isCorrupted: Boolean = false
)

/**
 * Metadata for save system
 */
@Entity(tableName = "save_metadata")
data class SaveMetadataEntity(
    @PrimaryKey val id: Int = 1,
    val lastSaveTime: Long,
    val lastSaveChecksum: String,
    val lastSuccessfulLoad: Long,
    val corruptionCount: Int = 0,
    val totalSaves: Int = 0
)

/**
 * Room entity for storing analytics events
 */
@Entity(tableName = "game_events")
data class GameEventEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val eventName: String,
    val eventData: String, // JSON
    val timestamp: Long = System.currentTimeMillis(),
    val syncedToFirebase: Boolean = false
)

/**
 * Room entity for storing player preferences
 * Enhanced with all required settings for persistence
 */
@Entity(tableName = "player_prefs")
data class PlayerPrefsEntity(
    @PrimaryKey val id: Int = 1,
    val musicVolume: Float = 0.8f,
    val sfxVolume: Float = 0.8f,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val languageCode: String = "en", // "en", "hi", "kn"
    val vibrationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val tutorialCompleted: Boolean = false,
    val lastPlayedTimestamp: Long = 0,
    val installTimestamp: Long = System.currentTimeMillis(),
    val autoSaveInterval: Int = 30 // seconds
)

/**
 * DAO for game snapshots - supports 3-version backup system
 */
@Dao
interface GameSnapshotDao {
    @Query("SELECT * FROM game_snapshots WHERE version = :version LIMIT 1")
    suspend fun getByVersion(version: Int): GameSnapshotEntity?

    @Query("SELECT * FROM game_snapshots ORDER BY version ASC LIMIT :limit")
    suspend fun getByOrder(limit: Int = 3): List<GameSnapshotEntity>

    @Query("SELECT * FROM game_snapshots")
    suspend fun getAll(): List<GameSnapshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: GameSnapshotEntity)

    @Update
    suspend fun update(snapshot: GameSnapshotEntity)

    @Delete
    suspend fun delete(snapshot: GameSnapshotEntity)

    @Query("DELETE FROM game_snapshots")
    suspend fun deleteAll()

    @Query("DELETE FROM game_snapshots WHERE version = :version")
    suspend fun deleteByVersion(version: Int)
}

/**
 * DAO for save metadata
 */
@Dao
interface SaveMetadataDao {
    @Query("SELECT * FROM save_metadata WHERE id = 1")
    suspend fun getMetadata(): SaveMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: SaveMetadataEntity)

    @Update
    suspend fun update(metadata: SaveMetadataEntity)

    @Query("UPDATE save_metadata SET lastSuccessfulLoad = :timestamp WHERE id = 1")
    suspend fun updateLastSuccessfulLoad(timestamp: Long)
}

/**
 * DAO for analytics events
 */
@Dao
interface GameEventDao {
    @Insert
    suspend fun insertEvent(event: GameEventEntity)

    @Query("SELECT * FROM game_events WHERE syncedToFirebase = 0 ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getUnsyncedEvents(limit: Int = 100): List<GameEventEntity>

    @Query("SELECT * FROM game_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentEvents(limit: Int = 100): List<GameEventEntity>

    @Query("SELECT * FROM game_events WHERE eventName = :eventName ORDER BY timestamp DESC")
    suspend fun getEventsByName(eventName: String): List<GameEventEntity>

    @Query("UPDATE game_events SET syncedToFirebase = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    @Query("DELETE FROM game_events WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldEvents(beforeTimestamp: Long)

    @Query("SELECT COUNT(*) FROM game_events")
    suspend fun getEventCount(): Int
}

/**
 * DAO for player preferences
 */
@Dao
interface PlayerPrefsDao {
    @Query("SELECT * FROM player_prefs WHERE id = 1")
    suspend fun getPrefs(): PlayerPrefsEntity?

    @Query("SELECT * FROM player_prefs WHERE id = 1")
    fun getPrefsSync(): PlayerPrefsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePrefs(prefs: PlayerPrefsEntity)

    @Update
    suspend fun update(prefs: PlayerPrefsEntity)
}

/**
 * Room database for Street Tycoon
 * Enhanced with backup system, analytics, and comprehensive settings
 */
@Database(
    entities = [
        GameSnapshotEntity::class,
        SaveMetadataEntity::class,
        GameEventEntity::class,
        PlayerPrefsEntity::class
    ],
    version = 2, // Incremented for new entities
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameSnapshotDao(): GameSnapshotDao
    abstract fun saveMetadataDao(): SaveMetadataDao
    abstract fun gameEventDao(): GameEventDao
    abstract fun playerPrefsDao(): PlayerPrefsDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "street_tycoon_database"
                )
                    .fallbackToDestructiveMigration() // For now, during development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
