package com.streettycoon.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Street Tycoon color palette - vibrant Indian street theme
private val OrangePrimary = Color(0xFFFF6B35)
private val OrangeSecondary = Color(0xFFF7931E)
private val GreenAccent = Color(0xFF4CAF50)
private val YellowHighlight = Color(0xFFFFC107)
private val DarkBrown = Color(0xFF3E2723)
private val LightCream = Color(0xFFFFF8E1)

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    secondary = OrangeSecondary,
    tertiary = GreenAccent,
    background = DarkBrown,
    surface = Color(0xFF4E342E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    secondary = OrangeSecondary,
    tertiary = GreenAccent,
    background = LightCream,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DarkBrown,
    onSurface = DarkBrown
)

@Composable
fun StreetTycoonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
