package com.streettycoon.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Advanced particle system for visual effects
 * Supports coins, stars, explosions, and custom particles
 */
class ParticleSystem {

    companion object {
        private const val UPDATE_INTERVAL = 16L // ~60 FPS
        private const val MAX_PARTICLES = 500
    }

    data class Particle(
        val id: Long,
        val position: Offset,
        val velocity: Offset,
        val acceleration: Offset = Offset(0f, 0.5f), // Gravity
        val color: Color,
        val size: Float,
        val alpha: Float,
        val rotation: Float = 0f,
        val rotationSpeed: Float = 0f,
        val lifespan: Long,
        val createdAt: Long,
        val type: ParticleType
    ) {
        fun isAlive(currentTime: Long): Boolean {
            return (currentTime - createdAt) < lifespan
        }

        fun getProgress(currentTime: Long): Float {
            val elapsed = currentTime - createdAt
            return (elapsed.toFloat() / lifespan.toFloat()).coerceIn(0f, 1f)
        }
    }

    enum class ParticleType {
        COIN,
        STAR,
        SPARKLE,
        SMOKE,
        CONFETTI,
        HEART,
        EXPLOSION
    }

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    private var nextParticleId = 0L
    private var updateJob: Job? = null
    private var isRunning = false

    /**
     * Start the particle system update loop
     */
    fun start() {
        if (isRunning) return
        isRunning = true

        updateJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive && isRunning) {
                update()
                delay(UPDATE_INTERVAL)
            }
        }
    }

    /**
     * Stop the particle system
     */
    fun stop() {
        isRunning = false
        updateJob?.cancel()
        _particles.value = emptyList()
    }

    /**
     * Update all particles
     */
    private fun update() {
        val currentTime = System.currentTimeMillis()
        val updatedParticles = _particles.value
            .filter { it.isAlive(currentTime) }
            .map { particle ->
                val progress = particle.getProgress(currentTime)
                val dt = UPDATE_INTERVAL / 1000f

                // Update physics
                val newVelocity = Offset(
                    particle.velocity.x + particle.acceleration.x * dt,
                    particle.velocity.y + particle.acceleration.y * dt
                )
                val newPosition = Offset(
                    particle.position.x + newVelocity.x * dt,
                    particle.position.y + newVelocity.y * dt
                )

                // Update rotation
                val newRotation = particle.rotation + particle.rotationSpeed * dt

                // Fade out at the end
                val newAlpha = if (progress > 0.7f) {
                    1f - ((progress - 0.7f) / 0.3f)
                } else {
                    particle.alpha
                }

                particle.copy(
                    position = newPosition,
                    velocity = newVelocity,
                    rotation = newRotation,
                    alpha = newAlpha
                )
            }

        _particles.value = updatedParticles
    }

    /**
     * Emit coin particles (money earned effect)
     */
    fun emitCoins(position: Offset, count: Int = 10, amount: Int = 0) {
        if (_particles.value.size + count > MAX_PARTICLES) return

        val newParticles = mutableListOf<Particle>()
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 100f + 50f

            newParticles.add(
                Particle(
                    id = nextParticleId++,
                    position = position,
                    velocity = Offset(
                        cos(angle) * speed,
                        sin(angle) * speed - 100f // Initial upward velocity
                    ),
                    acceleration = Offset(0f, 200f), // Gravity
                    color = Color(0xFFFFD700), // Gold
                    size = Random.nextFloat() * 8f + 12f,
                    alpha = 1f,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = Random.nextFloat() * 360f - 180f,
                    lifespan = Random.nextLong(1000, 2000),
                    createdAt = System.currentTimeMillis(),
                    type = ParticleType.COIN
                )
            )
        }

        _particles.value = _particles.value + newParticles
    }

    /**
     * Emit star particles (achievement/level up effect)
     */
    fun emitStars(position: Offset, count: Int = 20) {
        if (_particles.value.size + count > MAX_PARTICLES) return

        val newParticles = mutableListOf<Particle>()
        for (i in 0 until count) {
            val angle = (i.toFloat() / count.toFloat()) * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 150f + 100f

            newParticles.add(
                Particle(
                    id = nextParticleId++,
                    position = position,
                    velocity = Offset(
                        cos(angle) * speed,
                        sin(angle) * speed
                    ),
                    acceleration = Offset(0f, 50f),
                    color = listOf(
                        Color(0xFFFFD700), // Gold
                        Color(0xFFFF69B4), // Hot pink
                        Color(0xFF00CED1), // Dark turquoise
                        Color(0xFFFF4500)  // Orange red
                    ).random(),
                    size = Random.nextFloat() * 6f + 10f,
                    alpha = 1f,
                    rotation = 0f,
                    rotationSpeed = Random.nextFloat() * 720f - 360f,
                    lifespan = Random.nextLong(1500, 2500),
                    createdAt = System.currentTimeMillis(),
                    type = ParticleType.STAR
                )
            )
        }

        _particles.value = _particles.value + newParticles
    }

    /**
     * Emit sparkle particles (tap effect)
     */
    fun emitSparkles(position: Offset, count: Int = 5) {
        if (_particles.value.size + count > MAX_PARTICLES) return

        val newParticles = mutableListOf<Particle>()
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 50f + 30f

            newParticles.add(
                Particle(
                    id = nextParticleId++,
                    position = position,
                    velocity = Offset(
                        cos(angle) * speed,
                        sin(angle) * speed
                    ),
                    acceleration = Offset.Zero,
                    color = Color.White,
                    size = Random.nextFloat() * 4f + 6f,
                    alpha = 1f,
                    rotation = 0f,
                    rotationSpeed = 0f,
                    lifespan = Random.nextLong(300, 600),
                    createdAt = System.currentTimeMillis(),
                    type = ParticleType.SPARKLE
                )
            )
        }

        _particles.value = _particles.value + newParticles
    }

    /**
     * Emit confetti particles (celebration effect)
     */
    fun emitConfetti(position: Offset, count: Int = 30) {
        if (_particles.value.size + count > MAX_PARTICLES) return

        val colors = listOf(
            Color(0xFFFF6B6B), // Red
            Color(0xFF4ECDC4), // Cyan
            Color(0xFFFFE66D), // Yellow
            Color(0xFF95E1D3), // Mint
            Color(0xFFF38181), // Pink
            Color(0xFFAA96DA)  // Purple
        )

        val newParticles = mutableListOf<Particle>()
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 200f + 100f

            newParticles.add(
                Particle(
                    id = nextParticleId++,
                    position = position,
                    velocity = Offset(
                        cos(angle) * speed,
                        sin(angle) * speed - 150f
                    ),
                    acceleration = Offset(0f, 300f),
                    color = colors.random(),
                    size = Random.nextFloat() * 8f + 8f,
                    alpha = 1f,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = Random.nextFloat() * 1080f - 540f,
                    lifespan = Random.nextLong(2000, 3000),
                    createdAt = System.currentTimeMillis(),
                    type = ParticleType.CONFETTI
                )
            )
        }

        _particles.value = _particles.value + newParticles
    }

    /**
     * Emit explosion particles
     */
    fun emitExplosion(position: Offset, count: Int = 40) {
        if (_particles.value.size + count > MAX_PARTICLES) return

        val newParticles = mutableListOf<Particle>()
        for (i in 0 until count) {
            val angle = (i.toFloat() / count.toFloat()) * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 300f + 200f

            newParticles.add(
                Particle(
                    id = nextParticleId++,
                    position = position,
                    velocity = Offset(
                        cos(angle) * speed,
                        sin(angle) * speed
                    ),
                    acceleration = Offset.Zero,
                    color = listOf(
                        Color(0xFFFF4500), // Orange
                        Color(0xFFFFD700), // Gold
                        Color(0xFFFF6347)  // Tomato
                    ).random(),
                    size = Random.nextFloat() * 12f + 8f,
                    alpha = 1f,
                    rotation = 0f,
                    rotationSpeed = 0f,
                    lifespan = Random.nextLong(500, 1000),
                    createdAt = System.currentTimeMillis(),
                    type = ParticleType.EXPLOSION
                )
            )
        }

        _particles.value = _particles.value + newParticles
    }

    /**
     * Clear all particles
     */
    fun clear() {
        _particles.value = emptyList()
    }

    /**
     * Get particle count
     */
    fun getParticleCount(): Int = _particles.value.size
}
