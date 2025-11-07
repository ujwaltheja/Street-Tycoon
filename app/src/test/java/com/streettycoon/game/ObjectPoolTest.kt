package com.streettycoon.game

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for ObjectPool
 */
class ObjectPoolTest {

    data class TestObject(var value: Int = 0, var name: String = "")

    private lateinit var pool: ObjectPool<TestObject>

    @Before
    fun setup() {
        pool = ObjectPool(
            factory = { TestObject() },
            reset = { obj ->
                obj.value = 0
                obj.name = ""
            },
            maxSize = 10,
            initialSize = 5
        )
    }

    @Test
    fun `test pool initializes with correct size`() {
        val stats = pool.getStats()
        assertEquals(5, stats.available, "Pool should start with 5 objects")
        assertEquals(0, stats.active, "No objects should be active initially")
    }

    @Test
    fun `test acquire returns object`() {
        val obj = pool.acquire()
        assertNotNull(obj)

        val stats = pool.getStats()
        assertEquals(1, stats.active, "One object should be active")
        assertEquals(4, stats.available, "Four objects should be available")
    }

    @Test
    fun `test release returns object to pool`() {
        val obj = pool.acquire()
        obj.value = 42
        obj.name = "test"

        pool.release(obj)

        val stats = pool.getStats()
        assertEquals(0, stats.active, "No objects should be active")
        assertEquals(5, stats.available, "All objects should be available")

        // Verify object was reset
        val newObj = pool.acquire()
        assertEquals(0, newObj.value, "Value should be reset to 0")
        assertEquals("", newObj.name, "Name should be reset to empty")
    }

    @Test
    fun `test pool respects max size`() {
        // Acquire and release more objects than max size
        val objects = mutableListOf<TestObject>()
        repeat(20) {
            objects.add(pool.acquire())
        }

        objects.forEach { pool.release(it) }

        val stats = pool.getStats()
        assertTrue(stats.available <= 10, "Pool should not exceed max size of 10")
    }

    @Test
    fun `test pool creates new objects when empty`() {
        // Acquire all initial objects
        repeat(5) { pool.acquire() }

        // Acquire one more - should create new object
        val obj = pool.acquire()
        assertNotNull(obj)

        val stats = pool.getStats()
        assertEquals(6, stats.totalCreated, "Pool should have created 6 objects total")
    }

    @Test
    fun `test pool stats utilization`() {
        repeat(3) { pool.acquire() }

        val stats = pool.getStats()
        val expectedUtilization = (3f / 10f) * 100f
        assertEquals(expectedUtilization, stats.utilizationPercent, 0.01f)
    }

    @Test
    fun `test pool clear`() {
        pool.acquire()
        pool.acquire()

        pool.clear()

        val stats = pool.getStats()
        assertEquals(0, stats.available, "Pool should be empty after clear")
        assertEquals(0, stats.active, "No objects should be active after clear")
    }

    @Test
    fun `test pool use extension function`() {
        var valueSet = false

        pool.use { obj ->
            obj.value = 99
            valueSet = true
        }

        assertTrue(valueSet, "Use block should execute")

        // Object should be automatically released
        val stats = pool.getStats()
        assertEquals(0, stats.active, "Object should be released after use")
    }

    @Test
    fun `test GameObjectPools format number`() {
        assertEquals("42", GameObjectPools.formatNumber(42))
        assertEquals("1.00K", GameObjectPools.formatNumber(1_000))
        assertEquals("1.50K", GameObjectPools.formatNumber(1_500))
        assertEquals("1.00M", GameObjectPools.formatNumber(1_000_000))
        assertEquals("1.50M", GameObjectPools.formatNumber(1_500_000))
        assertEquals("1.00B", GameObjectPools.formatNumber(1_000_000_000))
        assertEquals("1.00T", GameObjectPools.formatNumber(1_000_000_000_000))
    }

    @Test
    fun `test ParticlePool acquire and release`() {
        val particlePool = ParticlePool(maxSize = 100)

        val particle = particlePool.acquire()
        assertNotNull(particle)
        assertTrue(particle.isActive, "Particle should be active after acquire")

        particlePool.release(particle)
        assertFalse(particle.isActive, "Particle should be inactive after reset")

        val stats = particlePool.getStats()
        assertTrue(stats.available > 0, "Particle should be available in pool")
    }

    @Test
    fun `test ArrayPool`() {
        val arrayPool = ArrayPool(
            factory = { size -> IntArray(size) },
            arraySize = 256,
            maxSize = 10
        )

        val array = arrayPool.acquire()
        assertNotNull(array)
        assertEquals(256, array.size)

        array[0] = 42
        arrayPool.release(array)

        // Get another array
        val array2 = arrayPool.acquire()
        // Should be the same array from pool
        assertEquals(42, array2[0])
    }
}
