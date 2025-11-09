package com.streettycoon.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streettycoon.game.model.GameState
import com.streettycoon.game.model.Stall
import com.streettycoon.game.model.StallType
import com.streettycoon.game.model.Zone
import com.streettycoon.ui.GameViewModel
import com.streettycoon.ui.components.AnimatedAuroraBackground
import com.streettycoon.ui.navigation.formatCash
import com.streettycoon.ui.utils.rememberWindowSize
import com.streettycoon.ui.utils.WindowSize
import com.streettycoon.ui.utils.getResponsivePadding

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapScreen(
    viewModel: GameViewModel,
    onStallClick: (Int) -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val offlineEarnings by viewModel.offlineEarnings.collectAsState()

    AnimatedAuroraBackground(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                EnhancedLoadingState()
            }
            gameState != null -> {
                val currentState = gameState ?: return@AnimatedAuroraBackground
                EnhancedMapContent(currentState, offlineEarnings, viewModel, onStallClick)
            }
            else -> {
                EnhancedErrorState()
            }
        }
    }
}

@Composable
private fun EnhancedLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Loading game data, please wait"
                liveRegion = LiveRegionMode.Polite
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFF1A1A2E).copy(alpha = 0.9f),
            shape = RoundedCornerShape(32.dp),
            tonalElevation = 16.dp,
            modifier = Modifier
                .padding(32.dp)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF6B35).copy(alpha = 0.8f),
                            Color(0xFFF7931E).copy(alpha = 0.8f),
                            Color(0xFFFFD23F).copy(alpha = 0.8f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Animated loading icon
                val infiniteTransition = rememberInfiniteTransition(label = "loading")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "loading_scale"
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD23F).copy(alpha = 0.8f),
                                    Color(0xFFFF6B35).copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏙️",
                        fontSize = 40.sp,
                        modifier = Modifier.shadow(8.dp, ambientColor = Color(0xFFFFD23F))
                    )
                }

                Text(
                    text = "Loading Your Street Empire...",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color(0xFFFFD23F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.shadow(4.dp)
                )

                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFFD23F),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun EnhancedErrorState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFF1A1A2E).copy(alpha = 0.9f),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 12.dp,
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "❌",
                    fontSize = 48.sp,
                    modifier = Modifier.shadow(8.dp)
                )
                Text(
                    text = "Failed to Load Game Data",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFFF6B35),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Please try restarting the app",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EnhancedMapContent(
    gameState: GameState,
    offlineEarnings: Double,
    viewModel: GameViewModel,
    onStallClick: (Int) -> Unit
) {
    val windowSize = rememberWindowSize()
    val isCompact = windowSize == WindowSize.Compact
    val responsivePadding = getResponsivePadding()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(responsivePadding),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 16.dp else 20.dp)
    ) {
        // Offline earnings notification
        if (offlineEarnings > 0) {
            item {
                EnhancedOfflineEarningsCard(
                    earnings = offlineEarnings,
                    onDismiss = { viewModel.dismissOfflineEarnings() }
                )
            }
        }

        // Enhanced income summary
        item {
            EnhancedIncomeSummaryCard(gameState)
        }

        // Zone grid with enhanced cards - responsive columns
        val zonesPerRow = when (windowSize) {
            WindowSize.Compact -> 2
            WindowSize.Medium -> 3
            WindowSize.Expanded -> 4
        }
        val zoneHeight = when (windowSize) {
            WindowSize.Compact -> 160.dp
            WindowSize.Medium -> 180.dp
            WindowSize.Expanded -> 200.dp
        }

        items(
            items = gameState.zones.chunked(zonesPerRow),
            key = { zonePair -> zonePair.map { it.id }.joinToString("-") }
        ) { zonePair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(zoneHeight),
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 16.dp)
            ) {
                zonePair.forEachIndexed { index, zone ->
                    val zoneIndex = gameState.zones.indexOf(zone)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        EnhancedZoneCard(
                            zone = zone,
                            zoneIndex = zoneIndex,
                            stalls = gameState.stalls.filter { it.zoneId == zone.id },
                            onStallClick = onStallClick,
                            onUnlockZone = { viewModel.unlockZone(zone.id) },
                            isCompact = isCompact
                        )
                    }
                }
                // Add empty space for incomplete rows
                repeat(zonesPerRow - zonePair.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EnhancedOfflineEarningsCard(earnings: Double, onDismiss: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "earnings_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "earnings_glow_alpha"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        color = Color(0xFF1A1A2E).copy(alpha = 0.95f),
        tonalElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4CAF50).copy(alpha = glowAlpha),
                            Color(0xFF8BC34A).copy(alpha = glowAlpha * 0.8f),
                            Color(0xFFCDDC39).copy(alpha = glowAlpha * 0.6f)
                        )
                    )
                )
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4CAF50).copy(alpha = 0.9f),
                            Color(0xFF8BC34A).copy(alpha = 0.9f),
                            Color(0xFFCDDC39).copy(alpha = 0.9f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💰",
                        fontSize = 32.sp,
                        modifier = Modifier.shadow(8.dp, ambientColor = Color(0xFF4CAF50))
                    )
                    Column {
                        Text(
                            text = "OFFLINE EARNINGS!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.shadow(4.dp)
                        )
                        Text(
                            text = "₹${formatCash(earnings)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD23F),
                            modifier = Modifier.shadow(6.dp, ambientColor = Color(0xFFFFD23F))
                        )
                    }
                }

                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onDismiss),
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedIncomeSummaryCard(gameState: GameState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        color = Color(0xFF0D0D0D).copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E).copy(alpha = 0.9f),
                            Color(0xFF16213E).copy(alpha = 0.8f),
                            Color(0xFF0F3460).copy(alpha = 0.7f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF6B35).copy(alpha = 0.8f),
                            Color(0xFFF7931E).copy(alpha = 0.8f),
                            Color(0xFFFFD23F).copy(alpha = 0.8f),
                            Color(0xFF4CAF50).copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "📊",
                        fontSize = 24.sp,
                        modifier = Modifier.shadow(6.dp, ambientColor = Color(0xFFFFD23F))
                    )
                    Text(
                        text = "GAME STATS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD23F),
                        modifier = Modifier.shadow(4.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EnhancedStatColumn(
                        icon = "💰",
                        label = "Income/sec",
                        value = "₹${formatCash(gameState.getTotalIncomePerSecond())}/s",
                        valueColor = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    EnhancedStatColumn(
                        icon = "👥",
                        label = "Customers",
                        value = gameState.totalCustomersServed.toString(),
                        valueColor = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                    EnhancedStatColumn(
                        icon = "🏆",
                        label = "Earnings",
                        value = "₹${formatCash(gameState.totalEarnings)}",
                        valueColor = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedStatColumn(
    icon: String,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier.shadow(4.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.shadow(3.dp, ambientColor = valueColor)
        )
    }
}

@Composable
private fun EnhancedZoneCard(
    zone: Zone,
    zoneIndex: Int,
    stalls: List<Stall>,
    onStallClick: (Int) -> Unit,
    onUnlockZone: () -> Unit,
    isCompact: Boolean = false
) {
    val isUnlocked = zone.isUnlocked
    val progress = getZoneProgress(zone, stalls)

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "zone_scale"
    )

    val zoneColors = getEnhancedZoneColors(zoneIndex)

    val cornerRadius = if (isCompact) 16.dp else 20.dp
    val borderWidth = if (isUnlocked) (if (isCompact) 2.dp else 3.dp) else (if (isCompact) 1.dp else 2.dp)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(cornerRadius))
            .shadow(
                elevation = if (isUnlocked) (if (isCompact) 10.dp else 12.dp) else (if (isCompact) 6.dp else 8.dp),
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = zoneColors.primary.copy(alpha = 0.3f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (!isUnlocked) {
                        onUnlockZone()
                    } else {
                        stalls.firstOrNull()?.let { onStallClick(it.id) }
                    }
                }
            ),
        color = if (isUnlocked) Color(0xFF1A1A2E).copy(alpha = 0.9f) else Color(0xFF0D0D0D).copy(alpha = 0.8f),
        tonalElevation = if (isUnlocked) (if (isCompact) 6.dp else 8.dp) else (if (isCompact) 3.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (isUnlocked) {
                        Brush.verticalGradient(
                            colors = listOf(
                                zoneColors.primary.copy(alpha = 0.2f),
                                zoneColors.secondary.copy(alpha = 0.15f),
                                zoneColors.accent.copy(alpha = 0.1f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2A2A2A).copy(alpha = 0.3f),
                                Color(0xFF1A1A1A).copy(alpha = 0.5f)
                            )
                        )
                    }
                )
                .border(
                    width = borderWidth,
                    brush = if (isUnlocked) {
                        Brush.linearGradient(colors = zoneColors.borderColors)
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF666666).copy(alpha = 0.5f),
                                Color(0xFF444444).copy(alpha = 0.5f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(cornerRadius)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isCompact) 12.dp else 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Zone header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (isCompact) 3.dp else 4.dp)
                ) {
                    Text(
                        text = getZoneEmoji(zoneIndex),
                        fontSize = if (isCompact) 24.sp else 28.sp,
                        modifier = Modifier.shadow(if (isCompact) 4.dp else 6.dp, ambientColor = zoneColors.primary)
                    )
                    Text(
                        text = zone.name.uppercase(),
                        fontSize = if (isCompact) 12.sp else 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.shadow(if (isUnlocked) 3.dp else 0.dp)
                    )
                    Text(
                        text = getZoneRegion(zoneIndex),
                        fontSize = if (isCompact) 9.sp else 10.sp,
                        color = if (isUnlocked) zoneColors.primary else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Progress or lock indicator
                if (!isUnlocked) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
                        )
                        Text(
                            text = "₹${formatCash(zone.unlockCost)}",
                            fontSize = if (isCompact) 11.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD23F),
                            modifier = Modifier.shadow(2.dp)
                        )
                    }
                } else {
                    // Progress indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(if (isCompact) 3.dp else 4.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(if (isCompact) 3.dp else 4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = zoneColors.primary,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = if (isCompact) 9.sp else 10.sp,
                            color = zoneColors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun getZoneEmoji(index: Int): String {
    return when (index) {
        0 -> "🏙️"  // Downtown
        1 -> "🏔️"  // North
        2 -> "🌅"  // East
        3 -> "🏛️"  // Central
        4 -> "🌆"  // West
        else -> "🏙️" // Default
    }
}

private data class ZoneColorScheme(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val borderColors: List<Color>
)

private fun getEnhancedZoneColors(index: Int): ZoneColorScheme {
    return when (index) {
        0 -> ZoneColorScheme( // Downtown - Blue theme
            primary = Color(0xFF2196F3),
            secondary = Color(0xFF00BCD4),
            accent = Color(0xFF009688),
            borderColors = listOf(Color(0xFF2196F3), Color(0xFF00BCD4), Color(0xFF009688))
        )
        1 -> ZoneColorScheme( // North - Purple theme
            primary = Color(0xFF9C27B0),
            secondary = Color(0xFFBA68C8),
            accent = Color(0xFFE91E63),
            borderColors = listOf(Color(0xFF9C27B0), Color(0xFFBA68C8), Color(0xFFE91E63))
        )
        2 -> ZoneColorScheme( // East - Green theme
            primary = Color(0xFF4CAF50),
            secondary = Color(0xFF8BC34A),
            accent = Color(0xFFCDDC39),
            borderColors = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39))
        )
        3 -> ZoneColorScheme( // Central - Orange theme
            primary = Color(0xFFFF9800),
            secondary = Color(0xFFFFC107),
            accent = Color(0xFFFFEB3B),
            borderColors = listOf(Color(0xFFFF9800), Color(0xFFFFC107), Color(0xFFFFEB3B))
        )
        4 -> ZoneColorScheme( // West - Pink theme
            primary = Color(0xFFE91E63),
            secondary = Color(0xFFF06292),
            accent = Color(0xFFF8BBD9),
            borderColors = listOf(Color(0xFFE91E63), Color(0xFFF06292), Color(0xFFF8BBD9))
        )
        else -> ZoneColorScheme( // Default - Gold theme
            primary = Color(0xFFFFD23F),
            secondary = Color(0xFFFFC107),
            accent = Color(0xFFFFEB3B),
            borderColors = listOf(Color(0xFFFFD23F), Color(0xFFFFC107), Color(0xFFFFEB3B))
        )
    }
}

fun getZoneRegion(index: Int): String {
    return when (index) {
        0 -> "Downtown"
        1 -> "North"
        2 -> "East"
        3 -> "Central"
        4 -> "West"
        else -> "Uptown"
    }
}

fun getZoneProgress(zone: Zone, stalls: List<Stall>): Float {
    if (!zone.isUnlocked) return 0f
    if (stalls.isEmpty()) return 1f

    val unlockedStalls = stalls.count { it.isUnlocked }
    return unlockedStalls.toFloat() / stalls.size.toFloat()
}
