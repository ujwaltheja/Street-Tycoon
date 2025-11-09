package com.streettycoon.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.streettycoon.ui.GameViewModel
import com.streettycoon.ui.screens.CharacterScreen
import com.streettycoon.ui.screens.FamilyScreen
import com.streettycoon.ui.screens.MapScreen
import com.streettycoon.ui.screens.SettingsScreen
import com.streettycoon.ui.screens.ShopScreen
import com.streettycoon.ui.screens.StallScreen
import com.streettycoon.ui.utils.rememberWindowSize
import com.streettycoon.ui.utils.WindowSize
import kotlinx.coroutines.launch

// Helper data class for navigation items
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// -----------------------------
// Screen Routes
// -----------------------------
sealed class Screen(val route: String, val title: String) {
    object Map : Screen("map", "Map")
    object Stall : Screen("stall/{stallId}", "Stall")
    object Shop : Screen("shop", "Shop")
    object Characters : Screen("characters", "Characters")
    object Family : Screen("family", "Family")
    object Settings : Screen("settings", "Settings")
}

// -----------------------------
// Main App Scaffold
// -----------------------------
@Composable
fun StreetTycoonApp(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentRoute = navController.currentBackStackEntryAsState()?.value?.destination?.route
    val canGoBack = navController.previousBackStackEntry != null

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StreetTycoonNavigationDrawer(
                navController = navController,
                drawerState = drawerState,
                viewModel = viewModel
            )
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                StreetTycoonTopBar(
                    viewModel = viewModel,
                    canGoBack = canGoBack,
                    onBackClick = { navController.popBackStack() },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    currentRoute = currentRoute
                )
            },
            bottomBar = { StreetTycoonNavigationBar(navController) },
            floatingActionButton = {
                StreetTycoonFAB(navController, currentRoute)
            },
            floatingActionButtonPosition = FabPosition.End
        ) { paddingValues ->
            NavigationGraph(
                navController = navController,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

// -----------------------------
// Top Bar — Enhanced Game-Style Header with Rich Visual Effects
// -----------------------------
@Composable
fun StreetTycoonTopBar(
    viewModel: GameViewModel,
    canGoBack: Boolean = false,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    currentRoute: String? = null
) {
    val gameState by viewModel.gameState.collectAsState()
    val windowSize = rememberWindowSize()
    val isCompact = windowSize == WindowSize.Compact

    Surface(
        tonalElevation = 20.dp,
        shadowElevation = 20.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D0D0D).copy(alpha = 0.95f), // Deep dark
                            Color(0xFF1A1A2E).copy(alpha = 0.9f),  // Dark blue-black
                            Color(0xFF16213E).copy(alpha = 0.9f),  // Deep navy
                            Color(0xFF0F3460).copy(alpha = 0.8f)   // Electric blue
                        )
                    )
                )
                .border(
                    width = 3.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF6B35).copy(alpha = 0.8f),
                            Color(0xFFF7931E).copy(alpha = 0.8f),
                            Color(0xFFFFD23F).copy(alpha = 0.8f),
                            Color(0xFF4CAF50).copy(alpha = 0.6f),
                            Color(0xFF2196F3).copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Navigation Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Back Button (only show if can go back)
                    if (canGoBack) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color.White.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                                .border(2.dp, Color(0xFFFFD23F).copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Go Back",
                                tint = Color(0xFFFFD23F),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Menu Button
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color.White.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                            .border(2.dp, Color(0xFFFFD23F).copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color(0xFFFFD23F),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Breadcrumb Navigation
                    currentRoute?.let { route ->
                        val screenName = when {
                            route.startsWith("stall/") -> "Stall Management"
                            route == Screen.Map.route -> "Market Map"
                            route == Screen.Characters.route -> "Team Management"
                            route == Screen.Family.route -> "Family Tree"
                            route == Screen.Shop.route -> "Item Shop"
                            route == Screen.Settings.route -> "Settings"
                            else -> "Street Tycoon"
                        }

                        Text(
                            text = screenName,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // Game Title with Enhanced Glow Effect (only show on main screens and larger screens)
                if (!isCompact && (currentRoute == Screen.Map.route || currentRoute?.startsWith("stall/") != true)) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Animated city icon
                            Text(
                                text = "🏙️",
                                fontSize = if (windowSize == WindowSize.Expanded) 28.sp else 24.sp,
                                modifier = Modifier.shadow(12.dp, ambientColor = Color(0xFFFFD23F))
                            )

                            Column {
                                Text(
                                    text = "STREET TYCOON",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontSize = if (windowSize == WindowSize.Expanded) 24.sp else 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = if (windowSize == WindowSize.Expanded) 2.sp else 1.sp
                                    ),
                                    color = Color(0xFFFFD23F),
                                    modifier = Modifier.shadow(12.dp, ambientColor = Color(0xFFFFD23F))
                                )
                                if (windowSize == WindowSize.Expanded) {
                                    Text(
                                        text = "Build Your Empire",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.shadow(4.dp)
                                    )
                                }
                            }
                        }
                    }
                } else if (isCompact) {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Enhanced Stats Panel - Responsive
                gameState?.let { state ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Always show cash
                        GameStatChip(
                            icon = "💰",
                            label = "CASH",
                            value = "₹${formatCash(state.playerCash)}",
                            gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39)),
                            isPrimary = true,
                            isCompact = isCompact
                        )

                        // Show tokens only on medium+ screens
                        if (!isCompact) {
                            GameStatChip(
                                icon = "🪙",
                                label = "TOKENS",
                                value = state.playerTokens.toString(),
                                gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFFC107), Color(0xFFFFEB3B)),
                                isPrimary = false,
                                isCompact = false
                            )
                        }

                        // Day indicator - always show but compact on small screens
                        Surface(
                            color = Color(0xFF9C27B0).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(if (isCompact) 16.dp else 20.dp),
                            tonalElevation = 6.dp,
                            modifier = Modifier.clip(RoundedCornerShape(if (isCompact) 16.dp else 20.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF9C27B0).copy(alpha = 0.3f),
                                                Color(0xFFBA68C8).copy(alpha = 0.3f),
                                                Color(0xFFE91E63).copy(alpha = 0.3f)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 2.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                                        ),
                                        shape = RoundedCornerShape(if (isCompact) 16.dp else 20.dp)
                                    )
                                    .padding(
                                        horizontal = if (isCompact) 10.dp else 14.dp,
                                        vertical = if (isCompact) 8.dp else 10.dp
                                    ),
                                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "📅", fontSize = if (isCompact) 14.sp else 16.sp)
                                Text(
                                    text = "DAY ${state.currentDay}",
                                    color = Color.White,
                                    fontSize = if (isCompact) 10.sp else 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.shadow(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------
// Enhanced Game Stat Chip with Icon and Visual Effects
// -----------------------------
@Composable
private fun GameStatChip(
    icon: String,
    label: String,
    value: String,
    gradientColors: List<Color>,
    isPrimary: Boolean = false,
    isCompact: Boolean = false
) {
    val chipSize = if (isPrimary) Modifier else Modifier.graphicsLayer(scaleX = 0.9f, scaleY = 0.9f)

    Surface(
        color = Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(if (isCompact) 16.dp else 20.dp),
        tonalElevation = 8.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(if (isCompact) 16.dp else 20.dp))
            .then(chipSize)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(colors = gradientColors)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = gradientColors.map { it.copy(alpha = 0.8f) }
                    ),
                    shape = RoundedCornerShape(if (isCompact) 16.dp else 20.dp)
                )
                .padding(
                    horizontal = if (isCompact) 12.dp else 16.dp,
                    vertical = if (isCompact) 8.dp else 12.dp
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon with glow effect
                Text(
                    text = icon,
                    fontSize = when {
                        isCompact && isPrimary -> 16.sp
                        isCompact -> 14.sp
                        isPrimary -> 18.sp
                        else -> 16.sp
                    },
                    modifier = Modifier.shadow(
                        elevation = if (isPrimary) 8.dp else 4.dp,
                        ambientColor = gradientColors.first()
                    )
                )

                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = label,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = when {
                            isCompact && isPrimary -> 9.sp
                            isCompact -> 8.sp
                            isPrimary -> 10.sp
                            else -> 9.sp
                        },
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = when {
                            isCompact && isPrimary -> 12.sp
                            isCompact -> 10.sp
                            isPrimary -> 14.sp
                            else -> 12.sp
                        },
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.shadow(
                            elevation = if (isPrimary) 6.dp else 3.dp,
                            ambientColor = gradientColors.first()
                        )
                    )
                }
            }
        }
    }
}

