package com.streettycoon.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streettycoon.ui.theme.Colors
import com.streettycoon.ui.theme.CornerRadius
import com.streettycoon.ui.theme.Spacing

// ==================== GAME NAVIGATION ITEMS ====================

data class GameNavItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val badge: String? = null,
    val route: String
)

// ==================== BOTTOM NAVIGATION BAR ====================
/**
 * Premium glossy bottom navigation bar for Street Tycoon
 * Features: Glossy effect, glow on selection, smooth animations
 */
@Composable
fun GameBottomNavigation(
    items: List<GameNavItem>,
    selectedItemId: String,
    onItemSelected: (GameNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = Color(0xFF1A1A1A)  // Deep dark background
    val selectedColor = Colors.CurrencyGold  // Vibrant gold for selected
    val unselectedColor = Color(0xFFB0B0B0)  // Muted light gray

    // Pulsing glow on selected item
    val infiniteTransition = rememberInfiniteTransition(label = "nav_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "nav_glow_alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                ambientColor = selectedColor.copy(alpha = 0.15f)
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        // Glossy overlay at top
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    GameNavItem(
                        item = item,
                        isSelected = item.id == selectedItemId,
                        selectedColor = selectedColor,
                        unselectedColor = unselectedColor,
                        glowAlpha = glowAlpha,
                        onClick = { onItemSelected(item) }
                    )
                }
            }
        }
    }
}

/**
 * Individual navigation item with glow effect
 */
@Composable
fun RowScope.GameNavItem(
    item: GameNavItem,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    glowAlpha: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else unselectedColor,
        animationSpec = tween(300),
        label = "nav_item_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "nav_item_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "nav_item_scale"
    )

    Box(
        modifier = modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Glow background
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                selectedColor.copy(alpha = glowAlpha * 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Icon container
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    color = if (isSelected)
                        selectedColor.copy(alpha = 0.15f)
                    else
                        Color.Transparent,
                    shape = CircleShape
                )
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = itemColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = itemColor,
                    modifier = Modifier
                        .size(24.dp)
                )

                // Badge
                if (item.badge != null) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(
                                color = Colors.ErrorRed,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.badge,
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Label
        Text(
            text = item.label,
            fontSize = 10.sp,
            color = itemColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        )
    }
}

// ==================== TAB NAVIGATION ====================
/**
 * Premium tab navigation with glossy style
 */
@Composable
fun GameTabNavigation(
    items: List<GameNavItem>,
    selectedItemId: String,
    onItemSelected: (GameNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = Color(0xFF0D0D0D)  // Deep black
    val selectedColor = Colors.OrangePrimary  // Vibrant orange
    val unselectedColor = Color(0xFF808080)  // Muted gray

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                ambientColor = selectedColor.copy(alpha = 0.2f)
            ),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                GameTabItem(
                    item = item,
                    isSelected = item.id == selectedItemId,
                    selectedColor = selectedColor,
                    unselectedColor = unselectedColor,
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual tab item with shimmer effect on selection
 */
@Composable
fun GameTabItem(
    item: GameNavItem,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else unselectedColor,
        animationSpec = tween(300),
        label = "tab_color"
    )

    // Shimmer on selected
    val infiniteTransition = rememberInfiniteTransition(label = "tab_shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isSelected) 0.2f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tab_shimmer"
    )

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (isSelected)
                    selectedColor.copy(alpha = 0.1f)
                else
                    Color.Transparent
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = itemColor.copy(alpha = if (isSelected) 1f else 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Shimmer overlay
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = shimmerAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = itemColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = item.label,
                    color = itemColor,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
    }
}

// ==================== NAVIGATION DRAWER ====================
/**
 * Premium glossy navigation drawer for side menu
 */
@Composable
fun GameNavigationDrawer(
    items: List<GameNavItem>,
    selectedItemId: String,
    onItemSelected: (GameNavItem) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {}
) {
    val drawerBackground = Color(0xFF1A1A1A)  // Deep dark
    val selectedColor = Colors.CurrencyGold
    val unselectedColor = Color(0xFFB0B0B0)

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp),
        color = drawerBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            // Drawer header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(Spacing.lg)
            ) {
                header()
            }

            Divider(
                color = selectedColor.copy(alpha = 0.1f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = Spacing.lg)
            )

            // Navigation items
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = Spacing.md)
            ) {
                items.forEach { item ->
                    GameDrawerItem(
                        item = item,
                        isSelected = item.id == selectedItemId,
                        selectedColor = selectedColor,
                        unselectedColor = unselectedColor,
                        onClick = { onItemSelected(item) }
                    )
                }
            }
        }
    }
}

/**
 * Individual drawer menu item with glow on selection
 */
@Composable
fun GameDrawerItem(
    item: GameNavItem,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else unselectedColor,
        animationSpec = tween(300),
        label = "drawer_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "drawer_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isSelected) 0.3f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drawer_glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (isSelected)
                    selectedColor.copy(alpha = 0.1f + glowAlpha * 0.1f)
                else
                    Color.Transparent
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = itemColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = itemColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = item.label,
                color = itemColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            // Badge on right
            if (item.badge != null) {
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(
                            color = Colors.ErrorRed,
                            shape = CircleShape
                        )
                        .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.badge,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==================== FLOATING ACTION NAVIGATION ====================
/**
 * Premium floating action button with glow for primary action
 */
@Composable
fun GameFloatingActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Colors.OrangePrimary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_glow_alpha"
    )

    Box(
        modifier = modifier
            .size(64.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = backgroundColor.copy(alpha = glowAlpha)
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

// ==================== BREADCRUMB NAVIGATION ====================
/**
 * Breadcrumb trail for showing navigation hierarchy
 */
@Composable
fun GameBreadcrumbs(
    items: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Colors.CurrencyGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items.forEachIndexed { index, (label, onClick) ->
            Text(
                text = label,
                color = Colors.CurrencyGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onClick)
            )

            if (index < items.size - 1) {
                Text(
                    text = ">",
                    color = Colors.CurrencyGold.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
