package com.streettycoon.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.streettycoon.game.HapticFeedbackManager

/**
 * Enhanced audio-haptic feedback system for premium user experience
 * Coordinates sound effects with haptic vibrations for multi-sensory feedback
 */

// ==================== FEEDBACK EFFECT PATTERNS ====================

/**
 * Combined feedback for successful actions
 */
@Composable
fun triggerSuccessFeedback(
    haptic: HapticFeedback? = null,
    hapticManager: HapticFeedbackManager? = null,
    playSound: (() -> Unit)? = null
) {
    playSound?.invoke()
    haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    hapticManager?.performHaptic(HapticFeedbackManager.FeedbackType.SUCCESS)
}

/**
 * Combined feedback for error/invalid actions
 */
@Composable
fun triggerErrorFeedback(
    haptic: HapticFeedback? = null,
    hapticManager: HapticFeedbackManager? = null,
    playSound: (() -> Unit)? = null
) {
    playSound?.invoke()
    haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    hapticManager?.performHaptic(HapticFeedbackManager.FeedbackType.ERROR)
}

/**
 * Combined feedback for money collection
 */
@Composable
fun triggerCoinCollectFeedback(
    haptic: HapticFeedback? = null,
    hapticManager: HapticFeedbackManager? = null,
    playSound: (() -> Unit)? = null
) {
    playSound?.invoke()
    hapticManager?.performHaptic(HapticFeedbackManager.FeedbackType.COIN_COLLECT)
}

/**
 * Combined feedback for level up events
 */
@Composable
fun triggerLevelUpFeedback(
    haptic: HapticFeedback? = null,
    hapticManager: HapticFeedbackManager? = null,
    playSound: (() -> Unit)? = null
) {
    playSound?.invoke()
    hapticManager?.performHaptic(HapticFeedbackManager.FeedbackType.LEVEL_UP)
}

/**
 * Combined feedback for achievement unlocked
 */
@Composable
fun triggerAchievementFeedback(
    haptic: HapticFeedback? = null,
    hapticManager: HapticFeedbackManager? = null,
    playSound: (() -> Unit)? = null
) {
    playSound?.invoke()
    hapticManager?.performHaptic(HapticFeedbackManager.FeedbackType.ACHIEVEMENT)
}

/**
 * Combined feedback for combo milestones
 */
@Composable
fun triggerComboFeedback(
    haptic: HapticFeedback? = null,
    hapticManager: HapticFeedbackManager? = null,
    playSound: (() -> Unit)? = null
) {
    playSound?.invoke()
    hapticManager?.performHaptic(HapticFeedbackManager.FeedbackType.COMBO)
}

/**
 * Combined feedback for upgrade completed
 */
@Composable
fun triggerUpgradeFeedback(
    haptic: HapticFeedback? = null,
    hapticManager: HapticFeedbackManager? = null,
    playSound: (() -> Unit)? = null
) {
    playSound?.invoke()
    hapticManager?.performHaptic(HapticFeedbackManager.FeedbackType.UPGRADE)
}

/**
 * Combined feedback for zone unlock
 */
@Composable
fun triggerZoneUnlockFeedback(
    haptic: HapticFeedback? = null,
    hapticManager: HapticFeedbackManager? = null,
    playSound: (() -> Unit)? = null
) {
    playSound?.invoke()
    hapticManager?.performHaptic(HapticFeedbackManager.FeedbackType.ZONE_UNLOCK)
}

/**
 * Light tap feedback for button interactions
 */
@Composable
fun triggerTapFeedback(
    haptic: HapticFeedback? = null,
    hapticManager: HapticFeedbackManager? = null,
    playSound: (() -> Unit)? = null
) {
    haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    hapticManager?.performHaptic(HapticFeedbackManager.FeedbackType.TAP_LIGHT)
}

/**
 * Medium button press feedback
 */
@Composable
fun triggerButtonPressFeedback(
    haptic: HapticFeedback? = null,
    hapticManager: HapticFeedbackManager? = null,
    playSound: (() -> Unit)? = null
) {
    haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    hapticManager?.performHaptic(HapticFeedbackManager.FeedbackType.BUTTON_PRESS)
}

// ==================== FEEDBACK CONTEXT HELPERS ====================

/**
 * Helper class for managing feedback state and coordination
 */
