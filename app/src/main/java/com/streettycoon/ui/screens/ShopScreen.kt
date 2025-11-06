package com.streettycoon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.streettycoon.ui.GameViewModel
import com.streettycoon.ui.components.InfoCard
import com.streettycoon.ui.components.PremiumCard
import com.streettycoon.ui.components.PrimaryButton
import com.streettycoon.ui.theme.Spacing

@Composable
fun ShopScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl)
    ) {
        item {
            Text(
                "Shop",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            DailyRewardCard(
                currentDay = gameState?.currentDay ?: 1,
                onClaim = { viewModel.claimDailyReward() }
            )
        }

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
    PremiumCard(
        title = "Daily Reward"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CardGiftcard,
                        contentDescription = null,
                        modifier = Modifier.size(Spacing.huge)
                    )
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Text(
                        "Day $currentDay streak",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text("Reward: ${50 * currentDay}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
            PrimaryButton(
                onClick = onClaim,
                text = "Claim"
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = Spacing.md)
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
    InfoCard(
        title = title,
        subtitle = description
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(Spacing.huge)
            )
            Spacer(modifier = Modifier.width(Spacing.xl))
            PrimaryButton(
                onClick = onClick,
                text = price
            )
        }
    }
}
