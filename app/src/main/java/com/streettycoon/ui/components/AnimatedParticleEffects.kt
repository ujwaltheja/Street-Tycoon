package com.streettycoon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streettycoon.ui.theme.Colors
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ==================== FLOATING COIN PARTICLE ====================
/**
 * Individual coin particle for money rewards
 */
@Composable
fun FloatingCoinParticle(
    modifier: Modifier = Modifier,
    duration: Long = 1200L,
    onComplete: () -> Unit = {}
) {
    var offsetY by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(1f) }
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        val angle = (Math.random() * 360).toFloat()

        while (System.currentTimeMillis() - startTime < duration) {
            val progress = (System.currentTimeMillis() - startTime).toFloat() / duration

            // Upward motion with arc
            offsetY = -120f * progress
            offsetX = 60f * cos(Math.toRadians(angle.toDouble())).toFloat() * progress * 0.5f

            // Rotation effect
            rotation = progress * 720f

            // Fade and scale out
            alpha = (1f - progress).coerceAtLeast(0f)
            scale = 1f + progress * 0.3f

            delay(16)
        }
        onComplete()
    }

    Box(
        modifier = modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .scale(scale)
            .size(20.dp)
            .clip(CircleShape)
            .background(Colors.CurrencyGold.copy(alpha = alpha))
    ) {
        Text(
            text = "💰",
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

// ==================== FLOATING STAR PARTICLE ====================
/**
 * Star particle for achievements and level-ups
 */
@Composable
fun FloatingStarParticle(
    modifier: Modifier = Modifier,
    duration: Long = 1500L,
    onComplete: () -> Unit = {}
) {
    var offsetY by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(1f) }
    var scale by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        val angle = (Math.random() * 360).toFloat()

        while (System.currentTimeMillis() - startTime < duration) {
            val progress = (System.currentTimeMillis() - startTime).toFloat() / duration

            // Outward motion in random direction
            offsetY = -100f * progress
            offsetX = 80f * cos(Math.toRadians(angle.toDouble())).toFloat() * progress

            // Fade out
            alpha = (1f - progress).coerceAtLeast(0f)
            scale = (1f - progress * 0.5f).coerceAtLeast(0.2f)

            delay(16)
        }
        onComplete()
    }

    Box(
        modifier = modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .scale(scale)
            .size(16.dp)
            .clip(CircleShape)
            .background(Colors.CurrencyGold.copy(alpha = alpha))
    ) {
        Text(
            text = "⭐",
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

// ==================== FLOATING SPARKLE PARTICLE ====================
/**
 * Sparkle effect for magical moments
 */
@Composable
fun FloatingSparkleParticle(
    modifier: Modifier = Modifier,
    duration: Long = 800L,
    onComplete: () -> Unit = {}
) {
    var offsetY by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        val angle = (Math.random() * 360).toFloat()

        while (System.currentTimeMillis() - startTime < duration) {
            val progress = (System.currentTimeMillis() - startTime).toFloat() / duration

            offsetY = -60f * progress
            offsetX = 40f * cos(Math.toRadians(angle.toDouble())).toFloat() * progress

            alpha = (1f - progress).coerceAtLeast(0f)

            delay(16)
        }
        onComplete()
    }

    Box(
        modifier = modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .size(8.dp)
            .clip(CircleShape)
            .background(Colors.CurrencyGold.copy(alpha = alpha))
    )
}

// ==================== FLOATING TEXT PARTICLE ====================
/**
 * Floating text particle for damage/healing numbers and notifications
 */
@Composable
fun FloatingTextParticle(
    text: String,
    textColor: Color = Colors.SuccessGreen,
    modifier: Modifier = Modifier,
    duration: Long = 1000L,
    fontSize: Int = 20,
    onComplete: () -> Unit = {}
) {
    var offsetY by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(1f) }
    var scale by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < duration) {
            val progress = (System.currentTimeMillis() - startTime).toFloat() / duration

            offsetY = -100f * progress
            alpha = (1f - progress).coerceAtLeast(0f)
            scale = 1f + progress * 0.5f

            delay(16)
        }
        onComplete()
    }

    Box(
        modifier = modifier
            .offset(y = offsetY.dp)
            .scale(scale)
    ) {
        Text(
            text = text,
            color = textColor.copy(alpha = alpha),
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==================== CONFETTI BURST EFFECT ====================
/**
 * Container for multiple confetti particles bursting
 */
@Composable
fun ConfettiBurst(
    count: Int = 15,
    modifier: Modifier = Modifier,
    duration: Long = 1500L
) {
    val particles = remember { List(count) { Random.nextLong() } }
    var displayedCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        particles.forEachIndexed { index, _ ->
            delay(50L * index)
            displayedCount = index + 1
        }
    }

    Box(modifier = modifier) {
        repeat(displayedCount) { index ->
            FloatingSparkleParticle(
                duration = duration,
                onComplete = {}
            )
        }
    }
}

// ==================== COIN BURST EFFECT ====================
/**
 * Burst of coins that float upward
 */
@Composable
fun CoinBurst(
    count: Int = 8,
    modifier: Modifier = Modifier,
    duration: Long = 1200L
) {
    val particles = remember { List(count) { Random.nextLong() } }
    var displayedCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        particles.forEachIndexed { index, _ ->
            delay(30L * index)
            displayedCount = index + 1
        }
    }

    Box(modifier = modifier) {
        repeat(displayedCount) { index ->
            FloatingCoinParticle(
                duration = duration,
                onComplete = {}
            )
        }
    }
}

