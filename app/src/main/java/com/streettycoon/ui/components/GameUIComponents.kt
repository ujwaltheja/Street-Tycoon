package com.streettycoon.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streettycoon.ui.theme.GameColors
import com.streettycoon.sound.SoundManager
import kotlin.math.absoluteValue

/**
 * Gaming-focused UI components with vibrant colors, glowing effects, and immersive animations
 */

/**
 * Gaming card with glowing border and shadow effects
 */
@Composable
fun GamingCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    glowColor: Color = GameColors.Success,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val glowIntensity by animateFloatAsState(
        targetValue = if (isPressed) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "glowIntensity"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(12.dp * glowIntensity, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = glowColor.copy(alpha = 0.6f * glowIntensity),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                enabled = onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick?.invoke()
                isPressed = false
            }
    ) {
        content()
    }
}

/**
 * Gaming button with glowing neon effect and haptic feedback
 */
@Composable
fun GamingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glowColor: Color = GameColors.Success,
    textColor: Color = Color.White,
    size: ButtonSize = ButtonSize.MEDIUM
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    var isPressed by remember { mutableStateOf(false) }

    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0.6f,
        label = "glowAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(16.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.9f),
                        glowColor.copy(alpha = 0.7f)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                color = glowColor.copy(alpha = glowAlpha),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (enabled) {
                    isPressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    soundManager.playTapSound()
                    onClick()
                    isPressed = false
                }
            }
            .padding(
                horizontal = when (size) {
                    ButtonSize.SMALL -> 12.dp
                    ButtonSize.MEDIUM -> 24.dp
                    ButtonSize.LARGE -> 32.dp
                },
                vertical = when (size) {
                    ButtonSize.SMALL -> 8.dp
                    ButtonSize.MEDIUM -> 12.dp
                    ButtonSize.LARGE -> 16.dp
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else Color.Gray,
            fontSize = when (size) {
                ButtonSize.SMALL -> 12.sp
                ButtonSize.MEDIUM -> 14.sp
                ButtonSize.LARGE -> 16.sp
            },
            fontWeight = FontWeight.Bold
        )
    }
}

enum class ButtonSize {
    SMALL, MEDIUM, LARGE
}

/**
 * Gaming currency display with shimmer effect
 */
@Composable
fun GamingCurrencyDisplay(
    amount: Double,
    label: String = "Cash",
    modifier: Modifier = Modifier,
    size: CurrencySize = CurrencySize.MEDIUM
) {
    val infiniteTransition = rememberInfiniteTransition(label = "currencyShimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Column(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        GameColors.Success.copy(alpha = 0.15f),
                        GameColors.Warning.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = GameColors.Success.copy(alpha = shimmerAlpha),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = when (size) {
                CurrencySize.SMALL -> 10.sp
                CurrencySize.MEDIUM -> 12.sp
                CurrencySize.LARGE -> 14.sp
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "₹${formatCurrency(amount)}",
            fontSize = when (size) {
                CurrencySize.SMALL -> 16.sp
                CurrencySize.MEDIUM -> 20.sp
                CurrencySize.LARGE -> 24.sp
            },
            fontWeight = FontWeight.Bold,
            color = GameColors.Success,
            modifier = Modifier.graphicsLayer(alpha = shimmerAlpha)
        )
    }
}

enum class CurrencySize {
    SMALL, MEDIUM, LARGE
}

/**
 * Gaming progress bar with animated filling effect
 */
@Composable
fun GamingProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    progressColor: Color = GameColors.Success
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progressAnimation"
    )

    Column(modifier = modifier) {
        if (label != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 1.5.dp,
                    color = progressColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                progressColor.copy(alpha = 0.7f),
                                progressColor
                            )
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )
            )
        }
    }
}

/**
 * Gaming status badge with color-coded indicators
 */
@Composable
fun GamingStatusBadge(
    text: String,
    status: BadgeStatus = BadgeStatus.GOOD,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status) {
        BadgeStatus.GOOD -> GameColors.Success
        BadgeStatus.WARNING -> GameColors.Warning
        BadgeStatus.CRITICAL -> GameColors.Error
        BadgeStatus.INFO -> GameColors.Info
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor.copy(alpha = 0.2f))
            .border(
                width = 1.dp,
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = backgroundColor
        )
    }
}

enum class BadgeStatus {
    GOOD, WARNING, CRITICAL, INFO
}

/**
 * Gaming progress ring with percentage display
 */
@Composable
fun GamingProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Float = 120f,
    color: Color = GameColors.Success
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progressRing"
    )

    Box(
        modifier = modifier
            .size(size.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = color.copy(alpha = 0.5f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.fillMaxSize(),
            color = color,
            trackColor = MaterialTheme.colorScheme.surface,
            strokeWidth = 6.dp
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Floating game stat indicator
 */
@Composable
fun GameStatIndicator(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = GameColors.Success
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.5.dp,
                color = color.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Format currency value with proper abbreviation
 */
fun formatCurrency(amount: Double): String {
    return when {
        amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000)
        amount >= 1_000 -> String.format("%.1fK", amount / 1_000)
        else -> String.format("%.0f", amount)
    }
}
