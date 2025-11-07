package com.streettycoon.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.streettycoon.game.model.StallType
import kotlin.math.roundToInt

/**
 * Futuristic skyline illustration rendered directly in Compose so that we do not rely on static bitmaps.
 */
@Composable
fun CityHeroIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val gradient = Brush.verticalGradient(
            colors = listOf(
                Color(0x22FFFFFF),
                Color.Transparent
            )
        )
        // Glow base
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x33FFFFFF), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.9f),
                radius = size.width
            ),
            size = size
        )

        // Skyline base gradient
        drawRect(
            brush = gradient,
            size = Size(width = size.width, height = size.height * 0.75f),
            topLeft = Offset.Zero
        )

        val towerColors = listOf(
            Color(0xFF5B4AF2),
            Color(0xFF8A63F4),
            Color(0xFFCC3AF3),
            Color(0xFFFF6E6C)
        )
        val buildingCount = 6
        val buildingWidth = size.width / (buildingCount * 1.5f)
        val maxHeight = size.height * 0.65f

        repeat(buildingCount) { index ->
            val heightFactor = 0.45f + (index % 3) * 0.12f
            val buildingHeight = maxHeight * heightFactor
            val left = index * buildingWidth * 1.4f + (size.width * 0.1f)
            val buildingColor = towerColors[index % towerColors.size]
            val buildingBrush = Brush.verticalGradient(
                colors = listOf(
                    buildingColor.copy(alpha = 0.9f),
                    buildingColor.copy(alpha = 0.4f)
                )
            )
            drawRoundRect(
                brush = buildingBrush,
                topLeft = Offset(x = left, y = maxHeight - buildingHeight),
                size = Size(width = buildingWidth, height = buildingHeight),
                cornerRadius = CornerRadius(x = buildingWidth * 0.18f, y = buildingWidth * 0.18f)
            )
            // Windows
            val windowHeight = buildingWidth * 0.2f
            val windowWidth = buildingWidth * 0.35f
            var yOffset = maxHeight - buildingHeight + windowHeight
            val windowBrush = Brush.horizontalGradient(
                colors = listOf(Color.White.copy(alpha = 0.65f), Color.Transparent)
            )
            while (yOffset < maxHeight - windowHeight) {
                val rowShift = if ((yOffset * 10).roundToInt() % 2 == 0) 0f else windowWidth * 0.5f
                val windowsPerRow = 2
                repeat(windowsPerRow) { rowIndex ->
                    val xOffset = left + rowShift + rowIndex * (windowWidth * 1.4f) + buildingWidth * 0.1f
                    if (xOffset + windowWidth < left + buildingWidth) {
                        drawRoundRect(
                            brush = windowBrush,
                            topLeft = Offset(x = xOffset, y = yOffset),
                            size = Size(windowWidth, windowHeight),
                            cornerRadius = CornerRadius(windowWidth * 0.3f)
                        )
                    }
                }
                yOffset += windowHeight * 2f
            }
        }

        // Street strip for depth
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF1B1740).copy(alpha = 0.8f), Color.Transparent),
                start = Offset(x = 0f, y = size.height * 0.82f),
                end = Offset(x = 0f, y = size.height)
            ),
            topLeft = Offset(x = 0f, y = size.height * 0.78f),
            size = Size(width = size.width, height = size.height * 0.22f)
        )
    }
}

/**
 * Compact circular badge that gives each zone its own stylised skyline.
 */
@Composable
fun ZoneBadgeIllustration(
    modifier: Modifier = Modifier,
    zoneIndex: Int
) {
    val palette = remember {
        listOf(
            listOf(Color(0xFF64B5F6), Color(0xFF1E88E5)),
            listOf(Color(0xFFBA68C8), Color(0xFF8E24AA)),
            listOf(Color(0xFF81C784), Color(0xFF2E7D32)),
            listOf(Color(0xFFFFB74D), Color(0xFFF57C00)),
            listOf(Color(0xFFF06292), Color(0xFFC2185B)),
            listOf(Color(0xFFFFF176), Color(0xFFFBC02D))
        )
    }
    val colors = palette[zoneIndex % palette.size]
    Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors.first().copy(alpha = 0.95f), colors.last().copy(alpha = 0.65f), Color.Transparent),
                center = center,
                radius = size.minDimension / 2f
            ),
            radius = size.minDimension / 2f
        )
        val buildingColor = Color.White.copy(alpha = 0.65f)
        val baseHeight = size.height * 0.6f
        val buildingWidth = size.width * 0.18f
        repeat(3) { i ->
            val left = size.width * (0.2f + i * 0.25f)
            val height = baseHeight * (0.6f + i * 0.2f)
            drawRoundRect(
                color = buildingColor,
                topLeft = Offset(x = left, y = size.height - height - size.height * 0.15f),
                size = Size(width = buildingWidth, height = height),
                cornerRadius = CornerRadius(buildingWidth * 0.3f)
            )
        }
        drawCircle(
            color = Color.White.copy(alpha = 0.2f),
            radius = size.minDimension * 0.15f,
            center = Offset(x = size.width * 0.75f, y = size.height * 0.35f)
        )
    }
}

