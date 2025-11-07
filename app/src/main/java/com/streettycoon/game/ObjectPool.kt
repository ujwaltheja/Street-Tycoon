package com.streettycoon.game

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * Generic object pooling system for reducing memory allocations
 * Thread-safe implementation for high-performance gaming
 */
class ObjectPool<T>(
    private val factory: () -> T,
    private val reset: (T) -> Unit,
    private val maxSize: Int = 100,
    initialSize: Int = 10
) {
    private val pool = ConcurrentLinkedQueue<T>()
    private val activeCount = AtomicInteger(0)
    private val totalCreated = AtomicInteger(0)

    init {
        // Pre-populate pool with initial objects
        repeat(initialSize) {
            pool.offer(factory())
            totalCreated.incrementAndGet()
        }
    }

    /**
     * Acquire an object from the pool
     */
    fun acquire(): T {
        val obj = pool.poll() ?: run {
            totalCreated.incrementAndGet()
            factory()
        }
        activeCount.incrementAndGet()
        return obj
    }

    /**
     * Release an object back to the pool
     */
    fun release(obj: T) {
        if (pool.size < maxSize) {
            reset(obj)
            pool.offer(obj)
        }
        activeCount.decrementAndGet()
    }

    /**
     * Clear the pool
     */
    fun clear() {
        pool.clear()
        activeCount.set(0)
    }

    /**
     * Get statistics
     */
    fun getStats(): PoolStats {
        return PoolStats(
            available = pool.size,
            active = activeCount.get(),
            totalCreated = totalCreated.get(),
            maxSize = maxSize
        )
    }

    data class PoolStats(
        val available: Int,
        val active: Int,
        val totalCreated: Int,
        val maxSize: Int
    ) {
        val utilizationPercent: Float
            get() = if (maxSize > 0) (active.toFloat() / maxSize.toFloat()) * 100f else 0f
    }
}

/**
 * Pooled object wrapper with automatic release
 */
class PooledObject<T>(
    private val pool: ObjectPool<T>,
    val value: T
) : AutoCloseable {
    override fun close() {
        pool.release(value)
    }
}

/**
 * Extension function to use pooled objects with automatic release
 */
inline fun <T, R> ObjectPool<T>.use(block: (T) -> R): R {
    val obj = acquire()
    try {
        return block(obj)
    } finally {
        release(obj)
    }
}

/**
 * Pre-configured pools for common game objects
 */
object GameObjectPools {

    /**
     * Pool for temporary string builders (for number formatting, etc.)
     */
    val stringBuilderPool = ObjectPool(
        factory = { StringBuilder(32) },
        reset = { it.clear() },
        maxSize = 50,
        initialSize = 10
    )

    /**
     * Pool for array lists (for temporary collections)
     */
    fun <T> createListPool(initialCapacity: Int = 10): ObjectPool<ArrayList<T>> {
        return ObjectPool(
            factory = { ArrayList(initialCapacity) },
            reset = { it.clear() },
            maxSize = 50,
            initialSize = 5
        )
    }

    /**
     * Pool for hash maps (for temporary maps)
     */
    fun <K, V> createMapPool(initialCapacity: Int = 16): ObjectPool<HashMap<K, V>> {
        return ObjectPool(
            factory = { HashMap(initialCapacity) },
            reset = { it.clear() },
            maxSize = 30,
            initialSize = 5
        )
    }

    /**
     * Get formatted number string using pooled StringBuilder
     */
    fun formatNumber(number: Long): String {
        return stringBuilderPool.use { sb ->
            when {
                number >= 1_000_000_000_000 -> {
                    val value = number / 1_000_000_000_000.0
                    sb.append(String.format("%.2fT", value))
                }
                number >= 1_000_000_000 -> {
                    val value = number / 1_000_000_000.0
                    sb.append(String.format("%.2fB", value))
                }
                number >= 1_000_000 -> {
                    val value = number / 1_000_000.0
                    sb.append(String.format("%.2fM", value))
                }
                number >= 1_000 -> {
                    val value = number / 1_000.0
                    sb.append(String.format("%.2fK", value))
                }
                else -> {
                    sb.append(number.toString())
                }
            }
            sb.toString()
        }
    }

    /**
     * Get all pool statistics
     */
    fun getAllStats(): String {
        return buildString {
            appendLine("=== Object Pool Statistics ===")
            appendLine("StringBuilder Pool: ${stringBuilderPool.getStats()}")
        }
    }
}

/**
 * Memory pool for primitive arrays (reduces GC pressure)
 */
class ArrayPool<T>(
    private val factory: (Int) -> T,
    private val arraySize: Int,
    maxSize: Int = 20
) {
    private val pool = ConcurrentLinkedQueue<T>()
    private val maxPoolSize = maxSize

    fun acquire(): T {
        return pool.poll() ?: factory(arraySize)
    }

    fun release(array: T) {
        if (pool.size < maxPoolSize) {
            pool.offer(array)
        }
    }

    fun clear() {
        pool.clear()
    }
}

/**
 * Common array pools
 */
object CommonArrayPools {
    val intArray256Pool = ArrayPool(
        factory = { size -> IntArray(size) },
        arraySize = 256,
        maxSize = 20
    )

    val floatArray256Pool = ArrayPool(
        factory = { size -> FloatArray(size) },
        arraySize = 256,
        maxSize = 20
    )

    val byteArray1024Pool = ArrayPool(
        factory = { size -> ByteArray(size) },
        arraySize = 1024,
        maxSize = 20
    )
}

/**
 * Particle pool specifically for particle system
 */
class ParticlePool(maxSize: Int = 500) {
    data class ReusableParticle(
        var x: Float = 0f,
        var y: Float = 0f,
        var vx: Float = 0f,
        var vy: Float = 0f,
        var life: Float = 1f,
        var color: Int = 0xFFFFFFFF.toInt(),
        var size: Float = 10f,
        var isActive: Boolean = false
    )

    private val pool = ObjectPool(
        factory = { ReusableParticle() },
        reset = { particle ->
            particle.x = 0f
            particle.y = 0f
            particle.vx = 0f
            particle.vy = 0f
            particle.life = 1f
            particle.color = 0xFFFFFFFF.toInt()
            particle.size = 10f
            particle.isActive = false
        },
        maxSize = maxSize,
        initialSize = 50
    )

    fun acquire(): ReusableParticle = pool.acquire().apply { isActive = true }
    fun release(particle: ReusableParticle) = pool.release(particle)
    fun getStats() = pool.getStats()
    fun clear() = pool.clear()
}
