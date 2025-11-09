package com.streettycoon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streettycoon.ui.theme.Colors
import com.streettycoon.ui.theme.CornerRadius
import com.streettycoon.ui.theme.Spacing

// ==================== GLOSSY CARD COMPONENT (ENHANCED) ====================
@Composable
fun GlossyCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Colors.CardBackground,
    borderColor: Color = Colors.CardBorder,
    elevation: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    // Shimmer animation for glossy effect
    val infiniteTransition = rememberInfiniteTransition(label = "gloss_shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Card(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(CornerRadius.Large),
                ambientColor = Colors.CardShadow,
                spotColor = Colors.CardShadow
            )
            .shadow(
                elevation = elevation / 2,
                shape = RoundedCornerShape(CornerRadius.Large),
                ambientColor = borderColor.copy(alpha = 0.1f),
                spotColor = borderColor.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(CornerRadius.Large),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    )
                )
                .border(
                    width = 3.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            // Inner glossy highlight at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f + shimmerAlpha),
                                Color.White.copy(alpha = 0.01f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(24.dp),
                content = content
            )
        }
    }
}

// ==================== STALL CONTAINER (from HTML) ====================
@Composable
fun StallContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    // Rotating gradient background animation
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    GlossyCard(
        modifier = modifier,
        backgroundColor = Colors.ComboYellow.copy(alpha = 0.9f), // Yellow from theme
        borderColor = Colors.CurrencyGold,
        elevation = 12.dp
    ) {
        Box {
            // Animated rotating gradient background (simulating HTML rotation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x1AFFD700),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(content = content)
        }
    }
}

// ==================== COMBO COUNTER (ENHANCED with glow) ====================
@Composable
fun ComboCounter(
    comboCount: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (isVisible && comboCount > 0) {
        // Pulsing glow animation
        val infiniteTransition = rememberInfiniteTransition(label = "combo_glow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "combo_glow_alpha"
        )

        Box(
            modifier = modifier
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(CornerRadius.Large),
                    ambientColor = Colors.CurrencyGold.copy(alpha = glowAlpha),
                    spotColor = Colors.CurrencyGold.copy(alpha = glowAlpha + 0.2f)
                )
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(CornerRadius.Large),
                    ambientColor = Colors.ComboYellow.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(CornerRadius.Large))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Colors.ComboYellow,
                            Colors.CurrencyGold
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(CornerRadius.Large)
                )
                .padding(horizontal = Spacing.xl, vertical = Spacing.md)
        ) {
            Text(
                text = "x$comboCount Combo!",
                color = Colors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==================== ZONE CARD (ENHANCED with spotlight and glow) ====================
@Composable
fun ZoneCardGlossy(
    zoneName: String,
    zoneRegion: String,
    isLocked: Boolean,
    progress: Float,
    backgroundColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    zoneIndex: Int = 0
) {
    // Spotlight/glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "zone_glow")
    val spotlightAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spotlight_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isLocked) 4.dp else 8.dp,
                shape = RoundedCornerShape(CornerRadius.Medium),
                ambientColor = borderColor.copy(alpha = spotlightAlpha),
                spotColor = borderColor.copy(alpha = spotlightAlpha + 0.15f)
            )
            .shadow(
                elevation = if (isLocked) 2.dp else 4.dp,
                shape = RoundedCornerShape(CornerRadius.Medium),
                ambientColor = backgroundColor.copy(alpha = 0.05f)
            )
            .clickable(enabled = !isLocked, onClick = onClick),
        shape = RoundedCornerShape(CornerRadius.Medium),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = if (isLocked) 0.6f else 1f)
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            backgroundColor,
                            backgroundColor.copy(alpha = 0.6f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = borderColor.copy(alpha = if (isLocked) 0.5f else 1f),
                    shape = RoundedCornerShape(CornerRadius.Medium)
                )
        ) {
            // Spotlight effect overlay (top-right corner)
            if (!isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(120.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = spotlightAlpha * 0.5f),
                                    Color.Transparent
                                ),
                                radius = 150f
                            )
                        )
                )
            }

            Column(modifier = Modifier.padding(Spacing.xl)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xl),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ZoneBadgeIllustration(
                        modifier = Modifier.size(64.dp),
                        zoneIndex = zoneIndex
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = zoneName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Colors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = zoneRegion,
                            fontSize = 12.sp,
                            color = Colors.TextSecondary
                        )
                    }
                    if (isLocked) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Lock,
                            contentDescription = "Locked",
                            tint = Colors.LockedGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Colors.SuccessGreen,
                                        Colors.ComboYellow
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isLocked) "Tap to unlock" else "${(progress * 100).toInt()}% district control",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = if (isLocked) Colors.LockedGray else Colors.TextPrimary
                )
            }
        }
    }
}

