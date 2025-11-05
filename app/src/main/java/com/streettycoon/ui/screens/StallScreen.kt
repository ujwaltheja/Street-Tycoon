package com.streettycoon.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.streettycoon.game.model.Helper
import com.streettycoon.game.model.Stall
import com.streettycoon.ui.GameViewModel
import com.streettycoon.ui.navigation.formatCash
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StallScreen(
    viewModel: GameViewModel,
    stallId: Int,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val stall = gameState?.findStall(stallId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (stall != null) getStallTypeName(stall.type) else "Stall") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (stall != null && stall.isUnlocked) {
            StallContent(
                stall = stall,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("This stall is locked", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (stall != null) {
                        Button(onClick = { viewModel.unlockStall(stallId) }) {
                            Text("Unlock for ₹${formatCash(stall.getUnlockCost())}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StallContent(
    stall: Stall,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stall info card
        item {
            StallInfoCard(stall)
        }

        // Tap to serve button
        item {
            TapServeButton(
                onTap = { viewModel.tapServe(stall.id) },
                earnings = stall.tapIncome * (1.0 + (stall.level - 1) * 0.5)
            )
        }

        // Upgrade section
        item {
            UpgradeCard(
                stall = stall,
                onUpgrade = { viewModel.upgradeStall(stall.id) }
            )
        }

        // Helpers section
        item {
            HelpersSection(
                helpers = stall.helpers,
                onHireHelper = { viewModel.hireHelper(stall.id) },
                helperCost = stall.getHelperCost(),
                baseIncome = stall.baseIncome
            )
        }
    }
}

@Composable
fun StallInfoCard(stall: Stall) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Level ${stall.level}", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Income: ₹${formatCash(stall.getTotalIncomePerSecond())}/s",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TapServeButton(onTap: () -> Unit, earnings: Double) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            isPressed = true
            onTap()
            // Reset after animation
            coroutineScope.launch {
                kotlinx.coroutines.delay(100)
                isPressed = false
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.TouchApp,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "SERVE CUSTOMER",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "+₹${formatCash(earnings)}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun UpgradeCard(stall: Stall, onUpgrade: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Upgrade Stall",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Increase tap earnings and helper efficiency",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Level ${stall.level} → ${stall.level + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onUpgrade) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Upgrade")
                    Text(
                        "₹${formatCash(stall.getUpgradeCost())}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun HelpersSection(
    helpers: List<Helper>,
    onHireHelper: () -> Unit,
    helperCost: Double,
    baseIncome: Double
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Helpers (${helpers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = onHireHelper) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hire")
                        Text(
                            "₹${formatCash(helperCost)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (helpers.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No helpers yet. Hire one to earn passive income!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                helpers.forEach { helper ->
                    HelperItem(helper)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun HelperItem(helper: Helper) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text("Helper #${helper.id + 1}", fontWeight = FontWeight.SemiBold)
            Text(
                "Level ${helper.level} • ₹${formatCash(helper.incomePerSecond)}/s",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
