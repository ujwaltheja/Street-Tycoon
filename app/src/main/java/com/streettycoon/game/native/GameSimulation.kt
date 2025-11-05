package com.streettycoon.game.native

import android.util.Log
import com.google.gson.Gson
import com.streettycoon.game.model.ActionResult
import com.streettycoon.game.model.GameState

/**
 * JNI wrapper for native C++ GameSimulation
 * Provides Kotlin-friendly interface to the deterministic game simulation
 */
class GameSimulation {

    private var nativeHandle: Long = 0
    private val gson = Gson()

    init {
        System.loadLibrary("streettycoon")
        nativeHandle = nativeCreate()
        Log.d(TAG, "GameSimulation created with handle: $nativeHandle")
    }

    /**
     * Initialize a new game with default state
     */
    fun initializeNewGame() {
        nativeInitializeNewGame(nativeHandle)
        Log.d(TAG, "New game initialized")
    }

    /**
     * Initialize game from JSON snapshot
     */
    fun initializeFromJson(json: String): Boolean {
        return nativeInitializeFromJson(nativeHandle, json)
    }

    /**
     * Initialize game from GameState object
     */
    fun initializeFromState(state: GameState): Boolean {
        val json = gson.toJson(state)
        return initializeFromJson(json)
    }

    /**
     * Tick the simulation forward by deltaTime milliseconds
     */
    fun tick(deltaTimeMs: Long) {
        nativeTick(nativeHandle, deltaTimeMs)
    }

    /**
     * Get current game state as JSON string
     */
    fun getSnapshotJson(): String {
        return nativeGetSnapshot(nativeHandle)
    }

    /**
     * Get current game state as GameState object
     */
    fun getSnapshot(): GameState {
        val json = getSnapshotJson()
        return gson.fromJson(json, GameState::class.java)
    }

    /**
     * Apply an action to the simulation
     * @param actionType Type of action (tap_serve, upgrade_stall, etc.)
     * @param params Action parameters (stallId, zoneId, etc.)
     */
    fun applyAction(actionType: String, params: Map<String, Any> = emptyMap()): ActionResult {
        val actionMap = mutableMapOf<String, Any>("action" to actionType)
        actionMap.putAll(params)

        val actionJson = gson.toJson(actionMap)
        val resultJson = nativeApplyAction(nativeHandle, actionJson)

        return gson.fromJson(resultJson, ActionResult::class.java)
    }

    /**
     * Tap to serve a customer at a stall
     */
    fun tapServe(stallId: Int): ActionResult {
        return applyAction("tap_serve", mapOf("stallId" to stallId))
    }

    /**
     * Upgrade a stall to the next level
     */
    fun upgradeStall(stallId: Int): ActionResult {
        return applyAction("upgrade_stall", mapOf("stallId" to stallId))
    }

    /**
     * Hire a helper for a stall
     */
    fun hireHelper(stallId: Int): ActionResult {
        return applyAction("hire_helper", mapOf("stallId" to stallId))
    }

    /**
     * Unlock a stall
     */
    fun unlockStall(stallId: Int): ActionResult {
        return applyAction("unlock_stall", mapOf("stallId" to stallId))
    }

    /**
     * Unlock a zone
     */
    fun unlockZone(zoneId: Int): ActionResult {
        return applyAction("unlock_zone", mapOf("zoneId" to zoneId))
    }

    /**
     * Claim daily reward
     */
    fun claimDailyReward(): ActionResult {
        return applyAction("claim_daily_reward")
    }

    /**
     * Apply offline earnings
     */
    fun applyOfflineEarnings(offlineTimeMs: Long): ActionResult {
        return applyAction("apply_offline_earnings", mapOf("offlineTimeMs" to offlineTimeMs))
    }

    /**
     * Calculate offline earnings without applying them
     */
    fun calculateOfflineEarnings(offlineTimeMs: Long): Double {
        return nativeCalculateOfflineEarnings(nativeHandle, offlineTimeMs)
    }

    /**
     * Clean up native resources
     */
    fun destroy() {
        if (nativeHandle != 0L) {
            nativeDestroy(nativeHandle)
            nativeHandle = 0
            Log.d(TAG, "GameSimulation destroyed")
        }
    }

    protected fun finalize() {
        destroy()
    }

    // Native methods
    private external fun nativeCreate(): Long
    private external fun nativeDestroy(handle: Long)
    private external fun nativeInitializeNewGame(handle: Long)
    private external fun nativeInitializeFromJson(handle: Long, json: String): Boolean
    private external fun nativeTick(handle: Long, deltaTimeMs: Long)
    private external fun nativeGetSnapshot(handle: Long): String
    private external fun nativeApplyAction(handle: Long, actionJson: String): String
    private external fun nativeCalculateOfflineEarnings(handle: Long, offlineTimeMs: Long): Double

    companion object {
        private const val TAG = "GameSimulation"
    }
}
