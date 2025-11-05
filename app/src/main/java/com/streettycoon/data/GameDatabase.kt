package com.streettycoon.data

import android.content.Context
import androidx.room.*
import com.google.gson.Gson

/**
 * Room entity for storing game snapshots
 */
@Entity(tableName = "game_snapshots")
data class GameSnapshotEntity(
    @PrimaryKey val id: Int = 1, // Single row for current game
    val snapshotJson: String,
    val timestamp: Long,
    val playerCash: Double, // Denormalized for quick access
    val playerLevel: Int
)

/**
 * Room entity for storing player preferences
 */
@Entity(tableName = "player_prefs")
data class PlayerPrefsEntity(
    @PrimaryKey val id: Int = 1,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val language: String = "en",
    val lastPlayedTimestamp: Long = 0
)

/**
 * DAO for game snapshots
 */
@Dao
interface GameSnapshotDao {
    @Query("SELECT * FROM game_snapshots WHERE id = 1")
    suspend fun getSnapshot(): GameSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSnapshot(snapshot: GameSnapshotEntity)

    @Query("DELETE FROM game_snapshots")
    suspend fun deleteAll()
}

/**
 * DAO for player preferences
 */
@Dao
interface PlayerPrefsDao {
    @Query("SELECT * FROM player_prefs WHERE id = 1")
    suspend fun getPrefs(): PlayerPrefsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePrefs(prefs: PlayerPrefsEntity)
}

/**
 * Room database for Street Tycoon
 */
@Database(
    entities = [GameSnapshotEntity::class, PlayerPrefsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameSnapshotDao(): GameSnapshotDao
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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
