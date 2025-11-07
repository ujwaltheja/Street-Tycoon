package com.streettycoon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streettycoon.ui.theme.Colors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==================== GLOSSY CARD COMPONENT ====================
@Composable
fun GlossyCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Colors.CardBackground,
    borderColor: Color = Colors.CardBorder,
    elevation: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Colors.CardShadow,
                spotColor = Colors.CardShadow
            ),
        shape = RoundedCornerShape(20.dp),
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
        backgroundColor = Color(0xFFFFF9C4), // Yellow gradient from HTML
        borderColor = Color(0xFFFFD54F),
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

// ==================== LARGE CIRCULAR TAP BUTTON (from HTML) ====================
@Composable
fun TapServeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "TAP"
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tap_scale"
    )

    Box(
        modifier = modifier
            .size(200.dp)
            .shadow(
                elevation = if (isPressed) 8.dp else 16.dp,
                shape = CircleShape,
                ambientColor = Colors.OrangePrimary.copy(alpha = 0.4f),
                spotColor = Colors.OrangePrimary.copy(alpha = 0.4f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Colors.OrangePrimary,
                        Colors.OrangeSecondary,
                        Colors.OrangeAccent
                    )
                )
            )
            .border(
                width = 6.dp,
                color = Colors.CurrencyGold,
                shape = CircleShape
            )
            .clickable {
                isPressed = true
                onClick()
                // Reset pressed state after animation
                scope.launch {
                    delay(150)
                    isPressed = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

// ==================== COMBO COUNTER (from HTML) ====================
@Composable
fun ComboCounter(
    comboCount: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (isVisible && comboCount > 0) {
        Box(
            modifier = modifier
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color(0x669C27B0),
                    spotColor = Color(0x669C27B0)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9C27B0),
                            Color(0xFFBA68C8)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "x$comboCount Combo!",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==================== ZONE CARD (from HTML) ====================
@Composable
fun ZoneCardGlossy(
    zoneName: String,
    zoneRegion: String,
    isLocked: Boolean,
    progress: Float,
    backgroundColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isLocked) 4.dp else 8.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isLocked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
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
                            backgroundColor.copy(alpha = 0.7f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = zoneName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (backgroundColor) {
                                Color(0xFFE3F2FD) -> Color(0xFF1565C0)
                                Color(0xFFF3E5F5) -> Color(0xFF7B1FA2)
                                Color(0xFFE8F5E9) -> Color(0xFF388E3C)
                                Color(0xFFFFF3E0) -> Color(0xFFF57C00)
                                Color(0xFFFCE4EC) -> Color(0xFFC2185B)
                                else -> Color(0xFFF9A825)
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = zoneRegion,
                            fontSize = 12.sp,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                    }

                    if (isLocked) {
                        Text(
                            text = "=",
                            fontSize = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4CAF50),
                                        Color(0xFF66BB6A)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isLocked) "Locked" else "${(progress * 100).toInt()}% Unlocked",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF5F5F5)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                                color = Color(0xFF212121)
                            )
                            Text(
                                text = characterType,
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        // Level badge
                        Box(
                            modifier = Modifier
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    ambientColor = Color(0x4DFFD700)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFFD700),
                                            Color(0xFFFFA000)
                                        )
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Lv. $level",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = bonus,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // XP progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.Black.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(xpProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF2196F3),
                                            Color(0xFF64B5F6)
                                        )
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = xpText,
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isHired) {
                            Text(
                                text = " Hired",
                                modifier = Modifier
                                    .background(
                                        color = Color(0x1A4CAF50),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFF81C784),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF388E3C)
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
