package com.streettycoon.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.MediaPlayer
import android.util.Log
import java.io.IOException

/**
 * Manages all game sounds and music
 * Uses SoundPool for short sound effects and MediaPlayer for background music
 */
class SoundManager(private val context: Context) {
    private lateinit var soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()
    private var backgroundMusic: MediaPlayer? = null
    private var isMusicEnabled = true
    private var isSoundsEnabled = true
    private var backgroundMusicVolume = 0.3f
    private var soundEffectsVolume = 0.5f

    init {
        try {
            initializeSoundPool()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SoundPool", e)
            // Don't throw - allow the app to continue without sound effects
        }
    }

    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { soundPool, soundId, status ->
            if (status == 0) {
                Log.d(TAG, "Sound loaded successfully: $soundId")
            } else {
                Log.e(TAG, "Failed to load sound: $soundId")
            }
        }
    }

    /**
     * Play a registered sound effect
     */
    fun playSound(soundName: String) {
        if (!isSoundsEnabled) return
        if (!::soundPool.isInitialized) {
            Log.e(TAG, "SoundPool not initialized")
            return
        }

        try {
            val soundId = soundMap[soundName]
            if (soundId != null) {
                soundPool.play(
                    soundId,
                    soundEffectsVolume,
                    soundEffectsVolume,
                    1,
                    0,
                    1f
                )
            } else {
                Log.w(TAG, "Sound not found: $soundName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: ${e.message}")
        }
    }

    /**
     * Load a sound from raw resources
     */
    fun loadSound(soundName: String, resourceId: Int) {
        if (!::soundPool.isInitialized) {
            Log.e(TAG, "SoundPool not initialized, cannot load sound")
            return
        }
        try {
            val soundId = soundPool.load(context, resourceId, 1)
            soundMap[soundName] = soundId
            Log.d(TAG, "Sound loaded: $soundName (ID: $soundId)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load sound $soundName: ${e.message}")
        }
    }

    /**
     * Play tap/click sound using system sound
     */
    fun playTapSound() {
        if (!isSoundsEnabled) return
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager == null) {
                Log.e(TAG, "AudioManager service not available")
                return
            }
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, soundEffectsVolume)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing tap sound: ${e.message}")
        }
    }

    /**
     * Play success/positive sound using system sound
     */
    fun playSuccessSound() {
        if (!isSoundsEnabled) return
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager == null) {
                Log.e(TAG, "AudioManager service not available")
                return
            }
            audioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP, soundEffectsVolume)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing success sound: ${e.message}")
        }
    }

    /**
     * Play error/negative sound using system sound
     */
    fun playErrorSound() {
        if (!isSoundsEnabled) return
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager == null) {
                Log.e(TAG, "AudioManager service not available")
                return
            }
            audioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN, soundEffectsVolume)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing error sound: ${e.message}")
        }
    }

    /**
     * Start background music
     */
    fun playBackgroundMusic(resourceId: Int) {
        if (!isMusicEnabled) return

        try {
            stopBackgroundMusic()

            backgroundMusic = MediaPlayer.create(context, resourceId).apply {
                isLooping = true
                setVolume(backgroundMusicVolume, backgroundMusicVolume)
                start()
            }
            Log.d(TAG, "Background music started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play background music: ${e.message}")
        }
    }

    /**
     * Resume background music
     */
    fun resumeBackgroundMusic() {
        try {
            backgroundMusic?.apply {
                if (!isPlaying) {
                    start()
                    Log.d(TAG, "Background music resumed")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming music: ${e.message}")
        }
    }

    /**
     * Pause background music
     */
    fun pauseBackgroundMusic() {
        try {
            backgroundMusic?.apply {
                if (isPlaying) {
                    pause()
                    Log.d(TAG, "Background music paused")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing music: ${e.message}")
        }
    }

    /**
     * Stop background music
     */
    fun stopBackgroundMusic() {
        try {
            backgroundMusic?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            backgroundMusic = null
            Log.d(TAG, "Background music stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping music: ${e.message}")
        }
    }

    /**
     * Set music volume (0f to 1f)
     */
    fun setMusicVolume(volume: Float) {
        backgroundMusicVolume = volume.coerceIn(0f, 1f)
        backgroundMusic?.setVolume(backgroundMusicVolume, backgroundMusicVolume)
    }

    /**
     * Set sound effects volume (0f to 1f)
     */
    fun setSoundEffectsVolume(volume: Float) {
        soundEffectsVolume = volume.coerceIn(0f, 1f)
    }

    /**
     * Enable/disable music
     */
    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (!enabled) {
            pauseBackgroundMusic()
        } else {
            resumeBackgroundMusic()
        }
    }

    /**
     * Enable/disable sound effects
     */
    fun setSoundsEnabled(enabled: Boolean) {
        isSoundsEnabled = enabled
    }

    /**
     * Check if music is playing
     */
    fun isMusicPlaying(): Boolean = backgroundMusic?.isPlaying == true

    /**
     * Release resources
     */
    fun release() {
        try {
            stopBackgroundMusic()
            if (::soundPool.isInitialized) {
                soundPool.release()
            }
            Log.d(TAG, "SoundManager released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing SoundManager: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "SoundManager"
        @Volatile
        private var instance: SoundManager? = null

        /**
         * Get or create SoundManager singleton (thread-safe)
         */
        fun getInstance(context: Context): SoundManager {
            // Double-checked locking pattern for thread safety
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
