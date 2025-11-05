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
import androidx.compose.ui.unit.sp
import com.streettycoon.game.model.Helper
import com.streettycoon.game.model.Stall
import com.streettycoon.game.model.StallType
import com.streettycoon.ui.GameViewModel
import com.streettycoon.ui.components.StallTapButton
import com.streettycoon.ui.components.PremiumCard
import com.streettycoon.ui.components.AnimatedMoneyCounter
import com.streettycoon.ui.components.GamingCard
import com.streettycoon.ui.components.GamingButton
import com.streettycoon.ui.components.GamingProgressBar
import com.streettycoon.ui.components.GameStatIndicator
import com.streettycoon.ui.theme.GameColors
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gaming stall info header
            item {
                GamingCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = GameColors.Success
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = getStallTypeName(stall.type),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = GameColors.Success,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Level ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "${stall.level}",
                                        color = GameColors.Warning,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            GameStatIndicator(
                                icon = "⭐",
                                label = "Income/s",
                                value = "₹${String.format("%.1f", stall.getTotalIncomePerSecond())}",
                                color = GameColors.Success
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        GamingProgressBar(
                            progress = (stall.level / 10f).coerceIn(0f, 1f),
                            label = "Level Progress",
                            progressColor = GameColors.Success,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Tap to serve button
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    StallTapButton(
                        stallType = stall.type.name,
                        onTap = { viewModel.tapServe(stall.id) },
                        enabled = true,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            // Upgrade section
            item {
                GamingCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = GameColors.Warning
                ) {
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
                                fontWeight = FontWeight.Bold,
                                color = GameColors.Warning
                            )
                            Text(
                                "Increase earnings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Level ${stall.level} → ${stall.level + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GameColors.Warning,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        GamingButton(
                            text = "₹${formatCash(stall.getUpgradeCost())}",
                            onClick = { viewModel.upgradeStall(stall.id) },
                            glowColor = GameColors.Warning,
                            size = com.streettycoon.ui.components.ButtonSize.SMALL
                        )
                    }
                }
            }

            // Helpers section
            item {
                GamingCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = GameColors.Info
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Helpers (${stall.helpers.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GameColors.Info
                            )
                            GamingButton(
                                text = "Hire",
                                onClick = { viewModel.hireHelper(stall.id) },
                                glowColor = GameColors.Info,
                                size = com.streettycoon.ui.components.ButtonSize.SMALL
                            )
                        }

                        if (stall.helpers.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No helpers yet. Hire one to earn passive income!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            stall.helpers.forEach { helper ->
                                HelperItem(helper)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
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
