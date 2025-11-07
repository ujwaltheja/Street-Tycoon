package com.streettycoon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.streettycoon.game.model.GameState
import com.streettycoon.game.model.Stall
import com.streettycoon.game.model.StallType
import com.streettycoon.game.model.Zone
import com.streettycoon.ui.GameViewModel
import com.streettycoon.ui.components.InfoCard
import com.streettycoon.ui.components.PrimaryButton
import com.streettycoon.ui.navigation.formatCash
import com.streettycoon.ui.theme.Colors
import com.streettycoon.ui.theme.Spacing

@Composable
fun MapScreen(
    viewModel: GameViewModel,
    onStallClick: (Int) -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val offlineEarnings by viewModel.offlineEarnings.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFF8E1),
                        Color(0xFFFFE0B2)
                    )
                )
            )
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            gameState != null -> {
                val currentState = gameState ?: return@Box
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xl)
                ) {
                    if (offlineEarnings > 0) {
                        item {
                            OfflineEarningsCard(
                                earnings = offlineEarnings,
                                onDismiss = { viewModel.dismissOfflineEarnings() }
                            )
                        }
                    }

                    item {
                        IncomeSummaryCard(currentState)
                    }

                    // Zone cards with 2-column grid layout (from HTML)
                    items(currentState.zones.chunked(2)) { zonePair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            zonePair.forEachIndexed { index, zone ->
                                val zoneIndex = currentState.zones.indexOf(zone)
                                Box(modifier = Modifier.weight(1f)) {
                                    ZoneCardGlossy(
                                        zoneName = zone.name,
                                        zoneRegion = getZoneRegion(zoneIndex),
                                        isLocked = !zone.isUnlocked,
                                        progress = getZoneProgress(zone, currentState.stalls.filter { it.zoneId == zone.id }),
                                        backgroundColor = getZoneBackgroundColor(zoneIndex),
                                        borderColor = getZoneBorderColor(zoneIndex),
                                        onClick = {
                                            if (!zone.isUnlocked) {
                                                viewModel.unlockZone(zone.id)
                                            } else {
                                                // Navigate to first stall in zone
                                                currentState.stalls.firstOrNull { it.zoneId == zone.id }?.let {
                                                    onStallClick(it.id)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                            // Add empty box if odd number of zones
                            if (zonePair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Failed to load game data",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
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

fun getZoneBackgroundColor(index: Int): Color {
    return when (index) {
        0 -> Color(0xFFE3F2FD)  // Blue
        1 -> Color(0xFFF3E5F5)  // Purple
        2 -> Color(0xFFE8F5E9)  // Green
        3 -> Color(0xFFFFF3E0)  // Orange
        4 -> Color(0xFFFCE4EC)  // Pink
        else -> Color(0xFFFFF9C4)  // Yellow
    }
}

fun getZoneBorderColor(index: Int): Color {
    return when (index) {
        0 -> Color(0xFF64B5F6)
        1 -> Color(0xFFBA68C8)
        2 -> Color(0xFF81C784)
        3 -> Color(0xFFFFB74D)
        4 -> Color(0xFFF06292)
        else -> Color(0xFFFFD54F)
    }
}

fun getZoneProgress(zone: Zone, stalls: List<Stall>): Float {
    if (!zone.isUnlocked) return 0f
    if (stalls.isEmpty()) return 1f

    val unlockedStalls = stalls.count { it.isUnlocked }
    return unlockedStalls.toFloat() / stalls.size.toFloat()
}

@Composable
fun OfflineEarningsCard(earnings: Double, onDismiss: () -> Unit) {
    InfoCard(
        title = "Offline Earnings",
        subtitle = "You earned ₹${formatCash(earnings)} while away!"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    }
}

@Composable
fun IncomeSummaryCard(gameState: GameState) {
    GlossyCard(
        backgroundColor = Color(0xFFFFFFFF),
        elevation = 8.dp
    ) {
        Text(
            text = "Game Stats",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Colors.TextPrimary
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Income/sec", style = MaterialTheme.typography.bodySmall, color = Colors.TextSecondary)
                Text(
                    "₹${formatCash(gameState.getTotalIncomePerSecond())}/s",
                    style = MaterialTheme.typography.titleMedium,
                    color = Colors.OrangePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(Spacing.xl))
            Column {
                Text("Customers Served", style = MaterialTheme.typography.bodySmall, color = Colors.TextSecondary)
                Text(
                    "${gameState.totalCustomersServed}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Colors.TextPrimary
                )
            }
        }
    }
}

@Composable
fun ZoneCard(
    zone: Zone,
    stalls: List<Stall>,
    onStallClick: (Int) -> Unit,
    onUnlockZone: () -> Unit
) {
    InfoCard(
        title = zone.name,
        subtitle = if (zone.isUnlocked) "Stalls" else "Locked"
    ) {
        Column(modifier = Modifier.padding(top = Spacing.lg)) {
            if (!zone.isUnlocked) {
                PrimaryButton(onClick = onUnlockZone, text = "Unlock: ₹${formatCash(zone.unlockCost)}")
            } else {
                stalls.forEach { stall ->
                    StallItem(stall = stall, onClick = { onStallClick(stall.id) })
                    Spacer(modifier = Modifier.height(Spacing.md))
                }
            }
        }
    }
}

@Composable
fun StallItem(stall: Stall, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (stall.isUnlocked)
                    Colors.GreenPrimaryContainer
                else
                    Colors.LockedGray.copy(alpha = 0.3f)
            )
            .clickable(enabled = stall.isUnlocked, onClick = onClick)
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = getStallTypeName(stall.type),
                fontWeight = FontWeight.SemiBold
            )
            if (stall.isUnlocked) {
                Text(
                    text = "Level ${stall.level} • ${stall.helpers.size} helpers",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = "Locked",
                    style = MaterialTheme.typography.bodySmall,
                    color = Colors.LockedGray
                )
            }
        }

        if (stall.isUnlocked) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Open")
        } else {
            Icon(Icons.Default.Lock, contentDescription = "Locked")
        }
    }
}

fun getStallTypeName(type: StallType): String {
    return when (type) {
        StallType.TEA -> "☕ Tea Stall"
        StallType.DOSA -> "🥞 Dosa Stall"
        StallType.MOMOS -> "🥟 Momos Stall"
        StallType.JUICE -> "🧃 Juice Stall"
    }
}
