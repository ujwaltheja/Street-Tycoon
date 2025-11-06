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

private val LightColorScheme = lightColorScheme(
    primary = Colors.GreenPrimary,
    onPrimary = Colors.GreenOnPrimary,
    primaryContainer = Colors.GreenPrimaryContainer,
    onPrimaryContainer = Colors.GreenOnPrimaryContainer,

    secondary = Colors.SecondaryGreen,
    onSecondary = Colors.GreenOnPrimary,
    secondaryContainer = Colors.SecondaryContainer,
    onSecondaryContainer = Colors.OnSecondaryContainer,

    tertiary = Colors.TertiaryGreen,
    onTertiary = Colors.GreenOnPrimary,
    tertiaryContainer = Colors.GreenPrimaryContainer,
    onTertiaryContainer = Colors.GreenOnPrimaryContainer,

    error = Colors.ErrorRed,
    onError = Colors.GreenOnPrimary,

    background = Colors.NeutralBackground,
    onBackground = Colors.GreenOnPrimaryContainer,

    surface = Colors.NeutralSurface,
    onSurface = Colors.GreenOnPrimaryContainer,
    surfaceVariant = Colors.NeutralSurfaceVariant,
    onSurfaceVariant = Colors.OnSecondaryContainer,

    outline = Colors.OutlineVariant,
)

@Composable
fun StreetTycoonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            if (activity == null) {
                // Context is not an Activity, cannot modify window
                return@SideEffect
            }
            val window = activity.window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StreetTycoonTypography,
        shapes = StreetTycoonShapes,
        content = content
    )
}
