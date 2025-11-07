package com.streettycoon.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manager for sound effects using Android SoundPool
 * Handles short audio clips for game actions
 */
class SoundEffectsManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<SoundEffect, Int>()
    private var isInitialized = false

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    /**
     * Available sound effects
     */
    enum class SoundEffect(val displayName: String, @RawRes val resourceId: Int) {
        TAP_SERVE("Tap Serve", com.streettycoon.R.raw.sfx_tap_serve),
        COIN_COLLECT("Coin Collect", com.streettycoon.R.raw.sfx_coin_collect),
        UPGRADE("Upgrade", com.streettycoon.R.raw.sfx_upgrade_success),
        UNLOCK("Unlock", com.streettycoon.R.raw.sfx_zone_unlock),
        PURCHASE("Purchase", com.streettycoon.R.raw.sfx_button_click),
        LEVEL_UP("Level Up", com.streettycoon.R.raw.sfx_level_up),
        ERROR("Error", com.streettycoon.R.raw.sfx_error);
    }

    /**
     * Initialize the sound pool and load all sounds
     * Loads sounds asynchronously on background thread to prevent ANR
     */
    fun initialize() {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(7)  // Match the number of sound effects
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                android.util.Log.d("SoundEffectsManager", "Sound loaded successfully: $sampleId")
            } else {
                android.util.Log.e("SoundEffectsManager", "Failed to load sound: $sampleId with status $status")
            }
        }

        // Load all sound effects asynchronously on IO thread to prevent ANR
        CoroutineScope(Dispatchers.IO).launch {
            SoundEffect.values().forEach { effect ->
                soundPool?.let { pool ->
                    try {
                        // Skip if resource ID is not set (0 means no resource)
                        if (effect.resourceId == 0) {
                            android.util.Log.w("SoundEffectsManager", "Sound effect ${effect.displayName} has no resource - skipping")
                        } else {
                            val soundId = pool.load(context, effect.resourceId, 1)
                            soundIds[effect] = soundId
                            android.util.Log.d("SoundEffectsManager", "Loading sound: ${effect.displayName} (ID: $soundId)")
                        }
                    } catch (e: Exception) {
                        // Log error but don't crash - sound effects are non-critical
                        android.util.Log.e("SoundEffectsManager", "Failed to load sound: ${effect.displayName}", e)
                    }
                }
            }
        }

        isInitialized = true
    }

    /**
     * Play a sound effect
     */
    fun play(effect: SoundEffect, loop: Boolean = false) {
        if (!isInitialized) {
            initialize()
        }

        if (_isMuted.value) return

        val soundId = soundIds[effect] ?: return
        val loopMode = if (loop) -1 else 0  // -1 = infinite loop, 0 = no loop

        soundPool?.play(
            soundId,
            _volume.value,  // left volume
            _volume.value,  // right volume
            1,              // priority
            loopMode,       // loop
            1.0f            // playback rate
        )
    }

    /**
     * Play tap serve sound (most common)
     */
    fun playTapServe() = play(SoundEffect.TAP_SERVE)

    /**
     * Play coin collect sound
     */
    fun playCoinCollect() = play(SoundEffect.COIN_COLLECT)

    /**
     * Play upgrade sound
     */
    fun playUpgrade() = play(SoundEffect.UPGRADE)

    /**
     * Play unlock sound
     */
    fun playUnlock() = play(SoundEffect.UNLOCK)

    /**
     * Play purchase sound
     */
    fun playPurchase() = play(SoundEffect.PURCHASE)

    /**
     * Play level up sound
     */
    fun playLevelUp() = play(SoundEffect.LEVEL_UP)

    /**
     * Play error sound
     */
    fun playError() = play(SoundEffect.ERROR)

    /**
     * Set sound effects volume (0.0 to 1.0)
     */
    fun setVolume(volume: Float) {
        _volume.value = volume.coerceIn(0f, 1f)
    }

    /**
     * Mute sound effects
     */
    fun mute() {
        _isMuted.value = true
    }

    /**
     * Unmute sound effects
     */
    fun unmute() {
        _isMuted.value = false
    }

    /**
     * Toggle mute state
     */
    fun toggleMute() {
        if (_isMuted.value) {
            unmute()
        } else {
            mute()
        }
    }

    /**
     * Stop all currently playing sounds
     */
    fun stopAll() {
        soundPool?.autoPause()
    }

    /**
     * Resume all paused sounds
     */
    fun resumeAll() {
        soundPool?.autoResume()
    }

    /**
     * Release sound pool resources
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        isInitialized = false
    }
}