/**
 * Illustration used for stall focused content so that each stall type feels bespoke.
 */
@Composable
fun StallBadgeIllustration(
    modifier: Modifier = Modifier,
    stallType: StallType
) {
    val background = when (stallType) {
        StallType.TEA -> listOf(Color(0xFF4DD0E1), Color(0xFF00838F))
        StallType.DOSA -> listOf(Color(0xFFFFB74D), Color(0xFFF57C00))
        StallType.MOMOS -> listOf(Color(0xFFF48FB1), Color(0xFFC2185B))
        StallType.JUICE -> listOf(Color(0xFF81C784), Color(0xFF388E3C))
    }
    Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(background.first(), background.last(), Color.Black.copy(alpha = 0.2f)),
                center = center,
                radius = size.minDimension / 2f
            ),
            radius = size.minDimension / 2f
        )

        val cupColor = Color.White
        val accent = Color(0xFFFFF176)
        val baseWidth = size.width * 0.5f
        val baseHeight = size.height * 0.5f
        val baseTop = Offset(x = size.width * 0.25f, y = size.height * 0.35f)
        drawRoundRect(
            color = cupColor.copy(alpha = 0.92f),
            topLeft = baseTop,
            size = Size(width = baseWidth, height = baseHeight),
            cornerRadius = CornerRadius(x = baseWidth * 0.18f, y = baseWidth * 0.18f)
        )
        drawRoundRect(
            color = cupColor,
            topLeft = Offset(x = size.width * 0.3f, y = size.height * 0.32f),
            size = Size(width = baseWidth * 0.6f, height = baseHeight * 0.15f),
            cornerRadius = CornerRadius(baseWidth * 0.2f)
        )
        // Straw / steam based on stall type for variation
        when (stallType) {
            StallType.TEA -> {
                drawLine(
                    color = accent,
                    strokeWidth = size.minDimension * 0.03f,
                    start = Offset(x = size.width * 0.45f, y = size.height * 0.32f),
                    end = Offset(x = size.width * 0.4f, y = size.height * 0.12f)
                )
                drawLine(
                    color = accent.copy(alpha = 0.7f),
                    strokeWidth = size.minDimension * 0.02f,
                    start = Offset(x = size.width * 0.55f, y = size.height * 0.33f),
                    end = Offset(x = size.width * 0.6f, y = size.height * 0.15f)
                )
            }
            StallType.DOSA -> {
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(x = size.width * 0.33f, y = size.height * 0.28f),
                    size = Size(width = baseWidth * 0.55f, height = baseHeight * 0.18f),
                    cornerRadius = CornerRadius(baseWidth * 0.2f)
                )
            }
            StallType.MOMOS -> {
                val dumpling = Path().apply {
                    moveTo(size.width * 0.33f, size.height * 0.55f)
                    cubicTo(
                        size.width * 0.4f,
                        size.height * 0.42f,
                        size.width * 0.6f,
                        size.height * 0.42f,
                        size.width * 0.67f,
                        size.height * 0.55f
                    )
                    lineTo(size.width * 0.67f, size.height * 0.6f)
                    lineTo(size.width * 0.33f, size.height * 0.6f)
                    close()
                }
                drawPath(path = dumpling, color = accent.copy(alpha = 0.8f))
            }
            StallType.JUICE -> {
                drawLine(
                    color = accent,
                    strokeWidth = size.minDimension * 0.035f,
                    start = Offset(x = size.width * 0.45f, y = size.height * 0.15f),
                    end = Offset(x = size.width * 0.58f, y = size.height * 0.34f)
                )
                drawCircle(
                    color = accent.copy(alpha = 0.75f),
                    radius = size.minDimension * 0.08f,
                    center = Offset(x = size.width * 0.62f, y = size.height * 0.28f)
                )
            }
        }
    }
}
