package com.streettycoon.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streettycoon.ui.theme.Colors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Enhanced tap button with animations, combo counter, and haptic feedback
 */
@Composable
fun EnhancedTapButton(
    onTap: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    emoji: String = "🍵",
    label: String = "Tap to Serve",
    hapticEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var scale by remember { mutableStateOf(1f) }
    var comboCount by remember { mutableStateOf(0) }
    var showCombo by remember { mutableStateOf(false) }
    var lastTapTime by remember { mutableStateOf(0L) }
    var lastHapticTime by remember { mutableStateOf(0L) }

    // Reset combo if no tap for 2 seconds
    LaunchedEffect(lastTapTime) {
        if (lastTapTime > 0) {
            delay(2000)
            if (System.currentTimeMillis() - lastTapTime >= 2000) {
                comboCount = 0
                showCombo = false
            }
        }
    }

    // Bounce animation
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_scale"
    )

    // Pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = if (comboCount > 1) {
                    "$label button. Combo: $comboCount. Tap to serve customers and earn money"
                } else {
                    "$label button. Tap to serve customers and earn money"
                }
                role = androidx.compose.ui.semantics.Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        // Glow effect
        if (enabled) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(1.2f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Colors.OrangePrimary.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Main tap button
        Card(
            modifier = Modifier
                .size(160.dp)
                .scale(animatedScale)
                .pointerInput(enabled) {
                    detectTapGestures(
                        onPress = {
                            if (enabled) {
                                scale = 0.9f
                                tryAwaitRelease()
                                scale = 1f
                            }
                        },
                        onTap = {
                            if (enabled) {
                                scope.launch {
                                    onTap()

                                    // Combo system
                                    val currentTime = System.currentTimeMillis()
                                    if (currentTime - lastTapTime < 500) {
                                        comboCount++
                                        showCombo = true
                                    } else {
                                        comboCount = 1
                                        showCombo = false
                                    }
                                    lastTapTime = currentTime

                                    // Debounced haptic feedback - only if enabled and 50ms since last haptic
                                    if (hapticEnabled && currentTime - lastHapticTime > 50) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        lastHapticTime = currentTime
                                    }
                                }
                            }
                        }
                    )
                },
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (enabled) 8.dp else 2.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (enabled) Colors.OrangePrimary else Colors.LockedGray
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        // Combo counter
        if (showCombo && comboCount > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 16.dp, y = (-16).dp)
            ) {
                ComboIndicator(
                    comboCount = comboCount,
                    modifier = Modifier.animateContentSize()
                )
            }
        }
    }
}

/**
 * Combo indicator badge
 */
@Composable
private fun ComboIndicator(
    comboCount: Int,
    modifier: Modifier = Modifier
) {
    // Bounce in animation
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "combo_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(Colors.ComboYellow)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "x$comboCount COMBO!",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Colors.TextPrimary
        )
    }
}

/**
 * Tap feedback particle effect
 */
@Composable
fun TapParticle(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetY by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(1f) }
    var scale by remember { mutableStateOf(1f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        val duration = 800L
        val angle = (Math.random() * 360).toFloat()

        while (System.currentTimeMillis() - startTime < duration) {
            val progress = (System.currentTimeMillis() - startTime).toFloat() / duration
            offsetY = -80f * progress
            offsetX = 40f * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat() * progress
            alpha = 1f - progress
            scale = 1f + progress * 0.5f
            delay(16)
        }

        onComplete()
    }

    Box(
        modifier = modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .scale(scale)
            .size(8.dp)
            .clip(CircleShape)
            .background(Colors.CurrencyGold.copy(alpha = alpha))
    )
}

/**
 * Multiple tap button for different stall types
 */
@Composable
fun StallTapButton(
    stallType: String,
    onTap: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val emoji = when (stallType) {
        "TEA" -> "🍵"
        "DOSA" -> "🥞"
        "MOMOS" -> "🥟"
        "JUICE" -> "🧃"
        else -> "🍽️"
    }

    val label = when (stallType) {
        "TEA" -> "Serve Tea"
        "DOSA" -> "Serve Dosa"
        "MOMOS" -> "Serve Momos"
        "JUICE" -> "Serve Juice"
        else -> "Tap to Serve"
    }

    EnhancedTapButton(
        onTap = onTap,
        enabled = enabled,
        modifier = modifier,
        emoji = emoji,
        label = label
    )
}