// -----------------------------
// Bottom Navigation Bar (Enhanced Game-Style with Rich Visual Effects)
// -----------------------------
@Composable
fun StreetTycoonNavigationBar(navController: NavHostController) {
    val windowSize = rememberWindowSize()
    val isCompact = windowSize == WindowSize.Compact

    val items = listOf(
        Quadruple(Screen.Map, "🗺️", "Market", listOf(Color(0xFF2196F3), Color(0xFF00BCD4), Color(0xFF009688))),
        Quadruple(Screen.Characters, "👥", "Team", listOf(Color(0xFFFF5722), Color(0xFFFF9800), Color(0xFFFFC107))),
        Quadruple(Screen.Family, "🏠", "Family", listOf(Color(0xFF9C27B0), Color(0xFFBA68C8), Color(0xFFE91E63))),
        Quadruple(Screen.Shop, "🛒", "Shop", listOf(Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39))),
        Quadruple(Screen.Settings, "⚙️", "Settings", listOf(Color(0xFF607D8B), Color(0xFF90A4AE), Color(0xFFB0BEC5)))
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (isCompact) 8.dp else 16.dp,
                vertical = if (isCompact) 8.dp else 12.dp
            )
            .clip(RoundedCornerShape(if (isCompact) 20.dp else 28.dp))
            .shadow(if (isCompact) 12.dp else 16.dp, RoundedCornerShape(if (isCompact) 20.dp else 28.dp)),
        color = Color(0xFF0D0D0D).copy(alpha = 0.95f),
        tonalElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E).copy(alpha = 0.9f),
                            Color(0xFF16213E).copy(alpha = 0.9f),
                            Color(0xFF0F3460).copy(alpha = 0.9f)
                        )
                    )
                )
                .border(
                    width = if (isCompact) 2.dp else 3.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF6B35).copy(alpha = 0.8f),
                            Color(0xFFF7931E).copy(alpha = 0.8f),
                            Color(0xFFFFD23F).copy(alpha = 0.8f),
                            Color(0xFF4CAF50).copy(alpha = 0.6f),
                            Color(0xFF2196F3).copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(if (isCompact) 20.dp else 28.dp)
                )
                .padding(
                    horizontal = if (isCompact) 8.dp else 12.dp,
                    vertical = if (isCompact) 12.dp else 16.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (screen, emoji, label, gradientColors) ->
                    val selected = currentRoute == screen.route
                    val interactionSource = remember { MutableInteractionSource() }
                    var isPressed by remember { mutableStateOf(false) }

                    val scale by animateFloatAsState(
                        targetValue = if (selected) (if (isCompact) 1.1f else 1.2f) else if (isPressed) 0.95f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "nav_item_scale"
                    )

                    val glowAlpha by animateFloatAsState(
                        targetValue = if (selected) 0.8f else if (isPressed) 0.4f else 0f,
                        animationSpec = tween(300),
                        label = "nav_item_glow"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .clip(RoundedCornerShape(if (isCompact) 16.dp else 20.dp))
                            .then(
                                if (selected) Modifier.background(
                                    Brush.radialGradient(
                                        colors = gradientColors.map { it.copy(alpha = 0.3f) },
                                        center = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                                        radius = if (isCompact) 60f else 80f
                                    ),
                                    shape = RoundedCornerShape(if (isCompact) 16.dp else 20.dp)
                                ) else Modifier
                            )
                            .border(
                                width = if (selected) (if (isCompact) 2.dp else 3.dp) else 0.dp,
                                brush = if (selected) Brush.linearGradient(gradientColors) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                                shape = RoundedCornerShape(if (isCompact) 16.dp else 20.dp)
                            )
                            .padding(
                                vertical = if (isCompact) 8.dp else 10.dp,
                                horizontal = if (isCompact) 4.dp else 6.dp
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                    ) {
                        // Glow effect behind icon
                        if (glowAlpha > 0f) {
                            Box(
                                modifier = Modifier
                                    .size(if (isCompact) 28.dp else 32.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                gradientColors.first().copy(alpha = glowAlpha),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = if (isCompact) 18.sp else 20.sp,
                                    modifier = Modifier.shadow(if (isCompact) 6.dp else 8.dp, ambientColor = gradientColors.first())
                                )
                            }
                        } else {
                            Text(
                                text = emoji,
                                fontSize = when {
                                    selected && isCompact -> 22.sp
                                    selected -> 26.sp
                                    isCompact -> 18.sp
                                    else -> 22.sp
                                },
                                modifier = Modifier.shadow(if (selected) 6.dp else 2.dp)
                            )
                        }

                        Text(
                            text = label,
                            fontSize = if (isCompact) 9.sp else 10.sp,
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.shadow(if (selected) 4.dp else 0.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        // Active indicator dot
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(if (isCompact) 3.dp else 4.dp)
                                    .background(
                                        Brush.linearGradient(gradientColors),
                                        shape = CircleShape
                                    )
                                    .shadow(4.dp, shape = CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}// -----------------------------
// Navigation Graph with Screen Transitions
// -----------------------------
@Composable
fun NavigationGraph(
    navController: NavHostController,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Map.route,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 300 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -300 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -300 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { 300 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.Map.route) {
            MapScreen(
                viewModel = viewModel,
                onStallClick = { stallId -> navController.navigate("stall/$stallId") }
            )
        }

        composable(
            Screen.Stall.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { -100 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val stallId = backStackEntry.arguments?.getString("stallId")?.toIntOrNull()
            if (stallId != null) {
                StallScreen(
                    viewModel = viewModel,
                    stallId = stallId,
                    onBack = { navController.popBackStack() }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Invalid stall", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) { Text("Go Back") }
                    }
                }
            }
        }

        composable(Screen.Shop.route) { ShopScreen(viewModel = viewModel) }
        composable(Screen.Characters.route) { CharacterScreen(viewModel = viewModel) }
        composable(Screen.Family.route) { FamilyScreen(viewModel = viewModel) }
        composable(Screen.Settings.route) {
            SettingsScreen(
                audioManager = viewModel.audioManager,
                onNavigateBack = { /* no back needed */ }
            )
        }
    }
}

// -----------------------------
// Navigation Drawer - Enhanced Side Menu
// -----------------------------
@Composable
fun StreetTycoonNavigationDrawer(
    navController: NavHostController,
    drawerState: DrawerState,
    viewModel: GameViewModel
) {
    val scope = rememberCoroutineScope()
    val gameState by viewModel.gameState.collectAsState()

    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF0D0D0D),
        drawerContentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFF6B35), Color(0xFFFFD23F))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👑", fontSize = 24.sp)
                }

                Column {
                    Text(
                        text = "Street Tycoon",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD23F)
                    )
                    gameState?.let {
                        Text(
                            text = "Day ${it.currentDay}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Quick Stats
            gameState?.let { state ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Quick Stats",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD23F)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cash:", color = Color.White.copy(alpha = 0.8f))
                            Text(
                                "₹${formatCash(state.playerCash)}",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tokens:", color = Color.White.copy(alpha = 0.8f))
                            Text(
                                state.playerTokens.toString(),
                                color = Color(0xFFFF9800),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Navigation Items
            val drawerItems = listOf(
                Triple("🗺️", "Map", Screen.Map.route),
                Triple("👥", "Team", Screen.Characters.route),
                Triple("👨‍👩‍👧‍👦", "Family", Screen.Family.route),
                Triple("🛒", "Shop", Screen.Shop.route),
                Triple("⚙️", "Settings", Screen.Settings.route)
            )

            val currentRoute = navController.currentBackStackEntryAsState()?.value?.destination?.route

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                drawerItems.forEach { (icon, label, route) ->
                    val isSelected = currentRoute == route

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        color = if (isSelected) Color(0xFFFF6B35).copy(alpha = 0.2f) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = icon,
                                fontSize = 20.sp,
                                modifier = Modifier.shadow(if (isSelected) 4.dp else 0.dp)
                            )

                            Text(
                                text = label,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFFFD23F) else Color.White.copy(alpha = 0.9f)
                            )

                            if (isSelected) {
                                Spacer(modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFFFFD23F), shape = CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Text(
                text = "Build your neon empire! 🌟",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// -----------------------------
// Floating Action Button - Contextual Actions
// -----------------------------
@Composable
fun StreetTycoonFAB(navController: NavHostController, currentRoute: String?) {
    val scope = rememberCoroutineScope()
    val windowSize = rememberWindowSize()
    val isCompact = windowSize == WindowSize.Compact
    val fabSize = if (isCompact) 56.dp else 64.dp
    val iconSize = if (isCompact) 22.dp else 24.dp

    when (currentRoute) {
        Screen.Map.route -> {
            // Quick action to go to shop
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.Shop.route) {
                        launchSingleTop = true
                    }
                },
                containerColor = Color(0xFFFF6B35),
                contentColor = Color.White,
                modifier = Modifier
                    .size(fabSize)
                    .shadow(if (isCompact) 6.dp else 8.dp, shape = CircleShape)
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Go to Shop",
                    modifier = Modifier.size(iconSize)
                )
            }
        }
        Screen.Shop.route -> {
            // Quick action to go back to map
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Map.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White,
                modifier = Modifier
                    .size(fabSize)
                    .shadow(if (isCompact) 6.dp else 8.dp, shape = CircleShape)
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = "Go to Map",
                    modifier = Modifier.size(iconSize)
                )
            }
        }
        Screen.Characters.route -> {
            // Quick action to go to shop (to hire characters)
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.Shop.route) {
                        launchSingleTop = true
                    }
                },
                containerColor = Color(0xFFFF9800),
                contentColor = Color.White,
                modifier = Modifier
                    .size(fabSize)
                    .shadow(if (isCompact) 6.dp else 8.dp, shape = CircleShape)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Hire Team",
                    modifier = Modifier.size(iconSize)
                )
            }
        }
        Screen.Family.route -> {
            // Quick action to view stats on map
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Map.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                containerColor = Color(0xFF9C27B0),
                contentColor = Color.White,
                modifier = Modifier
                    .size(fabSize)
                    .shadow(if (isCompact) 6.dp else 8.dp, shape = CircleShape)
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Go to Map",
                    modifier = Modifier.size(iconSize)
                )
            }
        }
        Screen.Settings.route -> {
            // Quick action to go back to map
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Map.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                containerColor = Color(0xFF607D8B),
                contentColor = Color.White,
                modifier = Modifier
                    .size(fabSize)
                    .shadow(if (isCompact) 6.dp else 8.dp, shape = CircleShape)
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Go to Map",
                    modifier = Modifier.size(iconSize)
                )
            }
        }
        else -> {
            // For stall screen and others, show quick navigation to map
            if (currentRoute?.startsWith("stall/") == true) {
                FloatingActionButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(fabSize)
                        .shadow(if (isCompact) 6.dp else 8.dp, shape = CircleShape)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Go Back",
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}
fun formatCash(amount: Double): String {
    return when {
        amount >= 1_000_000_000 -> String.format("%.2fB", amount / 1_000_000_000)
        amount >= 1_000_000 -> String.format("%.2fM", amount / 1_000_000)
        amount >= 1_000 -> String.format("%.2fK", amount / 1_000)
        else -> String.format("%.0f", amount)
    }
}
