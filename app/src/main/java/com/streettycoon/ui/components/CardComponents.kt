package com.streettycoon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.streettycoon.ui.theme.Colors
import com.streettycoon.ui.theme.CornerRadius
import com.streettycoon.ui.theme.Elevations
import com.streettycoon.ui.theme.Spacing

// ==================== INFO CARD ====================
@Composable
fun InfoCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(Elevations.Level2, RoundedCornerShape(CornerRadius.Large))
            .border(1.dp, Colors.NeutralSurfaceVariant, RoundedCornerShape(CornerRadius.Large)),
        colors = CardDefaults.cardColors(
            containerColor = Colors.NeutralBackground
        ),
        shape = RoundedCornerShape(CornerRadius.Large)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
            Text(
                text = subtitle,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = Colors.OutlineVariant
            )
            content()
        }
    }
}

// ==================== PREMIUM CARD ====================
@Composable
fun PremiumCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(Elevations.Level4, RoundedCornerShape(CornerRadius.XLarge))
            .border(2.dp, Colors.CurrencyGold, RoundedCornerShape(CornerRadius.XLarge)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFDD7)
        ),
        shape = RoundedCornerShape(CornerRadius.XLarge)
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                color = Colors.CurrencyGold
            )
            content()
        }
    }
}

// ==================== CHARACTER CARD ====================
@Composable
fun CharacterCard(
    name: String,
    level: Int,
    isPremium: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .size(width = 160.dp, height = 200.dp)
            .shadow(Elevations.Level2),
        shape = RoundedCornerShape(CornerRadius.Large),
        colors = CardDefaults.cardColors(
            containerColor = if (isPremium) Color(0xFFFFFDD7) else Colors.NeutralBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Colors.GreenPrimaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("Avatar", color = Colors.GreenOnPrimaryContainer)
            }
            
            Text(text = name, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Text(text = "Level $level", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
    }
}
