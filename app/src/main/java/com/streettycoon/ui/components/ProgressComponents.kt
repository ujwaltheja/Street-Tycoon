package com.streettycoon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.streettycoon.ui.theme.Colors
import com.streettycoon.ui.theme.CornerRadius
import com.streettycoon.ui.theme.Spacing

// ==================== UNLOCK GATE PROGRESS BAR ====================
@Composable
fun UnlockGateProgressBar(
    current: Int,
    total: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(CornerRadius.Small))
                .background(Colors.LockedGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(current.toFloat() / total.toFloat())
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Colors.GreenPrimary, Colors.UnlockedGreen)
                        )
                    )
            )
        }
        
        Text(
            text = "$label: $current/$total",
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

// ==================== EXPERIENCE BAR (CHARACTER) ====================
@Composable
fun ExperienceBar(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = current.toFloat() / total.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(CornerRadius.Small)),
            color = Color(0xFF9C27B0),
            trackColor = Colors.NeutralSurfaceVariant
        )
        Text(
            text = "$current / $total XP",
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

// ==================== CIRCULAR HAPPINESS METER ====================
@Composable
fun HappinessMeter(
    happiness: Int, // 0-100
    modifier: Modifier = Modifier
) {
    val animatedProgress = animateFloatAsState(
        targetValue = happiness / 100f,
        animationSpec = tween(durationMillis = 500),
        label = "happiness_animation"
    )
    
    val color = when {
        happiness < 30 -> Colors.ErrorRed
        happiness < 60 -> Colors.WarningOrange
        else -> Colors.SuccessGreen
    }
    
    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Colors.NeutralSurfaceVariant, shape = RoundedCornerShape(50))
        )
        
        // Progress circle (simplified linear representation)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$happiness%",
                style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
                color = color
            )
            Text(
                text = "Happiness",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
        }
    }
}