package com.streettycoon.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.sin

/**
 * Animated aurora inspired backdrop used across high energy screens.
 * The gradient gently drifts to keep the UI feeling alive without being distracting.
 */
@Composable
fun AnimatedAuroraBackground(
    modifier: Modifier = Modifier,
    colorStops: List<Color> = listOf(
        Color(0xFF120C1F),
        Color(0xFF2B115E),
        Color(0xFF54127D),
        Color(0xFF932F8B),
        Color(0xFFFF7A45)
    ),
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "aurora_animation")
    val verticalDrift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aurora_vertical_drift"
    )
    val horizontalDrift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aurora_horizontal_drift"
    )

    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = colorStops,
                    start = Offset(x = horizontalDrift * 600f, y = verticalDrift * 1200f),
                    end = Offset(x = (1f - horizontalDrift) * -600f, y = (1f - verticalDrift) * -1200f)
                )
            )
    ) {
        // Soft light streaks hovering over the gradient to mimic animated illustrations.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val waveHeight = size.height * 0.35f
            val baseY = size.height * 0.25f
            val path = Path()
            val temporalOffset = horizontalDrift * PI.toFloat() * 2f
            val amplitude = waveHeight * 0.35f
            val waveLength = size.width / 2f
            path.moveTo(-size.width, baseY)
            var x = -size.width
            while (x <= size.width * 2f) {
                val y = baseY + sin((x / waveLength) + temporalOffset) * amplitude
                path.lineTo(x, y)
                x += 16f
            }
            path.lineTo(size.width * 2f, size.height)
            path.lineTo(-size.width, size.height)
            path.close()
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                )
            )
        }

        content()
    }
}