// ==================== STAR BURST EFFECT ====================
/**
 * Burst of stars that explode outward
 */
@Composable
fun StarBurst(
    count: Int = 10,
    modifier: Modifier = Modifier,
    duration: Long = 1500L
) {
    val particles = remember { List(count) { Random.nextLong() } }
    var displayedCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        particles.forEachIndexed { index, _ ->
            delay(40L * index)
            displayedCount = index + 1
        }
    }

    Box(modifier = modifier) {
        repeat(displayedCount) { index ->
            FloatingStarParticle(
                duration = duration,
                onComplete = {}
            )
        }
    }
}

// ==================== PULSING REWARD BADGE ====================
/**
 * Pulsing badge for showing rewards
 */
@Composable
fun PulsingRewardBadge(
    text: String,
    icon: String = "🎁",
    modifier: Modifier = Modifier,
    backgroundColor: Color = Colors.ComboYellow,
    textColor: Color = Colors.TextPrimary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reward_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

// ==================== FLOATING HEART PARTICLE ====================
/**
 * Floating heart for love/affection effects
 */
@Composable
fun FloatingHeartParticle(
    modifier: Modifier = Modifier,
    duration: Long = 1200L,
    onComplete: () -> Unit = {}
) {
    var offsetY by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(1f) }
    var scale by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        val angle = (Math.random() * 360).toFloat()

        while (System.currentTimeMillis() - startTime < duration) {
            val progress = (System.currentTimeMillis() - startTime).toFloat() / duration

            offsetY = -100f * progress
            offsetX = 50f * sin(Math.toRadians(angle.toDouble())).toFloat() * progress

            alpha = (1f - progress).coerceAtLeast(0f)
            scale = 1f + progress * 0.2f

            delay(16)
        }
        onComplete()
    }

    Box(
        modifier = modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .scale(scale)
            .size(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "❤️",
            fontSize = 16.sp,
            modifier = Modifier.graphicsLayer(alpha = alpha)
        )
    }
}

// ==================== HEART BURST EFFECT ====================
/**
 * Burst of hearts for love/celebration
 */
@Composable
fun HeartBurst(
    count: Int = 12,
    modifier: Modifier = Modifier,
    duration: Long = 1500L
) {
    val particles = remember { List(count) { Random.nextLong() } }
    var displayedCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        particles.forEachIndexed { index, _ ->
            delay(35L * index)
            displayedCount = index + 1
        }
    }

    Box(modifier = modifier) {
        repeat(displayedCount) { index ->
            FloatingHeartParticle(
                duration = duration,
                onComplete = {}
            )
        }
    }
}

// ==================== COMBO CELEBRATION EFFECT ====================
/**
 * Combined celebration effect for big combo moments
 */
@Composable
fun ComboCelebration(
    comboCount: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Multiple particle bursts
        StarBurst(count = 8, duration = 1500L)

        Spacer(modifier = Modifier.size(100.dp))

        CoinBurst(count = 6, duration = 1200L)
    }
}