class FeedbackCoordinator(
    val hapticManager: HapticFeedbackManager? = null,
    val soundPlayerFn: ((String) -> Unit)? = null
) {
    /**
     * Trigger coordinated feedback with custom parameters
     */
    fun triggerFeedback(
        hapticType: HapticFeedbackManager.FeedbackType,
        soundName: String? = null,
        delayMs: Long = 0L
    ) {
        if (delayMs > 0) {
            // Delay feedback for synchronization with animations
            try {
                Thread.sleep(delayMs)
            } catch (e: InterruptedException) {
                // Ignore
            }
        }

        soundPlayerFn?.invoke(soundName ?: "")
        hapticManager?.performHaptic(hapticType)
    }

    /**
     * Trigger multiple feedback pulses in sequence
     */
    fun triggerPulsedFeedback(
        hapticType: HapticFeedbackManager.FeedbackType,
        pulseCount: Int = 3,
        intervalMs: Long = 100L
    ) {
        repeat(pulseCount) {
            hapticManager?.performHaptic(hapticType)
            if (it < pulseCount - 1) {
                try {
                    Thread.sleep(intervalMs)
                } catch (e: InterruptedException) {
                    // Ignore
                }
            }
        }
    }
}

// ==================== AUDIO-VISUAL SYNCHRONIZATION ====================

/**
 * Manager for synchronizing audio cues with visual animations
 */
class AudioVisualSyncManager(
    private val feedbackCoordinator: FeedbackCoordinator
) {
    /**
     * Trigger synchronized feedback for explosion/impact effects
     */
    fun triggerImpactFeedback(intensity: Float = 1f) {
        val hapticType = when {
            intensity > 0.75f -> HapticFeedbackManager.FeedbackType.TAP_STRONG
            intensity > 0.5f -> HapticFeedbackManager.FeedbackType.TAP_MEDIUM
            else -> HapticFeedbackManager.FeedbackType.TAP_LIGHT
        }

        feedbackCoordinator.triggerFeedback(
            hapticType = hapticType,
            soundName = "impact"
        )
    }

    /**
     * Trigger synchronized feedback for rewards
     */
    fun triggerRewardFeedback(rewardType: String) {
        val hapticType = when (rewardType) {
            "coin" -> HapticFeedbackManager.FeedbackType.COIN_COLLECT
            "level" -> HapticFeedbackManager.FeedbackType.LEVEL_UP
            "achievement" -> HapticFeedbackManager.FeedbackType.ACHIEVEMENT
            "combo" -> HapticFeedbackManager.FeedbackType.COMBO
            else -> HapticFeedbackManager.FeedbackType.SUCCESS
        }

        feedbackCoordinator.triggerFeedback(
            hapticType = hapticType,
            soundName = "reward_${rewardType}"
        )
    }

    /**
     * Trigger synchronized feedback for progression
     */
    fun triggerProgressionFeedback(progressionType: String) {
        val hapticType = when (progressionType) {
            "upgrade" -> HapticFeedbackManager.FeedbackType.UPGRADE
            "unlock" -> HapticFeedbackManager.FeedbackType.ZONE_UNLOCK
            else -> HapticFeedbackManager.FeedbackType.SUCCESS
        }

        feedbackCoordinator.triggerFeedback(
            hapticType = hapticType,
            soundName = "progression_${progressionType}"
        )
    }
}

// ==================== FEEDBACK INTENSITY LEVELS ====================

/**
 * Intensity levels for user preference
 */
enum class FeedbackIntensity {
    OFF,        // No feedback
    LIGHT,      // Minimal feedback
    NORMAL,     // Default feedback
    STRONG,     // Enhanced feedback
    MAXIMUM     // Maximum feedback intensity
}

/**
 * Manager for user-configurable feedback intensity
 */
class FeedbackIntensityManager(
    private var intensity: FeedbackIntensity = FeedbackIntensity.NORMAL,
    private val hapticManager: HapticFeedbackManager? = null
) {
    fun setIntensity(level: FeedbackIntensity) {
        intensity = level
    }

    fun getIntensity(): FeedbackIntensity = intensity

    /**
     * Scale haptic feedback based on intensity level
     */
    fun performScaledHaptic(
        baseType: HapticFeedbackManager.FeedbackType,
        intenseType: HapticFeedbackManager.FeedbackType = HapticFeedbackManager.FeedbackType.TAP_STRONG,
        lightType: HapticFeedbackManager.FeedbackType = HapticFeedbackManager.FeedbackType.TAP_LIGHT
    ) {
        val feedbackType = when (intensity) {
            FeedbackIntensity.OFF -> return
            FeedbackIntensity.LIGHT -> lightType
            FeedbackIntensity.NORMAL -> baseType
            FeedbackIntensity.STRONG -> intenseType
            FeedbackIntensity.MAXIMUM -> intenseType
        }

        hapticManager?.performHaptic(feedbackType)
    }
}

// ==================== HAPTIC EFFECT HELPER EXTENSION ====================

/**
 * Extension function for composables to easily trigger feedback
 */
@Composable
fun triggerMultisensoryFeedback(
    hapticType: HapticFeedbackManager.FeedbackType,
    soundName: String? = null,
    feedbackCoordinator: FeedbackCoordinator? = null
) {
    val haptic = LocalHapticFeedback.current
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

    feedbackCoordinator?.triggerFeedback(
        hapticType = hapticType,
        soundName = soundName
    )
}
