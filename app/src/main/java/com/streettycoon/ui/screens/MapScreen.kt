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

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            gameState != null -> {
                val currentState = gameState ?: return@Box
                Column(modifier = Modifier.fillMaxSize()) {
                    if (offlineEarnings > 0) {
                        OfflineEarningsCard(
                            earnings = offlineEarnings,
                            onDismiss = { viewModel.dismissOfflineEarnings() }
                        )
                    }

                    IncomeSummaryCard(currentState)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.xl),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xl)
                    ) {
                        items(currentState.zones) { zone ->
                            ZoneCard(
                                zone = zone,
                                stalls = currentState.stalls.filter { it.zoneId == zone.id },
                                onStallClick = onStallClick,
                                onUnlockZone = { viewModel.unlockZone(zone.id) }
                            )
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
    InfoCard(
        title = "Game Stats",
        subtitle = ""
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Income/sec", style = MaterialTheme.typography.bodySmall)
                Text(
                    "₹${formatCash(gameState.getTotalIncomePerSecond())}/s",
                    style = MaterialTheme.typography.titleMedium,
                    color = Colors.GreenPrimary
                )
            }
            Spacer(modifier = Modifier.width(Spacing.xl))
            Column {
                Text("Customers Served", style = MaterialTheme.typography.bodySmall)
                Text(
                    "${gameState.totalCustomersServed}",
                    style = MaterialTheme.typography.titleMedium
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
