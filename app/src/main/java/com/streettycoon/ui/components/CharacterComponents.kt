package com.streettycoon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streettycoon.game.model.Character
import com.streettycoon.game.model.CharacterStats
import com.streettycoon.game.model.CharacterType

/**
 * Card displaying a character with stats and level
 */
@Composable
fun CharacterCard(
    character: Character,
    onLevelUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats = CharacterStats.getStatsForType(character.type)
    val canLevelUp = character.canLevelUp()

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Avatar, Name, Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar (colored circle with emoji)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(getCharacterColor(character.type)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getCharacterEmoji(character.type),
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = character.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = getCharacterTypeName(character.type),
                            fontSize = 13.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                // Level badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2196F3)
                ) {
                    Text(
                        text = "Lv ${character.level}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Income bonus
                if (stats.incomeMultiplier > 1.0f) {
                    StatRow(
                        icon = "💰",
                        label = "Income Bonus",
                        value = "+${((stats.incomeMultiplier - 1.0f) * 100 * character.productivityMultiplier).toInt()}%"
                    )
                }

                // Tap bonus
                if (stats.tapIncomeBonus > 0.0f) {
                    StatRow(
                        icon = "👆",
                        label = "Tap Bonus",
                        value = "+${(stats.tapIncomeBonus * 100 * character.productivityMultiplier).toInt()}%"
                    )
                }

                // Cost reduction
                if (stats.upgradeCostReduction > 0.0f) {
                    StatRow(
                        icon = "💵",
                        label = "Cost Reduction",
                        value = "-${(stats.upgradeCostReduction * 100).toInt()}%"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // XP Progress
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Experience",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                    Text(
                        text = "${character.experience} / ${character.getXpRequired()}",
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = character.getXpProgress(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFF9800),
                    trackColor = Color(0xFFFFE0B2)
                )
            }

            // Level up button
            if (canLevelUp) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onLevelUpClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Level Up!")
                }
            } else if (character.level >= stats.maxLevel) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "MAX LEVEL",
                    fontSize = 13.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Compact character row for lists
 */
@Composable
fun CharacterRow(
    character: Character,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(8.dp)
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
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(getCharacterColor(character.type)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getCharacterEmoji(character.type),
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = character.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "${getCharacterTypeName(character.type)} • Lv ${character.level}",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }
            }

            // Level up indicator
            if (character.canLevelUp()) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Can level up",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Display a stat row
 */
@Composable
private fun StatRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = icon,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color(0xFF616161)
            )
        }
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )
    }
}

/**
 * Get color for character type
 */
fun getCharacterColor(type: CharacterType): Color {
    return when (type) {
        CharacterType.CHEF -> Color(0xFFFF5722)      // Deep Orange
        CharacterType.MANAGER -> Color(0xFF2196F3)   // Blue
        CharacterType.STAFF -> Color(0xFF4CAF50)     // Green
        CharacterType.SPECIALIST -> Color(0xFF9C27B0) // Purple
    }
}

/**
 * Get emoji for character type
 */
fun getCharacterEmoji(type: CharacterType): String {
    return when (type) {
        CharacterType.CHEF -> "👨‍🍳"
        CharacterType.MANAGER -> "👔"
        CharacterType.STAFF -> "👨‍💼"
        CharacterType.SPECIALIST -> "⭐"
    }
}

/**
 * Get display name for character type
 */
fun getCharacterTypeName(type: CharacterType): String {
    return when (type) {
        CharacterType.CHEF -> "Chef"
        CharacterType.MANAGER -> "Manager"
        CharacterType.STAFF -> "Staff"
        CharacterType.SPECIALIST -> "Specialist"
    }
}
