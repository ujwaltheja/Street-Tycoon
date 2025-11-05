package com.streettycoon.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streettycoon.game.model.GateType
import com.streettycoon.game.model.MapGate
import com.streettycoon.game.model.Zone

/**
 * Card displaying a single map gate with progress
 */
@Composable
fun MapGateProgressCard(
    gate: MapGate,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gate.isCompleted) Color(0xFFE8F5E9) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Gate description
                Text(
                    text = gate.description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (gate.isCompleted) Color(0xFF2E7D32) else Color(0xFF424242)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { gate.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (gate.isCompleted) Color(0xFF4CAF50) else Color(0xFF2196F3),
                    trackColor = Color(0xFFE0E0E0)
                )

                // Progress text
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${gate.currentValue} / ${gate.targetValue}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = "${gate.progressPercent}%",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Completion icon
            if (gate.isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Display all gates for a zone
 */
@Composable
fun ZoneGatesSection(
    zone: Zone,
    modifier: Modifier = Modifier
) {
    if (zone.gates.isEmpty()) {
        // No gates for this zone
        return
    }

    Column(modifier = modifier) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Unlock Requirements",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )

            Text(
                text = "${zone.getCompletedGatesCount()}/${zone.gates.size} Complete",
                fontSize = 13.sp,
                color = if (zone.allGatesComplete()) Color(0xFF4CAF50) else Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )
        }

        // Gates list
        zone.gates.forEach { gate ->
            MapGateProgressCard(
                gate = gate,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        // Overall progress
        if (zone.allGatesComplete()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "✓ All requirements met! You can unlock this zone.",
                fontSize = 14.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

/**
 * Get icon description for gate type
 */
fun getGateIcon(gateType: GateType): String {
    return when (gateType) {
        GateType.UPGRADES_COMPLETED -> "⬆️"
        GateType.HELPERS_HIRED -> "👥"
        GateType.EARNINGS_THRESHOLD -> "💰"
        GateType.PLAYTIME_HOURS -> "⏰"
    }
}

/**
 * Get short description for gate type
 */
fun getGateTypeShortName(gateType: GateType): String {
    return when (gateType) {
        GateType.UPGRADES_COMPLETED -> "Upgrades"
        GateType.HELPERS_HIRED -> "Helpers"
        GateType.EARNINGS_THRESHOLD -> "Earnings"
        GateType.PLAYTIME_HOURS -> "Playtime"
    }
}
