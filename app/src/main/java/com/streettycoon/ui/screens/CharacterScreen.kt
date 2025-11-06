package com.streettycoon.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.streettycoon.game.model.CharacterStats
import com.streettycoon.game.model.CharacterType
import com.streettycoon.game.model.GameState
import com.streettycoon.ui.components.CharacterCard
import com.streettycoon.ui.components.InfoCard
import com.streettycoon.ui.components.PrimaryButton
import com.streettycoon.ui.theme.Colors
import com.streettycoon.ui.theme.Spacing
import com.streettycoon.utils.CharacterNameGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterRosterScreen(
    gameState: GameState,
    onLevelUpCharacter: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onHireCharacterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Team") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onHireCharacterClick) {
                        Icon(Icons.Default.Add, "Hire Character")
                    }
                }
            )
        }
    ) { padding ->
        if (gameState.characters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.xl)
                ) {
                    Text(
                        text = "No Characters Yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Colors.LockedGray
                    )
                    Text(
                        text = "Hire your first character to boost your income!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Colors.LockedGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = Spacing.xxxl)
                    )
                    PrimaryButton(
                        onClick = onHireCharacterClick,
                        text = "Hire Character"
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                items(gameState.characters) { character ->
                    CharacterCard(
                        name = character.name,
                        level = character.level,
                        onClick = { onLevelUpCharacter(character.characterId) }
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterHiringDialog(
    gameState: GameState,
    onDismiss: () -> Unit,
    onHireCharacter: (CharacterType, String, Int) -> Unit
) {
    var selectedType by remember { mutableStateOf<CharacterType?>(null) }
    var selectedStallId by remember { mutableStateOf<Int?>(null) }
    var generatedName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hire Character",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                Text(
                    text = "Select Character Type",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    CharacterType.values().forEach { type ->
                        CharacterTypeOption(
                            type = type,
                            isSelected = selectedType == type,
                            playerCash = gameState.playerCash,
                            onClick = {
                                selectedType = type
                                generatedName = CharacterNameGenerator.generateName(type)
                            }
                        )
                    }
                }

                if (selectedType != null) {
                    Spacer(modifier = Modifier.height(Spacing.xl))

                    Text(
                        text = "Name: $generatedName",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    Text(
                        text = "Assign to Stall",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        gameState.stalls.filter { it.isUnlocked }.forEach { stall ->
                            StallOption(
                                stallId = stall.id,
                                stallName = "Stall #${stall.id}",
                                isSelected = selectedStallId == stall.id,
                                onClick = { selectedStallId = stall.id }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    PrimaryButton(
                        onClick = {
                            if (selectedType != null && selectedStallId != null) {
                                onHireCharacter(selectedType!!, generatedName, selectedStallId!!)
                                onDismiss()
                            }
                        },
                        enabled = selectedType != null && selectedStallId != null &&
                                gameState.playerCash >= CharacterStats.getStatsForType(selectedType!!).baseCost,
                        text = "Hire Character"
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterTypeOption(
    type: CharacterType,
    isSelected: Boolean,
    playerCash: Double,
    onClick: () -> Unit
) {
    val stats = CharacterStats.getStatsForType(type)
    val canAfford = playerCash >= stats.baseCost

    InfoCard(
        title = type.name,
        subtitle = "Cost: ${stats.baseCost}"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = canAfford, onClick = onClick)
                .padding(top = Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(horizontalAlignment = Alignment.End) {
                if (stats.incomeMultiplier > 1.0f) {
                    Text(
                        text = "+${((stats.incomeMultiplier - 1.0f) * 100).toInt()}% Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = Colors.SuccessGreen
                    )
                }
                if (stats.tapIncomeBonus > 0.0f) {
                    Text(
                        text = "+${(stats.tapIncomeBonus * 100).toInt()}% Tap",
                        style = MaterialTheme.typography.bodySmall,
                        color = Colors.WarningOrange
                    )
                }
                if (stats.upgradeCostReduction > 0.0f) {
                    Text(
                        text = "-${(stats.upgradeCostReduction * 100).toInt()}% Cost",
                        style = MaterialTheme.typography.bodySmall,
                        color = Colors.InfoBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun StallOption(
    stallId: Int,
    stallName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    InfoCard(
        title = stallName,
        subtitle = ""
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {}
    }
}

@Composable
fun CharacterScreen(
    viewModel: com.streettycoon.ui.GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()
    var showHireDialog by remember { mutableStateOf(false) }

    gameState?.let { state ->
        CharacterRosterScreen(
            gameState = state,
            onLevelUpCharacter = { characterId ->
                viewModel.levelUpCharacter(characterId)
            },
            onNavigateBack = { /* No back navigation needed in main nav */ },
            onHireCharacterClick = { showHireDialog = true },
            modifier = modifier
        )

        if (showHireDialog) {
            CharacterHiringDialog(
                gameState = state,
                onDismiss = { showHireDialog = false },
                onHireCharacter = { type, name, stallId ->
                    viewModel.hireCharacter(type, name, stallId)
                    showHireDialog = false
                }
            )
        }
    }
}
