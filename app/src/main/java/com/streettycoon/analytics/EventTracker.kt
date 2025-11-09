package com.streettycoon.analytics

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.streettycoon.data.GameDatabase
import com.streettycoon.data.GameEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * EventTracker - Hybrid analytics system
 * Works offline (local DB) and online (Firebase sync)
 *
 * Features:
 * - Always saves events locally first
 * - Syncs to Firebase when network available
 * - No internet = game still works perfectly
 * - When internet available = insights visible in Firebase Console
 */
class EventTracker(private val context: Context) {

    private val database = GameDatabase.getDatabase(context)
    private val eventDao = database.gameEventDao()
    private val firebase: FirebaseAnalytics = Firebase.analytics
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Log a game event
     * Always saves locally, syncs to Firebase if online
     */
    fun logEvent(event: GameEvent) {
        scope.launch {
            try {
                // Always save locally first
                val entity = event.toEntity()
                eventDao.insertEvent(entity)

                // If network available, sync to Firebase immediately
                if (isNetworkAvailable()) {
                    syncEventToFirebase(event)
                }

                Log.d(TAG, "Event logged: ${event.eventName}")
            } catch (e: Exception) {
                Log.e(TAG, "Error logging event: ${event.eventName}", e)
            }
        }
    }

    /**
     * Sync unsynced events to Firebase
     * Called periodically when network becomes available
     */
    fun syncPendingEvents() {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "No network available, skipping sync")
            return
        }

        scope.launch {
            try {
                val unsyncedEvents = eventDao.getUnsyncedEvents(50)
                val syncedIds = mutableListOf<String>()

                unsyncedEvents.forEach { entity ->
                    try {
                        val eventData = JSONObject(entity.eventData)
                        val eventMap = eventData.toMap()

                        firebase.logEvent(entity.eventName) {
                            eventMap.forEach { (key, value) ->
                                when (value) {
                                    is String -> param(key, value)
                                    is Int -> param(key, value.toLong())
                                    is Long -> param(key, value)
                                    is Double -> param(key, value)
                                    is Boolean -> param(key, if (value) 1L else 0L)
                                    else -> param(key, value.toString())
                                }
                            }
                        }

                        syncedIds.add(entity.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing event ${entity.eventName}", e)
                    }
                }

                // Mark as synced
                if (syncedIds.isNotEmpty()) {
                    eventDao.markSynced(syncedIds)
                    Log.d(TAG, "Synced ${syncedIds.size} events to Firebase")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing pending events", e)
            }
        }
    }

    /**
     * Sync single event to Firebase immediately
     */
    private fun syncEventToFirebase(event: GameEvent) {
        try {
            firebase.logEvent(event.eventName) {
                event.properties.forEach { (key, value) ->
                    when (value) {
                        is String -> param(key, value)
                        is Int -> param(key, value.toLong())
                        is Long -> param(key, value)
                        is Double -> param(key, value)
                        is Boolean -> param(key, if (value) 1L else 0L)
                        else -> param(key, value.toString())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to Firebase: ${event.eventName}", e)
        }
    }

    /**
     * Check if network is available
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }

    /**
     * Clean up old events (older than 30 days)
     */
    fun cleanupOldEvents() {
        scope.launch {
            try {
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                eventDao.deleteOldEvents(thirtyDaysAgo)
                Log.d(TAG, "Cleaned up events older than 30 days")
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up old events", e)
            }
        }
    }

    companion object {
        private const val TAG = "EventTracker"

        @Volatile
        private var INSTANCE: EventTracker? = null

        fun getInstance(context: Context): EventTracker {
            return INSTANCE ?: synchronized(this) {
                val instance = EventTracker(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Extension to convert JSONObject to Map
 */
private fun JSONObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    val keys = this.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        map[key] = this.get(key)
    }
    return map
}
