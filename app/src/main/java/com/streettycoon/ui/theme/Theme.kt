package com.streettycoon.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Warm Orange Theme (from HTML prototype)
private val WarmOrangeColorScheme = lightColorScheme(
    primary = Colors.OrangePrimary,
    onPrimary = Colors.OrangeOnPrimary,
    primaryContainer = Colors.OrangePrimaryContainer,
    onPrimaryContainer = Colors.OrangeOnPrimaryContainer,

    secondary = Colors.TealPrimary,
    onSecondary = Colors.TealOnPrimary,
    secondaryContainer = Colors.CreamWarm,
    onSecondaryContainer = Colors.TextPrimary,

    tertiary = Colors.OrangeAccent,
    onTertiary = Colors.OrangeOnPrimary,
    tertiaryContainer = Colors.CreamWarm,
    onTertiaryContainer = Colors.TextSecondary,

    error = Colors.ErrorRed,
    onError = Colors.OrangeOnPrimary,
    errorContainer = Colors.ErrorLight,
    onErrorContainer = Colors.OrangeOnPrimary,

    background = Colors.CreamBackground,
    onBackground = Colors.TextPrimary,

    surface = Colors.CreamSurface,
    onSurface = Colors.TextPrimary,
    surfaceVariant = Colors.CreamLight,
    onSurfaceVariant = Colors.TextSecondary,

    outline = Colors.CardBorder,
    outlineVariant = Colors.CardBorder,
)

// Legacy Green Theme (kept for backwards compatibility)
private val LegacyGreenColorScheme = lightColorScheme(
    primary = Colors.GreenPrimary,
    onPrimary = Colors.GreenOnPrimary,
    primaryContainer = Colors.GreenPrimaryContainer,
    onPrimaryContainer = Colors.GreenOnPrimaryContainer,

    secondary = Colors.TealPrimary,
    onSecondary = Colors.TealOnPrimary,
    secondaryContainer = Colors.CreamWarm,
    onSecondaryContainer = Colors.TextPrimary,

    tertiary = Colors.GreenPrimary,
    onTertiary = Colors.GreenOnPrimary,
    tertiaryContainer = Colors.GreenPrimaryContainer,
    onTertiaryContainer = Colors.GreenOnPrimaryContainer,

    error = Colors.ErrorRed,
    onError = Colors.GreenOnPrimary,

    background = Colors.CreamBackground,
    onBackground = Colors.TextPrimary,

    surface = Colors.CreamSurface,
    onSurface = Colors.TextPrimary,
    surfaceVariant = Colors.CreamLight,
    onSurfaceVariant = Colors.TextSecondary,

    outline = Colors.CardBorder,
)

@Composable
fun StreetTycoonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,  // Disabled to use custom warm orange theme
    content: @Composable () -> Unit
) {
    // Use warm orange theme from HTML prototype
    val colorScheme = WarmOrangeColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            if (activity == null) {
                // Context is not an Activity, cannot modify window
                return@SideEffect
            }
            val window = activity.window
            // Set status bar to orange gradient color
            window.statusBarColor = Colors.OrangePrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StreetTycoonTypography,
        shapes = StreetTycoonShapes,
        content = content
    )
}
