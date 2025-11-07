package com.streettycoon.services

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.AchievementsClient
import com.google.android.gms.games.LeaderboardsClient
import com.google.android.gms.games.SnapshotsClient
import kotlinx.coroutines.tasks.await

/**
 * Google Play Games Services Manager
 * Handles achievements, leaderboards, and cloud saves
 */
class PlayGamesManager(private val context: Context) {

    companion object {
        private const val TAG = "PlayGamesManager"

        // Achievement IDs (replace with your actual IDs from Play Console)
        const val ACHIEVEMENT_FIRST_SALE = "CgkI_test_achievement_1"
        const val ACHIEVEMENT_100_SALES = "CgkI_test_achievement_2"
        const val ACHIEVEMENT_HIRE_FIRST_HELPER = "CgkI_test_achievement_3"
        const val ACHIEVEMENT_UPGRADE_STALL = "CgkI_test_achievement_4"
        const val ACHIEVEMENT_UNLOCK_ALL_ZONES = "CgkI_test_achievement_5"
        const val ACHIEVEMENT_EARN_1_LAKH = "CgkI_test_achievement_6"
        const val ACHIEVEMENT_EARN_10_LAKH = "CgkI_test_achievement_7"
        const val ACHIEVEMENT_COMPLETE_FAMILY = "CgkI_test_achievement_8"
        const val ACHIEVEMENT_MASTER_TYCOON = "CgkI_test_achievement_9"

        // Leaderboard IDs (replace with your actual IDs from Play Console)
        const val LEADERBOARD_TOTAL_EARNINGS = "CgkI_test_leaderboard_1"
        const val LEADERBOARD_HIGHEST_LEVEL = "CgkI_test_leaderboard_2"
        const val LEADERBOARD_CUSTOMERS_SERVED = "CgkI_test_leaderboard_3"
    }

    private var isInitialized = false
    private var isSignedIn = false

    private val achievementsClient: AchievementsClient
        get() = PlayGames.getAchievementsClient(context)

    private val leaderboardsClient: LeaderboardsClient
        get() = PlayGames.getLeaderboardsClient(context)

    private val snapshotsClient: SnapshotsClient
        get() = PlayGames.getSnapshotsClient(context)

    /**
     * Initialize Play Games SDK
     */
    fun initialize() {
        if (isInitialized) return

        try {
            PlayGamesSdk.initialize(context)
            isInitialized = true
            Log.d(TAG, "Play Games SDK initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Play Games SDK", e)
        }
    }

    /**
     * Sign in to Play Games
     */
    suspend fun signIn(activity: Activity): Boolean {
        if (!isInitialized) {
            initialize()
        }

        return try {
            val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
            val signInResult = gamesSignInClient.isAuthenticated().await()

            isSignedIn = if (signInResult.isAuthenticated) {
                Log.d(TAG, "User is already signed in")
                true
            } else {
                // Try to sign in silently
                val result = gamesSignInClient.signIn().await()
                result.isAuthenticated.also { authenticated ->
                    if (authenticated) {
                        Log.d(TAG, "User signed in successfully")
                    } else {
                        Log.w(TAG, "User declined to sign in")
                    }
                }
            }

            isSignedIn
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed", e)
            false
        }
    }

    /**
     * Sign out from Play Games
     */
    suspend fun signOut(activity: Activity) {
        try {
            PlayGames.getGamesSignInClient(activity).signOut().await()
            isSignedIn = false
            Log.d(TAG, "User signed out")
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
        }
    }

    /**
     * Check if user is signed in
     */
    fun isSignedIn(): Boolean = isSignedIn

    // ==================== ACHIEVEMENTS ====================

    /**
     * Unlock an achievement
     */
    fun unlockAchievement(achievementId: String) {
        if (!isSignedIn) {
            Log.w(TAG, "Cannot unlock achievement: user not signed in")
            return
        }

        try {
            achievementsClient.unlock(achievementId)
            Log.d(TAG, "Achievement unlocked: $achievementId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unlock achievement", e)
        }
    }

    /**
     * Increment an incremental achievement
     */
    fun incrementAchievement(achievementId: String, numSteps: Int = 1) {
        if (!isSignedIn) return

        try {
            achievementsClient.increment(achievementId, numSteps)
            Log.d(TAG, "Achievement incremented: $achievementId by $numSteps")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to increment achievement", e)
        }
    }

    /**
     * Show achievements UI
     */
    suspend fun showAchievements(activity: Activity) {
        if (!isSignedIn) {
            Log.w(TAG, "Cannot show achievements: user not signed in")
            return
        }

        try {
            val intent = achievementsClient.achievementsIntent.await()
            activity.startActivityForResult(intent, 9001)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show achievements", e)
        }
    }

    // ==================== LEADERBOARDS ====================

    /**
     * Submit score to leaderboard
     */
    fun submitScore(leaderboardId: String, score: Long) {
        if (!isSignedIn) {
            Log.w(TAG, "Cannot submit score: user not signed in")
            return
        }

        try {
            leaderboardsClient.submitScore(leaderboardId, score)
            Log.d(TAG, "Score submitted: $score to $leaderboardId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit score", e)
        }
    }

