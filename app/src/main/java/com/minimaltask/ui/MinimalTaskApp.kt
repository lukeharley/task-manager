package com.minimaltask.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.minimaltask.ui.screens.AddTaskScreen
import com.minimaltask.ui.screens.EditTaskScreen
import com.minimaltask.ui.screens.FocusScreen
import com.minimaltask.ui.screens.HomeScreen
import com.minimaltask.ui.screens.PremiumScreen
import com.minimaltask.ui.screens.SettingsScreen
import com.minimaltask.ui.screens.StatsScreen
import com.minimaltask.ui.theme.MinimalTaskTheme
import com.minimaltask.viewmodel.BillingViewModel

@Composable
fun MinimalTaskApp(billingViewModel: BillingViewModel = hiltViewModel()) {
    val preferences by billingViewModel.preferencesState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    MinimalTaskTheme(mode = preferences.themeMode) {
        Scaffold(
            bottomBar = {
                val entry by navController.currentBackStackEntryAsState()
                val current = entry?.destination?.route
                NavigationBar {
                    NavigationBarItem(
                        selected = current == Route.Home.path,
                        onClick = { navController.navigate(Route.Home.path) { launchSingleTop = true } },
                        icon = { Icon(Icons.Outlined.Home, null) },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = current == Route.Focus.path,
                        onClick = { navController.navigate(Route.Focus.path) { launchSingleTop = true } },
                        icon = { Icon(Icons.Outlined.Timer, null) },
                        label = { Text("Focus") }
                    )
                    NavigationBarItem(
                        selected = current == Route.Stats.path,
                        onClick = {
                            navController.navigate(if (preferences.premiumActive) Route.Stats.path else Route.Premium.path) {
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Outlined.BarChart, null) },
                        label = { Text("Stats") }
                    )
                    NavigationBarItem(
                        selected = current == Route.Settings.path,
                        onClick = { navController.navigate(Route.Settings.path) { launchSingleTop = true } },
                        icon = { Icon(Icons.Outlined.Settings, null) },
                        label = { Text("Impost.") }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(padding)
            ) {
                composable(Route.Home.path) {
                    HomeScreen(
                        onAdd = { navController.navigate(Route.AddTask.path) },
                        onEdit = { navController.navigate(Route.EditTask.create(it)) },
                        onPremium = { navController.navigate(Route.Premium.path) }
                    )
                }
                composable(Route.AddTask.path) {
                    AddTaskScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    route = Route.EditTask.path,
                    arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                ) { entry ->
                    EditTaskScreen(
                        taskId = entry.arguments?.getInt("taskId") ?: 0,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Route.Focus.path) {
                    FocusScreen(onPremium = { navController.navigate(Route.Premium.path) })
                }
                composable(Route.Stats.path) {
                    StatsScreen(onPremium = { navController.navigate(Route.Premium.path) })
                }
                composable(Route.Settings.path) {
                    SettingsScreen(onPremium = { navController.navigate(Route.Premium.path) })
                }
                composable(Route.Premium.path) {
                    PremiumScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
