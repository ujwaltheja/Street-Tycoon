package com.streettycoon.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.streettycoon.ui.animations.SpringSpecs
import com.streettycoon.ui.theme.Colors
import com.streettycoon.ui.theme.CornerRadius
import com.streettycoon.ui.theme.Spacing

// ==================== PRIMARY BUTTON ====================
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(CornerRadius.Large)
    val gradient = Brush.linearGradient(
        colors = listOf(
            Colors.OrangePrimary,
            Colors.OrangeSecondary,
            Colors.OrangeAccent
        )
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .height(60.dp)
            .defaultMinSize(minWidth = 220.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Colors.OrangeOnPrimary,
            disabledContainerColor = Color.Transparent
        ),
        enabled = enabled,
        shape = shape,
        contentPadding = PaddingValues(),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    if (enabled) gradient else Brush.linearGradient(listOf(Colors.LockedGray, Colors.LockedGray))
                )
                .border(width = 1.dp, color = Colors.OverlayLight.copy(alpha = 0.6f), shape = shape)
                .padding(horizontal = Spacing.xl, vertical = Spacing.md),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, textAlign = TextAlign.Center)
        }
    }
}

// ==================== SECONDARY BUTTON ====================
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(CornerRadius.Large)
    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .defaultMinSize(minWidth = 180.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Colors.TealOnPrimary,
            disabledContainerColor = Color.Transparent
        ),
        enabled = enabled,
        shape = shape,
        contentPadding = PaddingValues(),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    if (enabled) Brush.linearGradient(listOf(Colors.TealPrimary, Colors.TealLight))
                    else Brush.linearGradient(listOf(Colors.LockedGray, Colors.LockedGray))
                )
                .border(width = 1.dp, color = Colors.OverlayLight.copy(alpha = 0.4f), shape = shape)
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, textAlign = TextAlign.Center)
        }
    }
}

// ==================== TERTIARY BUTTON ====================
@Composable
fun TertiaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .defaultMinSize(minWidth = 140.dp),
        enabled = enabled,
        shape = RoundedCornerShape(CornerRadius.Medium),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Colors.TextPrimary),
        border = androidx.compose.foundation.BorderStroke(1.dp, Colors.OutlineVariant)
    ) {
        Text(text = text, textAlign = TextAlign.Center)
    }
}

// ==================== TAP SERVE BUTTON (ENHANCED) ====================
@Composable
fun TapServeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    comboCount: Int = 0
) {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = SpringSpecs.BouncySpring,
        label = "tap_button_scale"
    )

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isPressed = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is PressInteraction.Release -> {
                    isPressed = false
                    onClick()
                }
                is PressInteraction.Cancel -> {
                    isPressed = false
                }
            }
        }
    }

    val activeBrush = when {
        comboCount > 10 -> Brush.linearGradient(listOf(Colors.ErrorRed, Colors.ErrorLight))
        comboCount > 5 -> Brush.linearGradient(listOf(Colors.ComboYellow, Colors.CurrencyGold))
        else -> Brush.linearGradient(listOf(Colors.OrangePrimary, Colors.TealPrimary))
    }

    Button(
        onClick = {},
        modifier = modifier
            .size(110.dp)
            .scale(scale),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(activeBrush)
                .border(width = 4.dp, color = Colors.OverlayLight.copy(alpha = 0.6f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "TAP",
                color = Colors.OrangeOnPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
        }
    }
}