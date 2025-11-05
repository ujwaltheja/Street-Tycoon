package com.streettycoon.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streettycoon.ui.theme.GameColors
import java.text.NumberFormat
import kotlin.math.roundToInt

/**
 * Enhanced tap button with spring physics, haptics, and visual feedback
 * Perfect for main game action button
 */
@Composable
fun EnhancedTapButton(
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Float = 160f,
    label: String = "TAP",
    icon: androidx.compose.material.icons.Icons.Filled? = null
) {
    var pressedCount by remember { mutableStateOf(0) }
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (pressedCount > 0) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    val shadowElevation by animateFloatAsState(
        targetValue = if (pressedCount > 0) 4f else 12f,
        label = "shadowElevation"
    )

    Box(
        modifier = modifier
            .size(size.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                shadowElevation = shadowElevation
            )
            .shadow(shadowElevation.dp, CircleShape)
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (enabled) {
                    pressedCount++
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTap()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Animated money counter with format and color flash on increase
 */
@Composable
fun AnimatedMoneyCounter(
    amount: Double,
    modifier: Modifier = Modifier,
    label: String = "Money",
    decimals: Int = 0
) {
    var lastAmount by remember { mutableStateOf(amount) }
    var isIncreasing by remember { mutableStateOf(false) }

    LaunchedEffect(amount) {
        if (amount > lastAmount) {
            isIncreasing = true
            lastAmount = amount
        }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isIncreasing) 1f else 1f,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing),
        label = "moneyFlash",
        finishedListener = {
            isIncreasing = false
        }
    )

    val textColor by animateColorAsState(
        targetValue = if (isIncreasing) GameColors.Money else Color.Unspecified,
        label = "textColor"
    )

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatCurrency(amount, decimals),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor.takeIf { isIncreasing } ?: GameColors.Money,
            modifier = Modifier.graphicsLayer(alpha = animatedAlpha)
        )
    }
}

/**
 * Financial health indicator with color coding
 */
@Composable
fun FinancialHealthIndicator(
    expenseRatio: Float,
    modifier: Modifier = Modifier
) {
    val (color, status) = when {
        expenseRatio < 5f -> Pair(GameColors.Happy, "Excellent")
        expenseRatio < 20f -> Pair(GameColors.Success, "Healthy")
        expenseRatio < 30f -> Pair(GameColors.Warning, "Warning")
        else -> Pair(GameColors.Error, "Critical")
    }

    val progress = (expenseRatio / 100f).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Financial Health",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = status,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surface
        )
    }
}

/**
 * Animated progress ring for gate progress, level progression, etc
 */
@Composable
fun AnimatedProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Float = 120f,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
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
            .background(backgroundColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.fillMaxSize(),
            color = color,
            trackColor = backgroundColor,
            strokeWidth = 4.dp
        )
        Text(
            text = "${(animatedProgress * 100).roundToInt()}%",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Premium card with elevation and hover effect
 */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Float = 8f,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    Surface(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(elevation.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                enabled = onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (onClick != null) {
                    isPressed = true
                    onClick()
                    isPressed = false
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        content()
    }
}

/**
 * Format currency with K (thousands) and M (millions) notation
 */
fun formatCurrency(amount: Double, decimals: Int = 0): String {
    return when {
        amount >= 1_000_000 -> {
            val millions = amount / 1_000_000
            "₹${String.format("%.${decimals}f", millions)}M"
        }
        amount >= 1_000 -> {
            val thousands = amount / 1_000
            "₹${String.format("%.${decimals}f", thousands)}K"
        }
        else -> {
            "₹${String.format("%.${decimals}f", amount)}"
        }
    }
}

/**
 * Format large numbers with abbreviations
 */
fun formatNumber(number: Double): String {
    return when {
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000)
        number >= 1_000 -> String.format("%.1fK", number / 1_000)
        else -> String.format("%.0f", number)
    }
}
