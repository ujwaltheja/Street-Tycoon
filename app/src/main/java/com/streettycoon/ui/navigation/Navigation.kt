package com.streettycoon.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.streettycoon.ui.GameViewModel
import com.streettycoon.ui.screens.CharacterScreen
import com.streettycoon.ui.screens.FamilyScreen
import com.streettycoon.ui.screens.MapScreen
import com.streettycoon.ui.screens.SettingsScreen
import com.streettycoon.ui.screens.ShopScreen
import com.streettycoon.ui.screens.StallScreen

sealed class Screen(val route: String, val title: String) {
    object Map : Screen("map", "Map")
    object Stall : Screen("stall/{stallId}", "Stall")
    object Shop : Screen("shop", "Shop")
    object Characters : Screen("characters", "Characters")
    object Family : Screen("family", "Family")
    object Settings : Screen("settings", "Settings")
}

@Composable
fun StreetTycoonApp(viewModel: GameViewModel) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { StreetTycoonTopBar(viewModel) },
        bottomBar = { StreetTycoonNavigationBar(navController) }
    ) { paddingValues ->
        NavigationGraph(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun StreetTycoonTopBar(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()

    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp, color = Color.Transparent) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF1A0A2A), Color(0xFF6B1D5C), Color(0xFFFF6F61))
                    )
                )
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Street Tycoon",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                gameState?.let { state ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatChip(
                            label = "Cash",
                            value = "₹${formatCash(state.playerCash)}"
                        )
                        StatChip(
                            label = "Tokens",
                            value = state.playerTokens.toString()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Surface(
        color = Color.White.copy(alpha = 0.16f),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun StreetTycoonNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Map to Icons.Filled.Place,
        Screen.Characters to Icons.Filled.Person,
        Screen.Family to Icons.Filled.Home,
        Screen.Shop to Icons.Filled.ShoppingCart,
        Screen.Settings to Icons.Filled.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        items.forEach { (screen, icon) ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) {
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFFF8A80), Color(0xFFFF7043))
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.06f),
                                            Color.White.copy(alpha = 0.06f)
                                        )
                                    )
                                }
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = screen.title,
                            tint = Color.White
                        )
                    }
                },
                label = { Text(screen.title) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = Color.White.copy(alpha = 0.7f),
                    unselectedTextColor = Color.White.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Map.route,
        modifier = modifier
    ) {
        composable(Screen.Map.route) {
            MapScreen(
                viewModel = viewModel,
                onStallClick = { stallId ->
                    navController.navigate("stall/$stallId")
                }
            )
        }

        composable(Screen.Stall.route) { backStackEntry ->
            val stallId = backStackEntry.arguments?.getString("stallId")?.toIntOrNull()
            if (stallId != null) {
                StallScreen(
                    viewModel = viewModel,
                    stallId = stallId,
                    onBack = { navController.popBackStack() }
                )
            } else {
                // Invalid stall ID, show error and go back
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text(
                            text = "Invalid stall",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }

        composable(Screen.Shop.route) {
            ShopScreen(viewModel = viewModel)
        }

        composable(Screen.Characters.route) {
            CharacterScreen(viewModel = viewModel)
        }

        composable(Screen.Family.route) {
            FamilyScreen(viewModel = viewModel)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                audioManager = viewModel.audioManager,
                onNavigateBack = { /* No back navigation needed in main nav */ }
            )
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
