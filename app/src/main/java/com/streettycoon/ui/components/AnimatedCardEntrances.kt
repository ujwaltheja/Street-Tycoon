package com.streettycoon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// ==================== CARD ENTRANCE ANIMATIONS ====================

/**
 * Wrapper for animating card entrance with slide + fade effect
 */
@Composable
fun SlideInCard(
    modifier: Modifier = Modifier,
    duration: Int = 500,
    delay: Int = 0,
    fromLeft: Boolean = true,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        isVisible = true
    }

    val offsetX by animateFloatAsState(
        targetValue = if (isVisible) 0f else if (fromLeft) -100f else 100f,
        animationSpec = tween(duration, easing = FastOutSlowInEasing),
        label = "card_slide_x"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(duration, easing = LinearEasing),
        label = "card_alpha"
    )

    Box(
        modifier = modifier.graphicsLayer(
            translationX = offsetX,
            alpha = alpha
        )
    ) {
        content()
    }
}

/**
 * Wrapper for animating card entrance with scale + fade effect
 */
@Composable
fun ScaleInCard(
    modifier: Modifier = Modifier,
    duration: Int = 500,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(duration, easing = FastOutSlowInEasing),
        label = "card_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(duration, easing = LinearEasing),
        label = "card_alpha"
    )

    Box(
        modifier = modifier.graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            alpha = alpha
        )
    ) {
        content()
    }
}

/**
 * Wrapper for animating card entrance with slide + scale + fade
 */
@Composable
fun BounceInCard(
    modifier: Modifier = Modifier,
    duration: Int = 600,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        isVisible = true
    }

    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 50f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_bounce_y"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(duration, easing = LinearEasing),
        label = "card_alpha"
    )

    Box(
        modifier = modifier.graphicsLayer(
            translationY = offsetY,
            alpha = alpha
        )
    ) {
        content()
    }
}

/**
 * List of items with staggered entrance animation
 */
@Composable
fun <T> StaggeredEntranceList(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemDelayMs: Int = 100,
    animationDurationMs: Int = 500,
    content: @Composable (T, Int) -> Unit
) {
    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            SlideInCard(
                delay = index * itemDelayMs,
                duration = animationDurationMs,
                fromLeft = index % 2 == 0
            ) {
                content(item, index)
            }
        }
    }
}

/**
 * Grid of items with staggered entrance animation
 */
@Composable
fun <T> StaggeredEntranceGrid(
    items: List<T>,
    columns: Int = 2,
    modifier: Modifier = Modifier,
    itemDelayMs: Int = 80,
    animationDurationMs: Int = 500,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    content: @Composable (T, Int) -> Unit
) {
    Column(modifier = modifier) {
        items.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = verticalSpacing),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                row.forEachIndexed { columnIndex, item ->
                    val itemIndex = (items.indexOf(item))
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        ScaleInCard(
                            delay = itemIndex * itemDelayMs,
                            duration = animationDurationMs
                        ) {
                            content(item, itemIndex)
                        }
                    }
                }

                // Add spacers for incomplete rows
                repeat(columns - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Wrapper for rotating card entrance (3D-like effect)
 */
@Composable
fun RotateInCard(
    modifier: Modifier = Modifier,
    duration: Int = 600,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        isVisible = true
    }

    val rotationX by animateFloatAsState(
        targetValue = if (isVisible) 0f else 45f,
        animationSpec = tween(duration, easing = FastOutSlowInEasing),
        label = "card_rotation_x"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.3f,
        animationSpec = tween(duration, easing = LinearEasing),
        label = "card_alpha"
    )

    Box(
        modifier = modifier.graphicsLayer(
            rotationX = rotationX,
            alpha = alpha,
            cameraDistance = 8 * 24f // 8 * 24.dp in pixels
        )
    ) {
        content()
    }
}

/**
 * Sequence of entrance effects - each waits for previous to complete
 */
@Composable
fun SequentialEntrance(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var stage by remember { mutableStateOf(0) }

    LaunchedEffect(stage) {
        when (stage) {
            0 -> {
                delay(500) // Stage 1 complete
                stage = 1
            }
            1 -> {
                delay(500) // Stage 2 complete
                stage = 2
            }
            else -> {} // All stages complete
        }
    }

    Box(modifier = modifier) {
        content()
    }
}

// ==================== SCREEN TRANSITION EFFECTS ====================

/**
 * Fade transition between screens
 */
@Composable
fun FadeTransition(
    modifier: Modifier = Modifier,
    duration: Int = 300,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fade_transition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fade_alpha"
    )

    Box(modifier = modifier.graphicsLayer(alpha = alpha)) {
        content()
    }
}

/**
 * Slide transition from right to left
 */
@Composable
fun SlideLeftTransition(
    modifier: Modifier = Modifier,
    duration: Int = 500,
    content: @Composable () -> Unit
) {
    var isEntering by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isEntering = true
    }

    val offsetX by animateFloatAsState(
        targetValue = if (isEntering) 0f else 100f,
        animationSpec = tween(duration, easing = FastOutSlowInEasing),
        label = "slide_x"
    )

    Box(modifier = modifier.graphicsLayer(translationX = offsetX)) {
        content()
    }
}

// ==================== PROGRESSIVE REVEAL ====================

/**
 * Reveals content progressively (line by line or section by section)
 */
@Composable
fun ProgressiveReveal(
    modifier: Modifier = Modifier,
    totalDuration: Int = 1000,
    numberOfStages: Int = 3,
    content: @Composable (revealProgress: Float) -> Unit
) {
    var revealProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (revealProgress < 1f) {
            val elapsed = System.currentTimeMillis() - startTime
            revealProgress = (elapsed.toFloat() / totalDuration).coerceIn(0f, 1f)
            kotlinx.coroutines.delay(16)
        }
    }

    Box(modifier = modifier) {
        content(revealProgress)
    }
}

// ==================== BOUNCE UP ENTRANCE ====================

/**
 * Cards that bounce up one after another
 */
@Composable
fun BounceUpList(
    itemCount: Int,
    modifier: Modifier = Modifier,
    itemDelayMs: Int = 150,
    content: @Composable (index: Int) -> Unit
) {
    Column(modifier = modifier) {
        repeat(itemCount) { index ->
            BounceInCard(
                delay = index * itemDelayMs,
                duration = 400
            ) {
                content(index)
            }
        }
    }
}

// ==================== FLOATING ENTRANCE ====================

/**
 * Cards that float in from bottom with transparency
 */
@Composable
fun FloatInCard(
    modifier: Modifier = Modifier,
    duration: Int = 600,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        isVisible = true
    }

    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 60f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 400f
        ),
        label = "float_y"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(duration, easing = LinearEasing),
        label = "float_alpha"
    )

    Box(
        modifier = modifier.graphicsLayer(
            translationY = offsetY,
            alpha = alpha
        )
    ) {
        content()
    }
}
