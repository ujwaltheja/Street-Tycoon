package com.streettycoon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    var comboCount by remember { mutableStateOf(0) }
    var showCombo by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFF3E0),
                        Color(0xFFFFE0B2),
                        Color(0xFFFFCC80)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            item {
                // Stall container with glossy card from HTML prototype
                StallContainer {
                    // Stall info
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🍵 ${stall.type.name}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Level ${stall.level}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF57C00)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Tap button with combo counter
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Combo counter above tap button
                            ComboCounter(
                                comboCount = comboCount,
                                isVisible = showCombo,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Large circular tap button
                            TapServeButton(
                                onClick = {
                                    viewModel.tapServe(stall.id)
                                    comboCount++
                                    showCombo = true
                                    // Hide combo after 2 seconds
                                    scope.launch {
                                        delay(2000)
                                        comboCount = 0
                                        showCombo = false
                                    }
                                },
                                text = "TAP"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Helpers count
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👥 ${stall.helpers.size} Helpers Working",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            item {
                GlossyCard {
                    Text(
                        text = "Upgrade Stall",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colors.TextPrimary
                    )
                    Text(
                        text = "Increase earnings",
                        fontSize = 12.sp,
                        color = Colors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Level ${stall.level} → ${stall.level + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Income: ₹${String.format("%.1f", stall.getTotalIncomePerSecond())}/s",
                                style = MaterialTheme.typography.bodySmall,
                                color = Colors.SuccessGreen
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
                GlossyCard {
                    Text(
                        text = "Helpers (${stall.helpers.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colors.TextPrimary
                    )
                    Text(
                        text = "Hire helpers to earn passive income",
                        fontSize = 12.sp,
                        color = Colors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(Spacing.lg))
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
                        text = "Hire Helper",
                        onClick = { viewModel.hireHelper(stall.id) }
                    )
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