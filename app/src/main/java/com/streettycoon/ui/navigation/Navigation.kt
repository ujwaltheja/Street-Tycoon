package com.streettycoon.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
        topBar = { TopAppBar(navController, viewModel) },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        NavigationGraph(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(navController: NavHostController, viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()

    TopAppBar(
        title = {
            Column {
                Text("Street Tycoon", style = MaterialTheme.typography.titleLarge)
                gameState?.let { state ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            "₹${formatCash(state.playerCash)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            "| ${state.playerTokens} tokens",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Map to Icons.Default.Place,
        Screen.Characters to Icons.Default.Person,
        Screen.Family to Icons.Default.Home,
        Screen.Shop to Icons.Default.ShoppingCart,
        Screen.Settings to Icons.Default.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { (screen, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
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
