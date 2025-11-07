package com.streettycoon.game

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Professional haptic feedback manager for enhanced tactile response
 * Supports various vibration patterns and intensities
 */
class HapticFeedbackManager(private val context: Context) {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private var isEnabled = true

    enum class FeedbackType {
        // Light feedback
        TAP_LIGHT,
        TICK,

        // Medium feedback
        TAP_MEDIUM,
        BUTTON_PRESS,

        // Strong feedback
        TAP_STRONG,
        SUCCESS,
        ERROR,

        // Special patterns
        COIN_COLLECT,
        LEVEL_UP,
        ACHIEVEMENT,
        UPGRADE,
        COMBO,
        ZONE_UNLOCK,

        // Custom durations
        QUICK,
        MEDIUM,
        LONG
    }

    /**
     * Enable or disable haptic feedback
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    /**
     * Check if haptic feedback is enabled
     */
    fun isEnabled(): Boolean = isEnabled

    /**
     * Perform haptic feedback
     */
    fun performHaptic(type: FeedbackType) {
        if (!isEnabled || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (type) {
                // Light feedback (10-20ms)
                FeedbackType.TAP_LIGHT, FeedbackType.TICK ->
                    VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)

                // Medium feedback (30-50ms)
                FeedbackType.TAP_MEDIUM, FeedbackType.BUTTON_PRESS ->
                    VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)

                // Strong feedback (50-100ms)
                FeedbackType.TAP_STRONG, FeedbackType.SUCCESS ->
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)

                // Error pattern (double tap)
                FeedbackType.ERROR ->
                    VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 50), -1)

                // Coin collect (quick double tap)
                FeedbackType.COIN_COLLECT ->
                    VibrationEffect.createWaveform(longArrayOf(0, 20, 30, 20), -1)

                // Level up (ascending pattern)
                FeedbackType.LEVEL_UP ->
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 30, 50, 30, 50, 50),
                        intArrayOf(0, 100, 150, 100, 150, 200),
                        -1
                    )

                // Achievement (triumphant pattern)
                FeedbackType.ACHIEVEMENT ->
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 50, 30, 50, 30, 100),
                        intArrayOf(0, 150, 100, 150, 100, 255),
                        -1
                    )

                // Upgrade (success pattern)
                FeedbackType.UPGRADE ->
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 40, 20, 60),
                        intArrayOf(0, 120, 80, 180),
                        -1
                    )

                // Combo (rhythmic pattern)
                FeedbackType.COMBO ->
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 15, 15, 15, 15, 15),
                        intArrayOf(0, 100, 120, 100, 120, 140),
                        -1
                    )

                // Zone unlock (celebration pattern)
                FeedbackType.ZONE_UNLOCK ->
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 60, 40, 60, 40, 80),
                        intArrayOf(0, 180, 120, 180, 120, 255),
                        -1
                    )

                // Custom durations
                FeedbackType.QUICK ->
                    VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE)

                FeedbackType.MEDIUM ->
                    VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)

                FeedbackType.LONG ->
                    VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
            }

            vibrator.vibrate(effect)
        } else {
            // Fallback for older devices
            @Suppress("DEPRECATION")
            val duration = when (type) {
                FeedbackType.TAP_LIGHT, FeedbackType.TICK, FeedbackType.QUICK -> 10L
                FeedbackType.TAP_MEDIUM, FeedbackType.BUTTON_PRESS, FeedbackType.MEDIUM -> 30L
                FeedbackType.TAP_STRONG, FeedbackType.SUCCESS, FeedbackType.LONG -> 50L
                FeedbackType.ERROR -> 70L
                FeedbackType.COIN_COLLECT -> 20L
                FeedbackType.LEVEL_UP, FeedbackType.ACHIEVEMENT -> 100L
                FeedbackType.UPGRADE -> 60L
                FeedbackType.COMBO -> 25L
                FeedbackType.ZONE_UNLOCK -> 120L
            }
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    /**
     * Perform custom vibration pattern
     * @param pattern Array of durations (off, on, off, on, ...)
     * @param amplitudes Array of amplitudes (0-255) for each duration
     */
    fun performCustomPattern(pattern: LongArray, amplitudes: IntArray? = null) {
        if (!isEnabled || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (amplitudes != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect.createWaveform(pattern, amplitudes, -1)
            } else {
                VibrationEffect.createWaveform(pattern, -1)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    /**
     * Cancel ongoing vibration
     */
    fun cancel() {
        vibrator.cancel()
    }

    /**
     * Perform haptic feedback using Compose HapticFeedback
     */
    fun performComposeHaptic(hapticFeedback: HapticFeedback?, type: FeedbackType) {
        if (!isEnabled) return

        hapticFeedback?.let {
            when (type) {
                FeedbackType.TAP_LIGHT, FeedbackType.TICK ->
                    it.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                FeedbackType.TAP_MEDIUM, FeedbackType.BUTTON_PRESS,
                FeedbackType.COIN_COLLECT, FeedbackType.COMBO ->
                    it.performHapticFeedback(HapticFeedbackType.LongPress)

                else -> performHaptic(type)
            }
        } ?: performHaptic(type)
    }

    /**
     * Check if device has vibrator
     */
    fun hasVibrator(): Boolean = vibrator.hasVibrator()

    /**
     * Get vibrator capabilities (API 29+)
     */
    fun getCapabilities(): String {
        if (!vibrator.hasVibrator()) return "No vibrator"

        val capabilities = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            capabilities.add("Amplitude Control")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (vibrator.hasAmplitudeControl()) {
                capabilities.add("Advanced Amplitude")
            }
        }

        return if (capabilities.isNotEmpty()) {
            capabilities.joinToString(", ")
        } else {
            "Basic Vibration"
        }
    }
}

/**
 * Convenience extension for View haptic feedback
 */
fun View.performGameHaptic(type: HapticFeedbackManager.FeedbackType) {
    val hapticType = when (type) {
        HapticFeedbackManager.FeedbackType.TAP_LIGHT -> HapticFeedbackConstants.CLOCK_TICK
        HapticFeedbackManager.FeedbackType.BUTTON_PRESS -> HapticFeedbackConstants.KEYBOARD_TAP
        HapticFeedbackManager.FeedbackType.TAP_MEDIUM -> HapticFeedbackConstants.VIRTUAL_KEY
        HapticFeedbackManager.FeedbackType.TAP_STRONG -> HapticFeedbackConstants.LONG_PRESS
        else -> HapticFeedbackConstants.VIRTUAL_KEY
    }
    this.performHapticFeedback(hapticType)
}
