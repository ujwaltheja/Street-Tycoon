package com.streettycoon.audio

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Unified audio manager that coordinates music and sound effects
 * Manages global audio settings and preferences
 */
class AudioManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val musicManager = MusicManager(appContext)
    val soundEffects = SoundEffectsManager(appContext)

    private val _isMusicEnabled = MutableStateFlow(prefs.getBoolean(KEY_MUSIC_ENABLED, true))
    val isMusicEnabled: StateFlow<Boolean> = _isMusicEnabled.asStateFlow()

    private val _isSoundEffectsEnabled = MutableStateFlow(prefs.getBoolean(KEY_SFX_ENABLED, true))
    val isSoundEffectsEnabled: StateFlow<Boolean> = _isSoundEffectsEnabled.asStateFlow()

    private val _musicVolume = MutableStateFlow(prefs.getFloat(KEY_MUSIC_VOLUME, 0.7f))
    val musicVolume: StateFlow<Float> = _musicVolume.asStateFlow()

    private val _soundEffectsVolume = MutableStateFlow(prefs.getFloat(KEY_SFX_VOLUME, 0.8f))
    val soundEffectsVolume: StateFlow<Float> = _soundEffectsVolume.asStateFlow()

    companion object {
        private const val PREFS_NAME = "audio_prefs"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_SFX_ENABLED = "sfx_enabled"
        private const val KEY_MUSIC_VOLUME = "music_volume"
        private const val KEY_SFX_VOLUME = "sfx_volume"

        @Volatile
        private var instance: AudioManager? = null

        fun getInstance(context: Context): AudioManager {
            return instance ?: synchronized(this) {
                instance ?: AudioManager(context).also { instance = it }
            }
        }
    }

    init {
        // Initialize managers with saved settings
        musicManager.setVolume(_musicVolume.value)
        soundEffects.setVolume(_soundEffectsVolume.value)

        if (_isMusicEnabled.value) {
            musicManager.unmute()
        } else {
            musicManager.mute()
        }

        if (_isSoundEffectsEnabled.value) {
            soundEffects.unmute()
        } else {
            soundEffects.mute()
        }
    }

    /**
     * Initialize audio systems
     */
    fun initialize() {
        musicManager.initialize()
        soundEffects.initialize()
    }

    /**
     * Start background music
     */
    fun startMusic(track: MusicManager.MusicTrack = MusicManager.MusicTrack.getDefault()) {
        if (_isMusicEnabled.value) {
            musicManager.play(track)
        }
    }

    /**
     * Stop background music
     */
    fun stopMusic() {
        musicManager.stop()
    }

    /**
     * Pause background music
     */
    fun pauseMusic() {
        musicManager.pause()
    }

    /**
     * Resume background music
     */
    fun resumeMusic() {
        if (_isMusicEnabled.value) {
            musicManager.resume()
        }
    }

    /**
     * Enable music
     */
    fun enableMusic() {
        _isMusicEnabled.value = true
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, true).apply()
        musicManager.unmute()
    }

    /**
     * Disable music
     */
    fun disableMusic() {
        _isMusicEnabled.value = false
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, false).apply()
        musicManager.mute()
    }

    /**
     * Toggle music on/off
     */
    fun toggleMusic() {
        if (_isMusicEnabled.value) {
            disableMusic()
        } else {
            enableMusic()
        }
    }

    /**
     * Enable sound effects
     */
    fun enableSoundEffects() {
        _isSoundEffectsEnabled.value = true
        prefs.edit().putBoolean(KEY_SFX_ENABLED, true).apply()
        soundEffects.unmute()
    }

    /**
     * Disable sound effects
     */
    fun disableSoundEffects() {
        _isSoundEffectsEnabled.value = false
        prefs.edit().putBoolean(KEY_SFX_ENABLED, false).apply()
        soundEffects.mute()
    }

    /**
     * Toggle sound effects on/off
     */
    fun toggleSoundEffects() {
        if (_isSoundEffectsEnabled.value) {
            disableSoundEffects()
        } else {
            enableSoundEffects()
        }
    }

    /**
     * Set music volume (0.0 to 1.0)
     */
    fun setMusicVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        _musicVolume.value = clampedVolume
        prefs.edit().putFloat(KEY_MUSIC_VOLUME, clampedVolume).apply()
        musicManager.setVolume(clampedVolume)
    }

    /**
     * Set sound effects volume (0.0 to 1.0)
     */
    fun setSoundEffectsVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        _soundEffectsVolume.value = clampedVolume
        prefs.edit().putFloat(KEY_SFX_VOLUME, clampedVolume).apply()
        soundEffects.setVolume(clampedVolume)
    }

    /**
     * Play a sound effect
     */
    fun playSoundEffect(effect: SoundEffectsManager.SoundEffect) {
        if (_isSoundEffectsEnabled.value) {
            soundEffects.play(effect)
        }
    }

    /**
     * Convenience methods for common sound effects
     */
    fun playTapServe() = playSoundEffect(SoundEffectsManager.SoundEffect.TAP_SERVE)
    fun playCoinCollect() = playSoundEffect(SoundEffectsManager.SoundEffect.COIN_COLLECT)
    fun playUpgrade() = playSoundEffect(SoundEffectsManager.SoundEffect.UPGRADE)
    fun playUnlock() = playSoundEffect(SoundEffectsManager.SoundEffect.UNLOCK)
    fun playPurchase() = playSoundEffect(SoundEffectsManager.SoundEffect.PURCHASE)
    fun playLevelUp() = playSoundEffect(SoundEffectsManager.SoundEffect.LEVEL_UP)
    fun playError() = playSoundEffect(SoundEffectsManager.SoundEffect.ERROR)

    /**
     * Pause all audio (for when app goes to background)
     */
    fun pauseAll() {
        musicManager.pause()
        soundEffects.stopAll()
    }

    /**
     * Resume all audio (for when app comes to foreground)
     */
    fun resumeAll() {
        if (_isMusicEnabled.value) {
            musicManager.resume()
        }
        if (_isSoundEffectsEnabled.value) {
            soundEffects.resumeAll()
        }
    }

    /**
     * Mute all audio
     */
    fun muteAll() {
        musicManager.mute()
        soundEffects.mute()
    }

    /**
     * Unmute all audio
     */
    fun unmuteAll() {
        if (_isMusicEnabled.value) {
            musicManager.unmute()
        }
        if (_isSoundEffectsEnabled.value) {
            soundEffects.unmute()
        }
    }

    /**
     * Release all audio resources
     */
    fun release() {
        musicManager.release()
        soundEffects.release()
    }
}
