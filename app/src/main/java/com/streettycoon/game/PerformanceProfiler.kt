package com.streettycoon.game

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.LinkedList

/**
 * Professional performance profiler for monitoring game performance
 * Tracks FPS, memory usage, and performance metrics in real-time
 */
class PerformanceProfiler(private val context: Context) {

    companion object {
        private const val TAG = "PerformanceProfiler"
        private const val FRAME_TIME_WINDOW = 60 // Track last 60 frames
        private const val MEMORY_CHECK_INTERVAL = 1000L // Check memory every second
        private const val FPS_UPDATE_INTERVAL = 500L // Update FPS twice per second
    }

    data class PerformanceMetrics(
        val fps: Float = 0f,
        val avgFrameTime: Float = 0f,
        val maxFrameTime: Float = 0f,
        val minFrameTime: Float = Float.MAX_VALUE,
        val memoryUsedMB: Float = 0f,
        val memoryTotalMB: Float = 0f,
        val memoryPercentage: Float = 0f,
        val isLagging: Boolean = false,
        val frameDrops: Int = 0
    )

    private val _metrics = MutableStateFlow(PerformanceMetrics())
    val metrics: StateFlow<PerformanceMetrics> = _metrics.asStateFlow()

    private val frameTimes = LinkedList<Long>()
    private var lastFrameTime = 0L
    private var frameDropCount = 0
    private var monitoringJob: Job? = null

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private var isEnabled = false

    /**
     * Start performance monitoring
     */
    fun start() {
        if (isEnabled) return
        isEnabled = true
        lastFrameTime = System.nanoTime()

        monitoringJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive && isEnabled) {
                updateMemoryMetrics()
                delay(MEMORY_CHECK_INTERVAL)
            }
        }

        Log.d(TAG, "Performance monitoring started")
    }

    /**
     * Stop performance monitoring
     */
    fun stop() {
        isEnabled = false
        monitoringJob?.cancel()
        frameTimes.clear()
        Log.d(TAG, "Performance monitoring stopped")
    }

    /**
     * Record a frame - call this every frame from your game loop
     */
    fun recordFrame() {
        if (!isEnabled) return

        val currentTime = System.nanoTime()
        val frameTime = currentTime - lastFrameTime
        lastFrameTime = currentTime

        // Convert to milliseconds
        val frameTimeMs = frameTime / 1_000_000f

        // Add to window
        frameTimes.add(frameTime)
        if (frameTimes.size > FRAME_TIME_WINDOW) {
            frameTimes.removeFirst()
        }

        // Detect frame drops (frame took longer than 16.6ms = 60fps)
        if (frameTimeMs > 16.6f) {
            frameDropCount++
        }

        // Update FPS metrics periodically
        updateFpsMetrics()
    }

    private fun updateFpsMetrics() {
        if (frameTimes.isEmpty()) return

        val avgFrameTimeNs = frameTimes.average()
        val maxFrameTimeNs = frameTimes.maxOrNull() ?: 0L
        val minFrameTimeNs = frameTimes.minOrNull() ?: 0L

        val avgFrameTimeMs = (avgFrameTimeNs / 1_000_000f).toFloat()
        val fps = if (avgFrameTimeMs > 0) 1000f / avgFrameTimeMs else 0f

        // Consider lagging if FPS drops below 45
        val isLagging = fps < 45f

        val currentMetrics = _metrics.value
        _metrics.value = currentMetrics.copy(
            fps = fps,
            avgFrameTime = avgFrameTimeMs,
            maxFrameTime = (maxFrameTimeNs / 1_000_000f).toFloat(),
            minFrameTime = (minFrameTimeNs / 1_000_000f).toFloat(),
            isLagging = isLagging,
            frameDrops = frameDropCount
        )
    }

    private fun updateMemoryMetrics() {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        // Get app memory usage
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()

        val usedMB = usedMemory / (1024f * 1024f)
        val maxMB = maxMemory / (1024f * 1024f)
        val percentage = (usedMemory.toFloat() / maxMemory.toFloat()) * 100f

        val currentMetrics = _metrics.value
        _metrics.value = currentMetrics.copy(
            memoryUsedMB = usedMB,
            memoryTotalMB = maxMB,
            memoryPercentage = percentage
        )

        // Log warning if memory usage is high
        if (percentage > 80f) {
            Log.w(TAG, "High memory usage: ${percentage}% (${usedMB}MB / ${maxMB}MB)")
        }
    }

    /**
     * Get debug info string
     */
    fun getDebugString(): String {
        val m = _metrics.value
        return """
            FPS: ${"%.1f".format(m.fps)}
            Frame: ${"%.2f".format(m.avgFrameTime)}ms (min: ${"%.2f".format(m.minFrameTime)}ms, max: ${"%.2f".format(m.maxFrameTime)}ms)
            Memory: ${"%.1f".format(m.memoryUsedMB)}MB / ${"%.1f".format(m.memoryTotalMB)}MB (${"%.1f".format(m.memoryPercentage)}%)
            Drops: ${m.frameDrops}
            Status: ${if (m.isLagging) "LAGGING" else "OK"}
        """.trimIndent()
    }

    /**
     * Reset frame drop counter
     */
    fun resetFrameDrops() {
        frameDropCount = 0
    }

    /**
     * Check if performance is good
     */
    fun isPerformanceGood(): Boolean {
        val m = _metrics.value
        return m.fps >= 50f && m.memoryPercentage < 75f
    }

    /**
     * Get memory pressure level (0 = good, 1 = warning, 2 = critical)
     */
    fun getMemoryPressureLevel(): Int {
        val percentage = _metrics.value.memoryPercentage
        return when {
            percentage < 60f -> 0
            percentage < 80f -> 1
            else -> 2
        }
    }
}
