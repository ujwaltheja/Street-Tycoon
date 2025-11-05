package com.streettycoon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.streettycoon.game.model.GameState
import com.streettycoon.game.model.Stall
import com.streettycoon.game.model.StallType
import com.streettycoon.game.model.Zone
import com.streettycoon.ui.GameViewModel
import com.streettycoon.ui.navigation.formatCash

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
                Column(modifier = Modifier.fillMaxSize()) {
                    // Offline earnings notification
                    if (offlineEarnings > 0) {
                        OfflineEarningsCard(
                            earnings = offlineEarnings,
                            onDismiss = { viewModel.dismissOfflineEarnings() }
                        )
                    }

                    // Income summary
                    IncomeSummaryCard(gameState!!)

                    // Zones and stalls
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(gameState!!.zones) { zone ->
                            ZoneCard(
                                zone = zone,
                                stalls = gameState!!.stalls.filter { it.zoneId == zone.id },
                                onStallClick = onStallClick,
                                onUnlockZone = { viewModel.unlockZone(zone.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineEarningsCard(earnings: Double, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Offline Earnings", fontWeight = FontWeight.Bold)
                Text("You earned ₹${formatCash(earnings)} while away!")
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    }
}

@Composable
fun IncomeSummaryCard(gameState: GameState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Income/sec", style = MaterialTheme.typography.bodySmall)
                Text(
                    "₹${formatCash(gameState.getTotalIncomePerSecond())}/s",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (zone.isUnlocked)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = zone.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (!zone.isUnlocked) {
                    Button(onClick = onUnlockZone) {
                        Text("Unlock: ₹${formatCash(zone.unlockCost)}")
                    }
                }
            }

            if (zone.isUnlocked) {
                Spacer(modifier = Modifier.height(12.dp))
                stalls.forEach { stall ->
                    StallItem(stall = stall, onClick = { onStallClick(stall.id) })
                    Spacer(modifier = Modifier.height(8.dp))
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
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (stall.isUnlocked)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    Color.Gray.copy(alpha = 0.3f)
            )
            .clickable(enabled = stall.isUnlocked, onClick = onClick)
            .padding(12.dp),
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
                    color = Color.Gray
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
