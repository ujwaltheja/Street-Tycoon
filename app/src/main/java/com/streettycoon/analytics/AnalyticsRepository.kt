package com.streettycoon.analytics

import android.content.Context
import com.streettycoon.data.GameDatabase
import com.streettycoon.data.GameEventEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Repository for analytics operations
 * Provides high-level interface for event tracking and statistics
 */
class AnalyticsRepository(context: Context) {

    private val database = GameDatabase.getDatabase(context)
    private val eventDao = database.gameEventDao()
    private val eventTracker = EventTracker.getInstance(context)

    /**
     * Log a game event
     */
    suspend fun logEvent(event: GameEvent) {
        eventTracker.logEvent(event)
    }

    /**
     * Get recent events for display
     */
    suspend fun getRecentEvents(limit: Int = 100): List<GameEventEntity> =
        withContext(Dispatchers.IO) {
            eventDao.getRecentEvents(limit)
        }

    /**
     * Get events by name
     */
    suspend fun getEventsByName(eventName: String): List<GameEventEntity> =
        withContext(Dispatchers.IO) {
            eventDao.getEventsByName(eventName)
        }

    /**
     * Get total event count
     */
    suspend fun getTotalEventCount(): Int = withContext(Dispatchers.IO) {
        eventDao.getEventCount()
    }

    /**
     * Calculate game statistics from events
     */
    suspend fun getGameStats(): GameStats = withContext(Dispatchers.IO) {
        val allEvents = eventDao.getRecentEvents(10000) // Get all recent events

        val sessionStarts = allEvents.count { it.eventName == "session_start" }
        val tutorialCompletes = allEvents.count { it.eventName == "tutorial_completed" }
        val zonesUnlocked = allEvents.count { it.eventName == "zone_unlocked" }
        val helpersHired = allEvents.count { it.eventName == "helper_hired" }
        val upgradesCompleted = allEvents.count { it.eventName == "upgrade_completed" }

        GameStats(
            totalSessions = sessionStarts,
            tutorialCompletionRate = if (sessionStarts > 0) {
                (tutorialCompletes.toFloat() / sessionStarts) * 100
            } else 0f,
            totalZonesUnlocked = zonesUnlocked,
            totalHelpersHired = helpersHired,
            totalUpgrades = upgradesCompleted,
            totalEvents = allEvents.size
        )
    }

    /**
     * Sync pending events to Firebase
     */
    fun syncPendingEvents() {
        eventTracker.syncPendingEvents()
    }

    /**
     * Clean up old events
     */
    fun cleanupOldEvents() {
        eventTracker.cleanupOldEvents()
    }
}

/**
 * Data class for game statistics
 */
data class GameStats(
    val totalSessions: Int,
    val tutorialCompletionRate: Float,
    val totalZonesUnlocked: Int,
    val totalHelpersHired: Int,
    val totalUpgrades: Int,
    val totalEvents: Int
)
