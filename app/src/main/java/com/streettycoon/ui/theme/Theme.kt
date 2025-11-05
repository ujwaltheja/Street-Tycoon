package com.streettycoon.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Street Tycoon Color Palette
 * Inspired by vibrant Indian street markets
 */

// Primary - Green (Money, Success, Growth)
private val Green700 = Color(0xFF388E3C)
private val Green500 = Color(0xFF4CAF50)
private val Green300 = Color(0xFF81C784)

// Secondary - Orange (Energy, Warmth, Food)
private val Orange700 = Color(0xFFF57C00)
private val Orange500 = Color(0xFFFF9800)
private val Orange300 = Color(0xFFFFB74D)

// Tertiary - Blue (Trust, Business, Progress)
private val Blue700 = Color(0xFF1565C0)
private val Blue500 = Color(0xFF2196F3)
private val Blue300 = Color(0xFF64B5F6)

// Error - Red
private val Red700 = Color(0xFFC62828)
private val Red500 = Color(0xFFE53935)
private val Red300 = Color(0xFFE57373)

// Neutral colors
private val Gray900 = Color(0xFF212121)
private val Gray800 = Color(0xFF424242)
private val Gray700 = Color(0xFF616161)
private val Gray500 = Color(0xFF9E9E9E)
private val Gray300 = Color(0xFFE0E0E0)
private val Gray100 = Color(0xFFF5F5F5)
private val Gray50 = Color(0xFFFAFAFA)

/**
 * Light color scheme for Street Tycoon
 */
private val LightColorScheme = lightColorScheme(
    primary = Green500,
    onPrimary = Color.White,
    primaryContainer = Green300,
    onPrimaryContainer = Gray900,

    secondary = Orange500,
    onSecondary = Color.White,
    secondaryContainer = Orange300,
    onSecondaryContainer = Gray900,

    tertiary = Blue500,
    onTertiary = Color.White,
    tertiaryContainer = Blue300,
    onTertiaryContainer = Gray900,

    error = Red500,
    onError = Color.White,
    errorContainer = Red300,
    onErrorContainer = Gray900,

    background = Gray50,
    onBackground = Gray900,

    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,

    outline = Gray300,
    outlineVariant = Gray100,

    scrim = Color.Black.copy(alpha = 0.32f),

    inverseSurface = Gray800,
    inverseOnSurface = Color.White,
    inversePrimary = Green300,

    surfaceTint = Green500
)

/**
 * Dark color scheme for Street Tycoon
 */
private val DarkColorScheme = darkColorScheme(
    primary = Green300,
    onPrimary = Gray900,
    primaryContainer = Green700,
    onPrimaryContainer = Green300,

    secondary = Orange300,
    onSecondary = Gray900,
    secondaryContainer = Orange700,
    onSecondaryContainer = Orange300,

    tertiary = Blue300,
    onTertiary = Gray900,
    tertiaryContainer = Blue700,
    onTertiaryContainer = Blue300,

    error = Red300,
    onError = Gray900,
    errorContainer = Red700,
    onErrorContainer = Red300,

    background = Color(0xFF121212),
    onBackground = Color.White,

    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray300,

    outline = Gray700,
    outlineVariant = Gray800,

    scrim = Color.Black.copy(alpha = 0.5f),

    inverseSurface = Gray100,
    inverseOnSurface = Gray900,
    inversePrimary = Green700,

    surfaceTint = Green300
)

/**
 * Street Tycoon theme with Material3 design
 *
 * @param darkTheme Whether to use dark theme. Defaults to system preference.
 * @param dynamicColor Whether to use dynamic color from Android 12+. Defaults to true.
 * @param content The composable content to theme.
 */
@Composable
fun StreetTycoonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic color is available on Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

/**
 * Extension colors for game-specific UI elements
 */
object GameColors {
    // Currency and earnings
    val Money = Color(0xFF4CAF50)
    val MoneyDark = Color(0xFF388E3C)
    val MoneyLight = Color(0xFF81C784)

    // Stall types
    val Tea = Color(0xFF8D6E63)
    val Dosa = Color(0xFFFFB74D)
    val Momos = Color(0xFFF06292)
    val Juice = Color(0xFF4FC3F7)

    // Level indicators
    val Level1 = Color(0xFF9E9E9E)  // Gray - Basic
    val Level2 = Color(0xFF4CAF50)  // Green - Standard
    val Level3 = Color(0xFF2196F3)  // Blue - Premium
    val Level4 = Color(0xFF9C27B0)  // Purple - Luxury

    // Status colors
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFFA726)
    val Error = Color(0xFFE53935)
    val Info = Color(0xFF2196F3)

    // Family and happiness
    val Happy = Color(0xFF4CAF50)
    val Neutral = Color(0xFFFFA726)
    val Unhappy = Color(0xFFE53935)

    // Special effects
    val Glow = Color(0xFF4CAF50).copy(alpha = 0.3f)
    val Combo = Color(0xFFFF9800)
    val Particle = Color(0xFFFFEB3B)
}