// ==================== CHARACTER CARD (from HTML) ====================
@Composable
fun CharacterCardGlossy(
    characterName: String,
    characterType: String,
    level: Int,
    emoji: String,
    bonus: String,
    xpProgress: Float,
    xpText: String,
    isHired: Boolean,
    onHireClick: (() -> Unit)? = null,
    onLevelUpClick: (() -> Unit)? = null,
    onAssignClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(CornerRadius.Medium)
            ),
        shape = RoundedCornerShape(CornerRadius.Medium),
        colors = CardDefaults.cardColors(
            containerColor = Colors.CardBackground
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Colors.CardBackground,
                            Colors.CreamLight
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = Colors.CardBorder,
                    shape = RoundedCornerShape(CornerRadius.Medium)
                )
        ) {
            Row(
                modifier = Modifier.padding(Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Character avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            ambientColor = Colors.OrangePrimary.copy(alpha = 0.3f)
                        )
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Colors.OrangePrimary,
                                    Colors.OrangeSecondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 40.sp
                    )
                }

                // Character info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = characterName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Colors.TextPrimary
                            )
                            Text(
                                text = characterType,
                                fontSize = 12.sp,
                                color = Colors.TextSecondary
                            )
                        }

                        // Level badge
                        Box(
                            modifier = Modifier
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(CornerRadius.Medium),
                                    ambientColor = Colors.CurrencyGold.copy(alpha = 0.3f)
                                )
                                .clip(RoundedCornerShape(CornerRadius.Medium))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Colors.CurrencyGold,
                                            Colors.OrangePrimary
                                        )
                                    )
                                )
                                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
                        ) {
                            Text(
                                text = "Lv. $level",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Colors.TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = bonus,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Colors.SuccessGreen
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    // XP progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(CornerRadius.Small))
                            .background(Color.Black.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(xpProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(CornerRadius.Small))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Colors.InfoBlue,
                                            Colors.TealLight
                                        )
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Text(
                        text = xpText,
                        fontSize = 11.sp,
                        color = Colors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        if (isHired) {
                            Text(
                                    text = "Hired",
                                modifier = Modifier
                                    .background(
                                        color = Colors.SuccessGreen.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(CornerRadius.Medium)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Colors.SuccessGreen,
                                        shape = RoundedCornerShape(CornerRadius.Medium)
                                    )
                                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Colors.SuccessGreen
                            )

                            if (onLevelUpClick != null) {
                                SmallGlossyButton(
                                    text = "Level Up",
                                    onClick = onLevelUpClick,
                                    backgroundColor = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF2196F3),
                                            Color(0xFF64B5F6)
                                        )
                                    )
                                )
                            }

                            if (onAssignClick != null) {
                                SmallGlossyButton(
                                    text = "Assign",
                                    onClick = onAssignClick,
                                    backgroundColor = Brush.linearGradient(
                                        colors = listOf(
                                            Colors.OrangePrimary,
                                            Colors.OrangeSecondary
                                        )
                                    )
                                )
                            }
                        } else if (onHireClick != null) {
                            SmallGlossyButton(
                                text = "Hire",
                                onClick = onHireClick,
                                backgroundColor = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4CAF50),
                                        Color(0xFF66BB6A)
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmallGlossyButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

// ==================== HELPER: SHIMMER EFFECT ====================
/**
 * Creates a shimmer/gloss effect overlay for premium card appearance
 */
@Composable
fun ShimmerOverlay(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    duration: Int = 3000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_effect")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Box(
        modifier = modifier.background(
            color = color.copy(alpha = shimmerAlpha)
        )
    )
}

// ==================== HELPER: SPOTLIGHT EFFECT ====================
/**
 * Creates a spotlight glow effect at a given corner for depth
 * Position the effect using the modifier parameter (e.g., use in Box context with alignment)
 */
@Composable
fun SpotlightEffect(
    modifier: Modifier = Modifier,
    color: Color = Colors.OrangePrimary,
    intensity: Float = 0.3f,
    duration: Int = 2500
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spotlight")
    val spotlightAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = intensity,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spotlight_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth(0.5f)
            .height(150.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = spotlightAlpha * 0.6f),
                        Color.Transparent
                    ),
                    radius = 200f
                )
            )
    )
}

// ==================== HELPER: PULSING GLOW EFFECT ====================
/**
 * Creates a pulsing glow shadow effect for prominent UI elements
 */
@Composable
fun PulsingGlowModifier(
    modifier: Modifier = Modifier,
    glowColor: Color = Colors.OrangePrimary,
    duration: Int = 1000,
    initialAlpha: Float = 0.3f,
    targetAlpha: Float = 0.7f
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = initialAlpha,
        targetValue = targetAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    return modifier.shadow(
        elevation = 8.dp,
        shape = RoundedCornerShape(12.dp),
        ambientColor = glowColor.copy(alpha = glowAlpha),
        spotColor = glowColor.copy(alpha = glowAlpha + 0.2f)
    )
}
