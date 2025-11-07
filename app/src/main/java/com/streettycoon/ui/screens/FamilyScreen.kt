package com.streettycoon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streettycoon.game.model.FamilyState
import com.streettycoon.game.model.GameState
import com.streettycoon.game.model.SpendingCategory
import com.streettycoon.ui.components.InfoCard
import com.streettycoon.ui.components.PremiumCard
import com.streettycoon.ui.components.PrimaryButton
import com.streettycoon.ui.components.AnimatedAuroraBackground
import com.streettycoon.ui.theme.Colors
import com.streettycoon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyDashboardScreen(
    gameState: GameState,
    onNavigateBack: () -> Unit,
    onUpgradeCategory: (String) -> Unit,
    onGetMarried: (String) -> Unit,
    onHaveBaby: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val familyState = gameState.familyState
    val monthlyIncome = gameState.getMonthlyIncomeEstimate()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family & Life") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Colors.OrangePrimary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    ) { padding ->
        AnimatedAuroraBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.xl)
            ) {
            item {
                FamilyMetricsCard(
                    familyState = familyState,
                    monthlyIncome = monthlyIncome
                )
            }

            item {
                Text(
                    text = "Spending Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(familyState.categories) { category ->
                SpendingCategoryCard(
                    category = category,
                    playerCash = gameState.playerCash,
                    onUpgrade = { onUpgradeCategory(category.categoryId) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = "Life Events",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!familyState.isMarried) {
                item {
                    LifeEventCard(
                        icon = "💍",
                        title = "Get Married",
                        description = "Start a family together",
                        cost = "₹10,000 + ₹500/mo",
                        enabled = gameState.playerCash >= 10000,
                        onClick = { onGetMarried("Priya") }
                    )
                }
            }

            if (familyState.isMarried) {
                item {
                    LifeEventCard(
                        icon = "👶",
                        title = "Have a Baby",
                        description = "Grow your family",
                        cost = "₹5,000 + ₹1,500/mo",
                        enabled = gameState.playerCash >= 5000,
                        onClick = { onHaveBaby("Arjun") }
                    )
                }
            }

            if (familyState.members.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        text = "Family Members",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(familyState.members) { member ->
                    FamilyMemberCard(member)
                }
            }
            }
        }
    }
}

@Composable
private fun FamilyMetricsCard(
    familyState: FamilyState,
    monthlyIncome: Double
) {
    PremiumCard(
        title = "Family Happiness"
    ) {
        Column(
            modifier = Modifier.padding(top = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "${familyState.averageHappiness.toInt()}%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Colors.GreenPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn("Members", familyState.getMemberCount().toString())
                MetricColumn("Monthly", "₹${(monthlyIncome / 1000).toInt()}k")
                MetricColumn("Expenses", "₹${familyState.totalMonthlyExpense.toInt()}")
            }
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SpendingCategoryCard(
    category: SpendingCategory,
    playerCash: Double,
    onUpgrade: () -> Unit
) {
    val canUpgrade = category.canUpgrade() && playerCash >= category.nextUpgradeCost

    InfoCard(
        title = category.name,
        subtitle = category.currentItem
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Colors.GreenPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.getEmoji(),
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.lg))

                Column {
                    Text(
                        text = "₹${category.monthlyExpense.toInt()}/mo",
                        style = MaterialTheme.typography.bodySmall,
                        color = Colors.LockedGray
                    )
                }
            }

            if (category.canUpgrade()) {
                PrimaryButton(
                    onClick = onUpgrade,
                    enabled = canUpgrade,
                    text = "Upgrade: ₹${category.nextUpgradeCost.toInt()}"
                )
            } else {
                Text("MAX", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LifeEventCard(
    icon: String,
    title: String,
    description: String,
    cost: String,
    enabled: Boolean,
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 32.sp)

            Spacer(modifier = Modifier.width(Spacing.xl))

            Column(modifier = Modifier.weight(1f)) {
                Text(cost, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }

            PrimaryButton(onClick = onClick, enabled = enabled, text = "Confirm")
        }
    }
}

@Composable
private fun FamilyMemberCard(member: com.streettycoon.game.model.FamilyMember) {
    InfoCard(
        title = member.name,
        subtitle = "${member.relation} • ${member.age} years"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (member.relation) {
                    "player" -> "🧑"
                    "spouse" -> "👩"
                    "child" -> "👶"
                    "parent" -> "👴"
                    else -> "👤"
                },
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.width(Spacing.lg))

            Column {
                Text("Happiness: ${member.happiness.toInt()}%", style = MaterialTheme.typography.bodyMedium)
                if (member.monthlyExpense > 0) {
                    Text("Expense: ₹${member.monthlyExpense.toInt()}/mo", style = MaterialTheme.typography.bodySmall, color = Colors.LockedGray)
                }
            }
        }
    }
}

@Composable
fun FamilyScreen(
    viewModel: com.streettycoon.ui.GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()

    gameState?.let { state ->
        FamilyDashboardScreen(
            gameState = state,
            onNavigateBack = { /* No back navigation needed in main nav */ },
            onUpgradeCategory = { categoryId ->
                viewModel.upgradeCategory(categoryId)
            },
            onGetMarried = { spouseName ->
                viewModel.getMarried(spouseName)
            },
            onHaveBaby = { babyName ->
                viewModel.haveBaby(babyName)
            },
            modifier = modifier
        )
    }
}