    /**
     * Show leaderboard UI
     */
    suspend fun showLeaderboard(activity: Activity, leaderboardId: String) {
        if (!isSignedIn) {
            Log.w(TAG, "Cannot show leaderboard: user not signed in")
            return
        }

        try {
            val intent = leaderboardsClient.getLeaderboardIntent(leaderboardId).await()
            activity.startActivityForResult(intent, 9002)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show leaderboard", e)
        }
    }

    /**
     * Show all leaderboards UI
     */
    suspend fun showAllLeaderboards(activity: Activity) {
        if (!isSignedIn) {
            Log.w(TAG, "Cannot show leaderboards: user not signed in")
            return
        }

        try {
            val intent = leaderboardsClient.allLeaderboardsIntent.await()
            activity.startActivityForResult(intent, 9002)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show leaderboards", e)
        }
    }

    // ==================== CLOUD SAVES ====================

    /**
     * Save game state to cloud
     */
    suspend fun saveToCloud(saveName: String, data: ByteArray): Boolean {
        if (!isSignedIn) {
            Log.w(TAG, "Cannot save to cloud: user not signed in")
            return false
        }

        return try {
            val snapshotsClient = snapshotsClient
            val conflictResolutionPolicy = SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED

            // Open snapshot
            val snapshotResult = snapshotsClient.open(saveName, true, conflictResolutionPolicy).await()
            val snapshot = snapshotResult.data

            if (snapshot != null) {
                // Write data
                snapshot.snapshotContents.writeBytes(data)

                // Create metadata
                val metadata = snapshot.metadata.toBuilder()
                    .setDescription("Street Tycoon Save - ${System.currentTimeMillis()}")
                    .setPlayedTimeMillis(System.currentTimeMillis())
                    .build()

                // Commit
                snapshotsClient.commitAndClose(snapshot, metadata).await()
                Log.d(TAG, "Game saved to cloud: $saveName (${data.size} bytes)")
                true
            } else {
                Log.e(TAG, "Failed to open snapshot for writing")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save to cloud", e)
            false
        }
    }

    /**
     * Load game state from cloud
     */
    suspend fun loadFromCloud(saveName: String): ByteArray? {
        if (!isSignedIn) {
            Log.w(TAG, "Cannot load from cloud: user not signed in")
            return null
        }

        return try {
            val snapshotsClient = snapshotsClient
            val conflictResolutionPolicy = SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED

            // Open snapshot
            val snapshotResult = snapshotsClient.open(saveName, true, conflictResolutionPolicy).await()
            val snapshot = snapshotResult.data

            if (snapshot != null) {
                val data = snapshot.snapshotContents.readFully().await()
                // Close snapshot
                snapshotsClient.discardAndClose(snapshot)
                Log.d(TAG, "Game loaded from cloud: $saveName (${data.size} bytes)")
                data
            } else {
                Log.w(TAG, "No cloud save found: $saveName")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load from cloud", e)
            null
        }
    }

    // ==================== GAME-SPECIFIC ACHIEVEMENTS ====================

    /**
     * Track game events and unlock achievements
     */
    object AchievementTracker {
        fun onFirstSale(manager: PlayGamesManager) {
            manager.unlockAchievement(ACHIEVEMENT_FIRST_SALE)
        }

        fun onCustomerServed(manager: PlayGamesManager, totalServed: Int) {
            if (totalServed >= 100) {
                manager.unlockAchievement(ACHIEVEMENT_100_SALES)
            }
        }

        fun onHelperHired(manager: PlayGamesManager, helperCount: Int) {
            if (helperCount == 1) {
                manager.unlockAchievement(ACHIEVEMENT_HIRE_FIRST_HELPER)
            }
        }

        fun onStallUpgraded(manager: PlayGamesManager) {
            manager.unlockAchievement(ACHIEVEMENT_UPGRADE_STALL)
        }

        fun onAllZonesUnlocked(manager: PlayGamesManager) {
            manager.unlockAchievement(ACHIEVEMENT_UNLOCK_ALL_ZONES)
        }

        fun onMoneyEarned(manager: PlayGamesManager, totalMoney: Long) {
            when {
                totalMoney >= 100_000 -> manager.unlockAchievement(ACHIEVEMENT_EARN_1_LAKH)
                totalMoney >= 1_000_000 -> manager.unlockAchievement(ACHIEVEMENT_EARN_10_LAKH)
            }

            // Update leaderboard
            manager.submitScore(LEADERBOARD_TOTAL_EARNINGS, totalMoney)
        }

        fun onFamilyComplete(manager: PlayGamesManager) {
            manager.unlockAchievement(ACHIEVEMENT_COMPLETE_FAMILY)
        }

        fun onMasterTycoon(manager: PlayGamesManager) {
            manager.unlockAchievement(ACHIEVEMENT_MASTER_TYCOON)
        }
    }
}
