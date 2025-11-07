package com.streettycoon.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size classes for responsive layouts
 * Following Material3 guidelines
 */
enum class WindowSize {
    Compact,    // < 600dp (phones in portrait)
    Medium,     // 600dp - 840dp (tablets, phones in landscape)
    Expanded    // > 840dp (large tablets, desktops)
}

/**
 * Remember the current window size class based on screen width
 */
@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    return when {
        configuration.screenWidthDp < 600 -> WindowSize.Compact
        configuration.screenWidthDp < 840 -> WindowSize.Medium
        else -> WindowSize.Expanded
    }
}

/**
 * Get responsive padding based on window size
 */
@Composable
fun getResponsivePadding(): Dp {
    return when (rememberWindowSize()) {
        WindowSize.Compact -> 16.dp
        WindowSize.Medium -> 24.dp
        WindowSize.Expanded -> 32.dp
    }
}

/**
 * Get responsive card width based on window size
 */
@Composable
fun getResponsiveCardWidth(): Dp {
    return when (rememberWindowSize()) {
        WindowSize.Compact -> 320.dp
        WindowSize.Medium -> 400.dp
        WindowSize.Expanded -> 500.dp
    }
}

/**
 * Get number of columns for grid layouts
 */
@Composable
fun getGridColumns(): Int {
    return when (rememberWindowSize()) {
        WindowSize.Compact -> 2
        WindowSize.Medium -> 3
        WindowSize.Expanded -> 4
    }
}

/**
 * Check if device is in landscape mode
 */
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > configuration.screenHeightDp
}

/**
 * Get responsive font scale factor
 */
@Composable
fun getResponsiveFontScale(): Float {
    return when (rememberWindowSize()) {
        WindowSize.Compact -> 1.0f
        WindowSize.Medium -> 1.1f
        WindowSize.Expanded -> 1.2f
    }
}
