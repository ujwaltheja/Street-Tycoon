package com.streettycoon.audio

import android.content.Context
import android.net.Uri
import androidx.annotation.RawRes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manager for background music playback using Media3 ExoPlayer
 * Handles looping, volume control, and track management
 */
class MusicManager(private val context: Context) {

    private var player: ExoPlayer? = null
    private var currentTrack: MusicTrack? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    /**
     * Available music tracks
     */
    enum class MusicTrack(val displayName: String, val resourceId: Int) {
        // Placeholder resource IDs - these will need actual audio files
        MARKETPLACE("Marketplace Bustle", android.R.raw.test_audio),
        PEACEFUL("Peaceful Streets", android.R.raw.test_audio);

        companion object {
            fun getDefault() = MARKETPLACE
        }
    }

    /**
     * Initialize the music player
     */
    fun initialize() {
        if (player == null) {
            player = ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = _volume.value

                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }
                })
            }
        }
    }

    /**
     * Play a music track
     */
    fun play(track: MusicTrack = MusicTrack.getDefault()) {
        initialize()

        if (currentTrack != track) {
            currentTrack = track
            val uri = Uri.parse("android.resource://${context.packageName}/${track.resourceId}")
            val mediaItem = MediaItem.fromUri(uri)

            player?.apply {
                setMediaItem(mediaItem)
                prepare()
            }
        }

        if (!_isMuted.value) {
            player?.play()
        }
    }

    /**
     * Pause music playback
     */
    fun pause() {
        player?.pause()
    }

    /**
     * Resume music playback
     */
    fun resume() {
        if (!_isMuted.value) {
            player?.play()
        }
    }

    /**
     * Stop music playback
     */
    fun stop() {
        player?.stop()
        currentTrack = null
    }

    /**
     * Set music volume (0.0 to 1.0)
     */
    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        _volume.value = clampedVolume

        if (!_isMuted.value) {
            player?.volume = clampedVolume
        }
    }

    /**
     * Mute music
     */
    fun mute() {
        _isMuted.value = true
        player?.volume = 0f
    }

    /**
     * Unmute music
     */
    fun unmute() {
        _isMuted.value = false
        player?.volume = _volume.value
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
     * Check if music is currently playing
     */
    fun isPlaying(): Boolean = player?.isPlaying == true

    /**
     * Get current track
     */
    fun getCurrentTrack(): MusicTrack? = currentTrack

    /**
     * Switch to a different track
     */
    fun switchTrack(track: MusicTrack) {
        val wasPlaying = isPlaying()
        stop()
        currentTrack = null

        if (wasPlaying) {
            play(track)
        }
    }

    /**
     * Fade out music over duration
     */
    fun fadeOut(durationMs: Long = 1000) {
        val startVolume = _volume.value
        val steps = 20
        val stepDuration = durationMs / steps
        val volumeStep = startVolume / steps

        var currentStep = 0

        val fadeRunnable = object : Runnable {
            override fun run() {
                if (currentStep < steps) {
                    val newVolume = startVolume - (volumeStep * currentStep)
                    player?.volume = newVolume.coerceAtLeast(0f)
                    currentStep++
                    android.os.Handler(android.os.Looper.getMainLooper())
                        .postDelayed(this, stepDuration)
                } else {
                    pause()
                    player?.volume = startVolume
                }
            }
        }

        android.os.Handler(android.os.Looper.getMainLooper()).post(fadeRunnable)
    }

    /**
     * Fade in music over duration
     */
    fun fadeIn(track: MusicTrack = MusicTrack.getDefault(), durationMs: Long = 1000) {
        val targetVolume = _volume.value
        player?.volume = 0f

        play(track)

        val steps = 20
        val stepDuration = durationMs / steps
        val volumeStep = targetVolume / steps

        var currentStep = 0

        val fadeRunnable = object : Runnable {
            override fun run() {
                if (currentStep < steps) {
                    val newVolume = volumeStep * currentStep
                    player?.volume = newVolume.coerceAtMost(targetVolume)
                    currentStep++
                    android.os.Handler(android.os.Looper.getMainLooper())
                        .postDelayed(this, stepDuration)
                } else {
                    player?.volume = targetVolume
                }
            }
        }

        android.os.Handler(android.os.Looper.getMainLooper()).post(fadeRunnable)
    }

    /**
     * Release player resources
     */
    fun release() {
        player?.release()
        player = null
        currentTrack = null
        _isPlaying.value = false
    }
}
