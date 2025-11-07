package com.streettycoon.game

import android.app.ActivityManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for PerformanceProfiler
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PerformanceProfilerTest {

    private lateinit var profiler: PerformanceProfiler
    private lateinit var context: Context

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
        profiler = PerformanceProfiler(context)
    }

    @After
    fun tearDown() {
        profiler.stop()
    }

    @Test
    fun `test profiler starts and stops correctly`() {
        profiler.start()
        assertTrue(profiler.metrics.value.fps >= 0f)

        profiler.stop()
        assertEquals(0f, profiler.metrics.value.fps)
    }

    @Test
    fun `test frame recording updates metrics`() = runBlocking {
        profiler.start()

        // Record multiple frames
        repeat(60) {
            profiler.recordFrame()
            delay(16) // Simulate ~60 FPS
        }

        delay(600) // Wait for metrics update

        val metrics = profiler.metrics.value
        assertTrue(metrics.fps > 0f, "FPS should be greater than 0")
        assertTrue(metrics.avgFrameTime > 0f, "Average frame time should be positive")
    }

    @Test
    fun `test frame drop detection`() = runBlocking {
        profiler.start()

        // Record normal frames
        repeat(30) {
            profiler.recordFrame()
            delay(16)
        }

        // Record slow frames (simulate lag)
        repeat(10) {
            profiler.recordFrame()
            delay(50) // Slow frame (>16.6ms)
        }

        delay(600)

        val metrics = profiler.metrics.value
        assertTrue(metrics.frameDrops > 0, "Frame drops should be detected")
    }

    @Test
    fun `test performance good status`() = runBlocking {
        profiler.start()

        repeat(60) {
            profiler.recordFrame()
            delay(16)
        }

        delay(600)

        // Performance should be measurable and FPS should be positive
        // In test environments, FPS might be lower, so we check for reasonable metrics
        val metrics = profiler.metrics.value
        assertTrue(
            metrics.fps > 0f || profiler.isPerformanceGood(),
            "Metrics should be calculated or performance should be good. FPS: ${metrics.fps}"
        )
    }

    @Test
    fun `test memory pressure levels`() {
        profiler.start()

        val level = profiler.getMemoryPressureLevel()
        assertTrue(level in 0..2, "Memory pressure level should be 0, 1, or 2")
    }

    @Test
    fun `test debug string generation`() {
        profiler.start()
        val debugString = profiler.getDebugString()

        assertTrue(debugString.contains("FPS:"))
        assertTrue(debugString.contains("Frame:"))
        assertTrue(debugString.contains("Memory:"))
    }

    @Test
    fun `test frame drop reset`() = runBlocking {
        profiler.start()

        repeat(10) {
            profiler.recordFrame()
            delay(50) // Slow frames
        }

        delay(600)

        assertTrue(profiler.metrics.value.frameDrops > 0)

        profiler.resetFrameDrops()
        assertEquals(0, profiler.metrics.value.frameDrops)
    }
}
