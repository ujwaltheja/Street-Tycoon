package com.streettycoon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streettycoon.game.model.FamilyState
import com.streettycoon.game.model.GameState
import com.streettycoon.game.model.SpendingCategory
import androidx.compose.ui.window.Dialog
import com.streettycoon.ui.components.PremiumCard
import com.streettycoon.ui.components.FinancialHealthIndicator
import com.streettycoon.ui.components.AnimatedMoneyCounter

/**
 * Family dashboard screen showing members, spending, and life events
 */
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
                    containerColor = Color(0xFF1565C0)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Key Metrics Card
            item {
                FamilyMetricsCard(
                    familyState = familyState,
                    monthlyIncome = monthlyIncome,
                    playerCash = gameState.playerCash
                )
            }

            // Spending Categories
            item {
                Text(
                    text = "Spending Categories",
                    fontSize = 18.sp,
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

            // Life Events
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Life Events",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Marriage
            if (!familyState.isMarried) {
                item {
                    LifeEventCard(
                        icon = "💍",
                        title = "Get Married",
                        description = "Start a family together",
                        cost = "₹10,000 + ₹500/mo",
                        enabled = gameState.playerCash >= 10000,
                        onClick = { onGetMarried("Priya") }  // Default name
                    )
                }
            }

            // Baby
            if (familyState.isMarried) {
                item {
                    LifeEventCard(
                        icon = "👶",
                        title = "Have a Baby",
                        description = "Grow your family",
                        cost = "₹5,000 + ₹1,500/mo",
                        enabled = gameState.playerCash >= 5000,
                        onClick = { onHaveBaby("Arjun") }  // Default name
                    )
                }
            }

            // Family Members List
            if (familyState.members.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Family Members",
                        fontSize = 18.sp,
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

/**
 * Card showing key family metrics
 */
@Composable
private fun FamilyMetricsCard(
    familyState: FamilyState,
    monthlyIncome: Double,
    playerCash: Double
) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 8f
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Family Happiness",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                "${familyState.averageHappiness.toInt()}%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn("Members", familyState.getMemberCount().toString())
                MetricColumn("Monthly", "₹${(monthlyIncome / 1000).toInt()}k")
                MetricColumn("Expenses", "₹${familyState.totalMonthlyExpense.toInt()}")
            }

            // Financial health indicator with premium component
            Spacer(modifier = Modifier.height(16.dp))

            val expenseRatio = familyState.getExpenseRatio(monthlyIncome).toFloat()

            FinancialHealthIndicator(
                expenseRatio = expenseRatio,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Individual metric in the metrics card
 */
@Composable
private fun MetricColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

/**
 * Card for displaying a spending category
 */
@Composable
private fun SpendingCategoryCard(
    category: SpendingCategory,
    playerCash: Double,
    onUpgrade: () -> Unit
) {
    val canUpgrade = category.canUpgrade() && playerCash >= category.nextUpgradeCost

    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 6f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon and Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(category.getLevelColor().copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.getEmoji(),
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = category.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = category.currentItem,
                        fontSize = 12.sp,
                        color = Color(0xFF616161)
                    )
                    Text(
                        text = "₹${category.monthlyExpense.toInt()}/mo",
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            // Level Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index <= category.level) category.getLevelColor()
                                else Color(0xFFE0E0E0)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Upgrade Button
            if (category.canUpgrade()) {
                Button(
                    onClick = onUpgrade,
                    enabled = canUpgrade,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        disabledContainerColor = Color(0xFFE0E0E0)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Upgrade", fontSize = 11.sp)
                        Text("₹${category.nextUpgradeCost.toInt()}", fontSize = 10.sp)
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "MAX",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Card for life events (marriage, baby)
 */
@Composable
private fun LifeEventCard(
    icon: String,
    title: String,
    description: String,
    cost: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (enabled) onClick else null,
        elevation = 6f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = icon,
                    fontSize = 32.sp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (enabled) Color(0xFF212121) else Color(0xFF9E9E9E)
                    )
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = if (enabled) Color(0xFF616161) else Color(0xFFBDBDBD)
                    )
                    Text(
                        text = cost,
                        fontSize = 11.sp,
                        color = if (enabled) Color(0xFF4CAF50) else Color(0xFFE53935)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (enabled) Color(0xFFFFA726) else Color(0xFFE0E0E0),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Card for displaying a family member
 */
@Composable
private fun FamilyMemberCard(member: com.streettycoon.game.model.FamilyMember) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 6f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar based on relation
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

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = member.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${member.relation} • ${member.age} years",
                        fontSize = 11.sp,
                        color = Color(0xFF616161)
                    )
                    if (member.monthlyExpense > 0) {
                        Text(
                            text = "₹${member.monthlyExpense.toInt()}/mo",
                            fontSize = 10.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }

            // Happiness indicator
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${member.happiness.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        member.happiness > 75f -> Color(0xFF4CAF50)
                        member.happiness > 50f -> Color(0xFFFFA726)
                        else -> Color(0xFFE53935)
                    }
                )
                Text(
                    text = when {
                        member.happiness > 75f -> "Happy"
                        member.happiness > 50f -> "Okay"
                        else -> "Unhappy"
                    },
                    fontSize = 10.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

/**
 * Main Family screen entry point for navigation
 */
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
