package com.streettycoon.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

// Extension function to get emoji for StallType
fun com.streettycoon.game.model.StallType.getEmoji(): String {
    return when (this) {
        com.streettycoon.game.model.StallType.TEA -> "🍵"
        com.streettycoon.game.model.StallType.DOSA -> "🥞"
        com.streettycoon.game.model.StallType.MOMOS -> "🥟"
        com.streettycoon.game.model.StallType.JUICE -> "🧃"
    }
}

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
            EnhancedTopAppBar(
                title = stall?.type?.name ?: "Stall",
                onBack = onBack,
                subtitle = stall?.let { "Level ${it.level}" } ?: ""
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                EnhancedLoadingState(
                    modifier = Modifier.padding(paddingValues)
                )
            }
            stall != null && stall.isUnlocked -> {
                StallContent(
                    stall = stall,
                    viewModel = viewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            stall != null && !stall.isUnlocked -> {
                EnhancedLockedState(
                    stall = stall,
                    onUnlock = { viewModel.unlockStall(stallId) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                EnhancedErrorState(
                    title = "Stall Not Found",
                    message = "The requested stall could not be found.",
                    onRetry = onBack,
                    retryText = "Go Back",
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun EnhancedTopAppBar(
    title: String,
    onBack: () -> Unit,
    subtitle: String = ""
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
            .drawBehind {
                // Add subtle glow effect
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33FF6B35),
                            Color.Transparent
                        )
                    ),
                    size = size
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.xl, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(Spacing.lg))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedLoadingState(modifier: Modifier = Modifier) {
    AnimatedAuroraBackground(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Animated stall icon
                val infiniteTransition = rememberInfiniteTransition()
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOutCubic),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF6B35),
                                    Color(0xFFF7931E)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏪",
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                Text(
                    text = "Loading Stall...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                LinearProgressIndicator(
                    modifier = Modifier.width(200.dp),
                    color = Color(0xFFFF6B35),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun EnhancedLockedState(
    stall: Stall,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedAuroraBackground(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Locked stall icon with glow
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF424242),
                                    Color(0xFF616161)
                                )
                            )
                        )
                        .border(3.dp, Color(0xFFFF6B35), CircleShape)
                        .drawBehind {
                            // Add glow effect
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x33FF6B35),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.width / 2 + 20.dp.toPx()
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                Text(
                    text = "Stall Locked",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = "Unlock this stall to start earning!",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.xl))

                // Unlock button with enhanced styling
                Button(
                    onClick = onUnlock,
                    modifier = Modifier
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF6B35),
                                    Color(0xFFF7931E)
                                )
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Text(
                            text = "Unlock for ₹${formatCash(stall.getUnlockCost())}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    retryText: String,
    modifier: Modifier = Modifier
) {
    AnimatedAuroraBackground(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = Color(0xFFFF6B35),
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(Spacing.xl))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.xl))

                PrimaryButton(onClick = onRetry, text = retryText)
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

    AnimatedAuroraBackground(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            item {
                EnhancedStallContainer(stall = stall) {
                    // Enhanced stall info
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Stall emoji with glow
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFF6B35).copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stall.type.getEmoji(),
                                fontSize = 40.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.lg))

                        Text(
                            text = stall.type.name,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        // Level badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFF7931E),
                                            Color(0xFFFF6B35)
                                        )
                                    )
                                )
                                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                        ) {
                            Text(
                                text = "Level ${stall.level}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    // Enhanced tap mechanics
                    EnhancedTapSection(
                        stall = stall,
                        comboCount = comboCount,
                        showCombo = showCombo,
                        onTap = {
                            viewModel.tapServe(stall.id)
                            comboCount++
                            showCombo = true
                            scope.launch {
                                delay(2000)
                                comboCount = 0
                                showCombo = false
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    // Enhanced helpers summary
                    EnhancedHelpersSummary(helperCount = stall.helpers.size)
                }
            }

            item {
                EnhancedUpgradeSection(
                    stall = stall,
                    onUpgrade = { viewModel.upgradeStall(stall.id) }
                )
            }

            item {
                EnhancedHelpersSection(
                    helpers = stall.helpers,
                    onHireHelper = { viewModel.hireHelper(stall.id) }
                )
            }
        }
    }
}

