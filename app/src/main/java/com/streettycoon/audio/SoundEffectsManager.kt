package com.streettycoon.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    enum class SoundEffect(val displayName: String, val resourceId: Int) {
        // Placeholder resource IDs - these will need actual audio files
        TAP_SERVE("Tap Serve", android.R.raw.test_audio),
        COIN_COLLECT("Coin Collect", android.R.raw.test_audio),
        UPGRADE("Upgrade", android.R.raw.test_audio),
        UNLOCK("Unlock", android.R.raw.test_audio),
        PURCHASE("Purchase", android.R.raw.test_audio),
        LEVEL_UP("Level Up", android.R.raw.test_audio),
        ERROR("Error", android.R.raw.test_audio);
    }

    /**
     * Initialize the sound pool and load all sounds
     */
    fun initialize() {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)  // Allow up to 8 simultaneous sounds
            .setAudioAttributes(audioAttributes)
            .build()

        // Load all sound effects
        SoundEffect.values().forEach { effect ->
            soundPool?.let { pool ->
                try {
                    val soundId = pool.load(context, effect.resourceId, 1)
                    soundIds[effect] = soundId
                } catch (e: Exception) {
                    // Log error but don't crash - sound effects are non-critical
                    android.util.Log.e("SoundEffectsManager", "Failed to load sound: ${effect.displayName}", e)
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
