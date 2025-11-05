package com.streettycoon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.streettycoon.game.model.Character
import com.streettycoon.game.model.CharacterStats
import com.streettycoon.game.model.CharacterType
import com.streettycoon.game.model.GameState
import com.streettycoon.ui.components.CharacterCard
import com.streettycoon.ui.components.CharacterRow
import com.streettycoon.ui.components.getCharacterColor
import com.streettycoon.ui.components.getCharacterEmoji
import com.streettycoon.ui.components.getCharacterTypeName
import com.streettycoon.utils.CharacterNameGenerator

/**
 * Character roster screen - shows all owned characters
 */
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
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "No Characters Yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF757575)
                    )
                    Text(
                        text = "Hire your first character to boost your income!",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Button(
                        onClick = onHireCharacterClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hire Character")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(gameState.characters) { character ->
                    CharacterCard(
                        character = character,
                        onLevelUpClick = { onLevelUpCharacter(character.characterId) }
                    )
                }
            }
        }
    }
}

/**
 * Character hiring dialog
 */
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
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hire Character",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Character type selection
                Text(
                    text = "Select Character Type",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF616161)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Spacer(modifier = Modifier.height(16.dp))

                    // Generated name display
                    Text(
                        text = "Name: $generatedName",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stall selection
                    Text(
                        text = "Assign to Stall",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF616161)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        gameState.stalls.filter { it.isUnlocked }.forEach { stall ->
                            StallOption(
                                stallId = stall.id,
                                stallName = "Stall #${stall.id}",
                                isSelected = selectedStallId == stall.id,
                                onClick = { selectedStallId = stall.id }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hire button
                    Button(
                        onClick = {
                            if (selectedType != null && selectedStallId != null) {
                                onHireCharacter(selectedType!!, generatedName, selectedStallId!!)
                                onDismiss()
                            }
                        },
                        enabled = selectedType != null && selectedStallId != null &&
                                gameState.playerCash >= CharacterStats.getStatsForType(selectedType!!).baseCost,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Hire Character")
                    }
                }
            }
        }
    }
}

/**
 * Character type option for hiring dialog
 */
@Composable
private fun CharacterTypeOption(
    type: CharacterType,
    isSelected: Boolean,
    playerCash: Double,
    onClick: () -> Unit
) {
    val stats = CharacterStats.getStatsForType(type)
    val canAfford = playerCash >= stats.baseCost

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canAfford, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = when {
            isSelected -> getCharacterColor(type).copy(alpha = 0.2f)
            canAfford -> Color(0xFFF5F5F5)
            else -> Color(0xFFEEEEEE)
        },
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, getCharacterColor(type))
        } else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = getCharacterEmoji(type),
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = getCharacterTypeName(type),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (canAfford) Color(0xFF212121) else Color(0xFF9E9E9E)
                    )
                    Text(
                        text = "₹${stats.baseCost}",
                        fontSize = 12.sp,
                        color = if (canAfford) Color(0xFF4CAF50) else Color(0xFFE53935)
                    )
                }
            }

            // Show bonuses
            Column(horizontalAlignment = Alignment.End) {
                if (stats.incomeMultiplier > 1.0f) {
                    Text(
                        text = "+${((stats.incomeMultiplier - 1.0f) * 100).toInt()}% Income",
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
                if (stats.tapIncomeBonus > 0.0f) {
                    Text(
                        text = "+${(stats.tapIncomeBonus * 100).toInt()}% Tap",
                        fontSize = 11.sp,
                        color = Color(0xFFFF9800)
                    )
                }
                if (stats.upgradeCostReduction > 0.0f) {
                    Text(
                        text = "-${(stats.upgradeCostReduction * 100).toInt()}% Cost",
                        fontSize = 11.sp,
                        color = Color(0xFF2196F3)
                    )
                }
            }
        }
    }
}

/**
 * Stall option for assigning character
 */
@Composable
private fun StallOption(
    stallId: Int,
    stallName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFFE3F2FD) else Color(0xFFF5F5F5),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF2196F3))
        } else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stallName,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

/**
 * Main Character screen entry point for navigation
 */
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