@Composable
fun EnhancedStallContainer(
    stall: Stall,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .drawBehind {
                // Add subtle glow
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33FF6B35),
                            Color.Transparent
                        )
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    size = size
                )
            }
            .padding(Spacing.xl)
    ) {
        content()
    }
}

@Composable
fun EnhancedTapSection(
    stall: Stall,
    comboCount: Int,
    showCombo: Boolean,
    onTap: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Combo counter with animation
        AnimatedVisibility(
            visible = showCombo,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF6B35),
                                Color(0xFFF7931E)
                            )
                        )
                    )
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                    .drawBehind {
                        // Add glow effect
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x66FF6B35),
                                    Color.Transparent
                                )
                            ),
                            cornerRadius = CornerRadius(20.dp.toPx()),
                            size = size
                        )
                    }
            ) {
                Text(
                    text = "COMBO x$comboCount",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Enhanced tap button
        val infiniteTransition = rememberInfiniteTransition()
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF6B35),
                            Color(0xFFF7931E),
                            Color(0xFFFF8A65)
                        )
                    )
                )
                .border(3.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                .clickable(onClick = onTap)
                .drawBehind {
                    // Add outer glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x33FF6B35),
                                Color.Transparent
                            )
                        ),
                        radius = size.width / 2 + 30.dp.toPx()
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TAP",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SERVE",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "Tap to earn ₹${formatCash(stall.tapIncome)}",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp
        )
    }
}

@Composable
fun EnhancedHelpersSummary(helperCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.People,
                contentDescription = null,
                tint = Color(0xFFFF6B35),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                text = "$helperCount Helpers Working",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun EnhancedUpgradeSection(
    stall: Stall,
    onUpgrade: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF6B35).copy(alpha = 0.5f),
                        Color(0xFFF7931E).copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .drawBehind {
                // Add glow effect
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x22FF6B35),
                            Color.Transparent
                        )
                    ),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    size = size
                )
            }
            .padding(Spacing.xl)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = Spacing.lg)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6B35)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Upgrade,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Upgrade Stall",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Increase earnings and efficiency",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Level ${stall.level} → ${stall.level + 1}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Income: ₹${String.format("%.1f", stall.getTotalIncomePerSecond())}/s",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onUpgrade,
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF6B35),
                                    Color(0xFFF7931E)
                                )
                            )
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Text(
                        text = "₹${formatCash(stall.getUpgradeCost())}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedHelpersSection(
    helpers: List<Helper>,
    onHireHelper: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4CAF50).copy(alpha = 0.5f),
                        Color(0xFF81C784).copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .drawBehind {
                // Add glow effect
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x224CAF50),
                            Color.Transparent
                        )
                    ),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    size = size
                )
            }
            .padding(Spacing.xl)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = Spacing.lg)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Helpers (${helpers.size})",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hire helpers for passive income",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            if (helpers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(Spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No helpers yet. Hire one to earn passive income!",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                helpers.forEach { helper ->
                    EnhancedHelperItem(helper)
                    if (helper != helpers.last()) {
                        Spacer(modifier = Modifier.height(Spacing.md))
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Button(
                onClick = onHireHelper,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFF81C784)
                            )
                        )
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = "Hire Helper",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedHelperItem(helper: Helper) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Helper avatar with gradient background
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4CAF50),
                            Color(0xFF81C784)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(Spacing.lg))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Helper #${helper.id + 1}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Level ${helper.level} • ₹${formatCash(helper.incomePerSecond)}/s",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }

        // Working indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50))
                .drawBehind {
                    // Add glow
                    drawCircle(
                        color = Color(0x664CAF50),
                        radius = 8.dp.toPx()
                    )
                }
        )
    }
}