package com.streettycoon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.streettycoon.ui.GameViewModel
import com.streettycoon.ui.navigation.formatCash

@Composable
fun ShopScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                "Shop",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Daily Reward
        item {
            DailyRewardCard(
                currentDay = gameState?.currentDay ?: 1,
                onClaim = { viewModel.claimDailyReward() }
            )
        }

        // Token Packs
        item {
            SectionHeader("Token Packs")
        }

        item {
            ShopItem(
                icon = Icons.Default.MonetizationOn,
                title = "Small Token Pack",
                description = "100 tokens",
                price = "$0.99",
                onClick = { /* TODO: IAP */ }
            )
        }

        item {
            ShopItem(
                icon = Icons.Default.MonetizationOn,
                title = "Medium Token Pack",
                description = "500 tokens + 50 bonus",
                price = "$4.99",
                onClick = { /* TODO: IAP */ }
            )
        }

        item {
            ShopItem(
                icon = Icons.Default.MonetizationOn,
                title = "Large Token Pack",
                description = "1200 tokens + 300 bonus",
                price = "$9.99",
                onClick = { /* TODO: IAP */ }
            )
        }

        // Boosters
        item {
            SectionHeader("Boosters")
        }

        item {
            ShopItem(
                icon = Icons.Default.TrendingUp,
                title = "Double Earnings",
                description = "2x earnings for 1 hour",
                price = "Watch Ad",
                onClick = { /* TODO: Rewarded Ad */ }
            )
        }

        item {
            ShopItem(
                icon = Icons.Default.Speed,
                title = "Speed Boost",
                description = "Instant upgrade completion",
                price = "Watch Ad",
                onClick = { /* TODO: Rewarded Ad */ }
            )
        }

        // Cosmetics
        item {
            SectionHeader("Stall Skins")
        }

        item {
            ShopItem(
                icon = Icons.Default.Palette,
                title = "Festival Theme",
                description = "Colorful festival decorations",
                price = "50 tokens",
                onClick = { /* TODO: Cosmetics */ }
            )
        }

        item {
            ShopItem(
                icon = Icons.Default.Palette,
                title = "Modern Theme",
                description = "Sleek modern stall design",
                price = "100 tokens",
                onClick = { /* TODO: Cosmetics */ }
            )
        }
    }
}

@Composable
fun DailyRewardCard(currentDay: Int, onClaim: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CardGiftcard,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Daily Reward",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Day $currentDay streak",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Reward: ₹${formatCash(50.0 * currentDay)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Claim")
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ShopItem(
    icon: ImageVector,
    title: String,
    description: String,
    price: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(price)
            }
        }
    }
}
