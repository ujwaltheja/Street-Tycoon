package com.streettycoon.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material3 shape system for Street Tycoon
 *
 * Defines the corner shapes used throughout the app:
 * - Small: Buttons, badges, chips
 * - Medium: Cards, dialogs
 * - Large: Bottom sheets, large surfaces
 */
val Shapes = Shapes(
    // Small components (4dp)
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),

    // Medium components (12dp)
    medium = RoundedCornerShape(12.dp),

    // Large components (16dp)
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
