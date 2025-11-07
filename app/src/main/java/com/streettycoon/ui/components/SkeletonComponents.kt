package com.streettycoon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Shimmer effect for skeleton screens
 * Provides animated loading placeholders
 */
@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_animation"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 1000f, 0f),
        end = Offset(translateAnim, 0f)
    )
}

/**
 * Skeleton card for stall items
 */
@Composable
fun SkeletonStallCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skeleton image/icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush = shimmerBrush())
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Skeleton title
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush = shimmerBrush())
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Skeleton subtitle
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush = shimmerBrush())
                )
            }

            // Skeleton action button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(brush = shimmerBrush())
            )
        }
    }
}

/**
 * Skeleton card for zone items
 */
@Composable
fun SkeletonZoneCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Skeleton title
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush = shimmerBrush())
            )

            // Skeleton progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush = shimmerBrush())
            )

            // Skeleton subtitle
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush = shimmerBrush())
            )
        }
    }
}

/**
 * Skeleton tap button
 */
@Composable
fun SkeletonTapButton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(brush = shimmerBrush()),
        contentAlignment = Alignment.Center
    ) {
        // Inner circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
        )
    }
}

/**
 * Skeleton stat card
 */
@Composable
fun SkeletonStatCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(2) {
                Column(modifier = Modifier.weight(1f)) {
                    // Label
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush = shimmerBrush())
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Value
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush = shimmerBrush())
                    )
                }
            }
        }
    }
}

/**
 * Loading screen with skeleton cards
 */
@Composable
fun SkeletonLoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Skeleton stat card
        SkeletonStatCard()

        // Skeleton zone cards in grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonZoneCard(modifier = Modifier.weight(1f))
            SkeletonZoneCard(modifier = Modifier.weight(1f))
        }

        // More skeleton cards
        repeat(3) {
            SkeletonStallCard()
        }
    }
}
