package com.streettycoon.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.math.roundToInt

/**
 * Animated money counter that smoothly transitions between values
 */
@Composable
fun AnimatedMoneyCounter(
    targetValue: Double,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 32.sp,
    color: Color = Color(0xFF4CAF50),
    prefix: String = "₹",
    animationDuration: Int = 1000
) {
    var displayValue by remember { mutableStateOf(targetValue) }

    LaunchedEffect(targetValue) {
        val startValue = displayValue
        val difference = targetValue - startValue
        val startTime = System.currentTimeMillis()
        val duration = animationDuration.toLong()

        while (System.currentTimeMillis() - startTime < duration) {
            val elapsed = System.currentTimeMillis() - startTime
            val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)

            // Ease out cubic interpolation for smooth deceleration
            val easedProgress = 1f - (1f - progress) * (1f - progress) * (1f - progress)

            displayValue = startValue + (difference * easedProgress)
            delay(16) // ~60 FPS
        }

        displayValue = targetValue
    }

    val formattedMoney = formatMoney(displayValue)
    val accessibilityText = formatCurrencyForAccessibility(targetValue, prefix)

    Text(
        text = "$prefix$formattedMoney",
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier.semantics {
            contentDescription = "Current balance: $accessibilityText"
        }
    )
}

/**
 * Format money value with K/M/B suffixes
 */
private fun formatMoney(value: Double): String {
    return when {
        value >= 1_000_000_000 -> String.format("%.2fB", value / 1_000_000_000)
        value >= 1_000_000 -> String.format("%.2fM", value / 1_000_000)
        value >= 10_000 -> String.format("%.1fK", value / 1_000)
        value >= 1_000 -> String.format("%.0fK", value / 1_000)
        else -> value.roundToInt().toString()
    }
}

/**
 * Format currency for accessibility announcements
 */
private fun formatCurrencyForAccessibility(amount: Double, prefix: String = "₹"): String {
    val currencyName = when (prefix) {
        "₹" -> "rupees"
        "$" -> "dollars"
        else -> "currency"
    }

    return when {
        amount >= 1_000_000_000 -> "$currencyName ${String.format("%.2f", amount / 1_000_000_000)} billion"
        amount >= 1_000_000 -> "$currencyName ${String.format("%.2f", amount / 1_000_000)} million"
        amount >= 1_000 -> "$currencyName ${String.format("%.1f", amount / 1_000)} thousand"
        else -> "$currencyName ${amount.toInt()}"
    }
}

/**
 * Pulsing animation for important elements
 */
@Composable
fun PulsingElement(
    content: @Composable () -> Unit,
    pulseScale: Float = 1.1f,
    durationMillis: Int = 1000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = pulseScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(modifier = Modifier.scale(scale)) {
        content()
    }
}

/**
 * Bouncing animation for tap feedback
 */
@Composable
fun BouncingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .then(
                Modifier.noRippleClickable {
                    isPressed = true
                    onClick()
                    // Reset after animation
                    scope.launch {
                        delay(100)
                        isPressed = false
                    }
                }
            )
    ) {
        content()
    }
}

/**
 * Floating coin animation for earnings
 */
@Composable
fun FloatingCoin(
    amount: Double,
    onAnimationEnd: () -> Unit
) {
    var offsetY by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        val duration = 1500L

        while (System.currentTimeMillis() - startTime < duration) {
            val progress = (System.currentTimeMillis() - startTime).toFloat() / duration
            offsetY = -100f * progress
            alpha = 1f - progress
            delay(16)
        }

        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .scale(1f + offsetY / 100f)
    ) {
        Text(
            text = "+₹${formatMoney(amount)}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50).copy(alpha = alpha)
        )
    }
}

/**
 * Shimmer loading effect
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "translate"
    )

    Box(
        modifier = modifier
            .background(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFE0E0E0),
                        Color(0xFFF5F5F5),
                        Color(0xFFE0E0E0)
                    ),
                    startX = translateAnim - 300f,
                    endX = translateAnim
                )
            )
    )
}

/**
 * Progress bar with animation
 */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFE0E0E0),
    progressColor: Color = Color(0xFF4CAF50),
    height: androidx.compose.ui.unit.Dp = 8.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(CircleShape)
                .background(progressColor)
        )
    }
}

/**
 * Fade in animation for new content
 */
@Composable
fun FadeInContent(
    visible: Boolean = true,
    durationMillis: Int = 300,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis)) +
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(durationMillis)
                ),
        exit = fadeOut(animationSpec = tween(durationMillis / 2))
    ) {
        content()
    }
}

/**
 * Scale in animation for cards
 */
@Composable
fun ScaleInCard(
    visible: Boolean = true,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    var show by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible && delay > 0) {
            delay(delay.toLong())
        }
        show = visible
    }

    AnimatedVisibility(
        visible = show,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(),
        exit = scaleOut(targetScale = 0.8f) + fadeOut()
    ) {
        content()
    }
}

/**
 * Ripple effect for tap interactions
 */
@Composable
fun TapRippleEffect(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF2196F3).copy(alpha = 0.3f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_alpha"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

/**
 * No-ripple clickable modifier (for custom animations)
 */
@Composable
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    return this.then(
        clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    )
}

/**
 * Counter badge with animation
 */
@Composable
fun AnimatedCounterBadge(
    count: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFE53935),
    textColor: Color = Color.White
) {
    AnimatedVisibility(
        visible = count > 0,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Box(
            modifier = modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
