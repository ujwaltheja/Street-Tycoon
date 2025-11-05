package com.streettycoon.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.streettycoon.ui.GameViewModel
import com.streettycoon.ui.components.AnimatedMoneyCounter
import com.streettycoon.ui.screens.MapScreen
import com.streettycoon.ui.screens.ShopScreen
import com.streettycoon.ui.screens.StallScreen

sealed class Screen(val route: String, val title: String) {
    object Map : Screen("map", "Map")
    object Stall : Screen("stall/{stallId}", "Stall")
    object Shop : Screen("shop", "Shop")
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
                        AnimatedMoneyCounter(
                            targetValue = state.playerCash,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            animationDuration = 500
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
        Screen.Shop to Icons.Default.ShoppingCart
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
            }
        }

        composable(Screen.Shop.route) {
            ShopScreen(viewModel = viewModel)
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
