package com.streettycoon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streettycoon.game.model.Helper
import com.streettycoon.game.model.Stall
import com.streettycoon.ui.GameViewModel
import com.streettycoon.ui.components.*
import com.streettycoon.ui.navigation.formatCash
import com.streettycoon.ui.theme.Colors
import com.streettycoon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StallScreen(
    viewModel: GameViewModel,
    stallId: Int,
    onBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val stall = gameState?.findStall(stallId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stall?.type?.name ?: "Stall") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            stall != null && stall.isUnlocked -> {
                StallContent(
                    stall = stall,
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            stall != null && !stall.isUnlocked -> {
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
                        Spacer(modifier = Modifier.height(Spacing.xl))
                        Text("This stall is locked", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(Spacing.md))
                        PrimaryButton(
                            onClick = { viewModel.unlockStall(stallId) },
                            text = "Unlock for ₹${formatCash(stall.getUnlockCost())}"
                        )
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Stall not found",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(Spacing.md))
                        PrimaryButton(
                            onClick = onBack,
                            text = "Go Back"
                        )
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            item {
                InfoCard(
                    title = stall.type.name,
                    subtitle = "Level ${stall.level}"
                ) {
                    Column(modifier = Modifier.padding(top = Spacing.lg)) {
                        UnlockGateProgressBar(
                            current = stall.level,
                            total = 10,
                            label = "Level Progress"
                        )
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Income/s", style = MaterialTheme.typography.bodyMedium)
                            Text("₹${String.format("%.1f", stall.getTotalIncomePerSecond())}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TapServeButton(
                        onClick = { viewModel.tapServe(stall.id) },
                        modifier = Modifier.padding(vertical = Spacing.xl)
                    )
                }
            }

            item {
                InfoCard(
                    title = "Upgrade Stall",
                    subtitle = "Increase earnings"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.lg),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Level ${stall.level} → ${stall.level + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(Spacing.md))
                        PrimaryButton(
                            text = "₹${formatCash(stall.getUpgradeCost())}",
                            onClick = { viewModel.upgradeStall(stall.id) }
                        )
                    }
                }
            }

            item {
                InfoCard(
                    title = "Helpers (${stall.helpers.size})",
                    subtitle = "Hire helpers to earn passive income"
                ) {
                    Column(modifier = Modifier.padding(top = Spacing.lg)) {
                        if (stall.helpers.isEmpty()) {
                            Text(
                                "No helpers yet. Hire one to earn passive income!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Colors.LockedGray
                            )
                        } else {
                            stall.helpers.forEach { helper ->
                                HelperItem(helper)
                                Spacer(modifier = Modifier.height(Spacing.md))
                            }
                        }
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        SecondaryButton(
                            text = "Hire",
                            onClick = { viewModel.hireHelper(stall.id) }
                        )
                    }
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
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(Spacing.lg),
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
                tint = Colors.GreenOnPrimary
            )
        }
        Spacer(modifier = Modifier.width(Spacing.lg))
        Column {
            Text("Helper #${helper.id + 1}", fontWeight = FontWeight.SemiBold)
            Text(
                "Level ${helper.level} • ₹${formatCash(helper.incomePerSecond)}/s",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}