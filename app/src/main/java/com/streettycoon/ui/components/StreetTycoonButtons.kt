package com.streettycoon.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.streettycoon.ui.animations.AnimationDurations
import com.streettycoon.ui.animations.EasingFunctions
import com.streettycoon.ui.animations.SpringSpecs
import com.streettycoon.ui.theme.Colors
import com.streettycoon.ui.theme.Spacing

// ==================== PRIMARY BUTTON ====================
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(64.dp)
            .width(200.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Colors.GreenPrimary,
            contentColor = Colors.GreenOnPrimary,
            disabledContainerColor = Colors.LockedGray
        ),
        enabled = enabled,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Text(text = text)
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
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .width(150.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Colors.GreenPrimaryContainer,
            contentColor = Colors.GreenOnPrimaryContainer
        ),
        enabled = enabled,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
    ) {
        Text(text = text)
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
            .height(40.dp)
            .width(100.dp),
        enabled = enabled,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    ) {
        Text(text = text)
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
    
    Button(
        onClick = {},
        modifier = modifier
            .size(96.dp)
            .scale(scale),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                comboCount > 10 -> Colors.ErrorRed
                comboCount > 5 -> Colors.ComboYellow
                else -> Colors.GreenPrimary
            }
        ),
        interactionSource = interactionSource
    ) {
        Text(
            text = "TAP",
            color = Colors.GreenOnPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )
    }
}