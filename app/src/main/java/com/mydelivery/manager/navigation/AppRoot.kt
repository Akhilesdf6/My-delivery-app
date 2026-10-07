package com.mydelivery.manager.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mydelivery.manager.ui.screens.CodScreen
import com.mydelivery.manager.ui.screens.CustomersScreen
import com.mydelivery.manager.ui.screens.DeliveriesScreen
import com.mydelivery.manager.ui.screens.HomeScreen
import com.mydelivery.manager.ui.screens.MapScreen
import com.mydelivery.manager.ui.screens.MoreScreen
import com.mydelivery.manager.ui.screens.PlaceholderScreen

private fun iconFor(screen: Screen): ImageVector = when (screen) {
    Screen.Home -> Icons.Filled.Home
    Screen.Deliveries -> Icons.AutoMirrored.Filled.List
    Screen.Customers -> Icons.Filled.Person
    Screen.Map -> Icons.Filled.Place
    else -> Icons.Filled.Menu
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.bottomTabs.forEach { screen ->
                    val selected = if (screen == Screen.More) {
                        destination?.hierarchy?.any { d ->
                            d.route == Screen.More.route || Screen.moreItems.any { it.route == d.route }
                        } == true
                    } else {
                        destination?.hierarchy?.any { it.route == screen.route } == true
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(iconFor(screen), contentDescription = null) },
                        label = { Text(stringResource(screen.title)) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Deliveries.route) { DeliveriesScreen() }
            composable(Screen.Customers.route) { CustomersScreen() }
            composable(Screen.Map.route) { MapScreen() }
            composable(Screen.Cod.route) { CodScreen() }
            composable(Screen.More.route) { 
                MoreScreen(onOpen = { navController.navigate(it.route) { launchSingleTop = true } }) 
            }
            
            val implementedScreens = setOf(
                Screen.Home, Screen.Deliveries, Screen.Customers, 
                Screen.Map, Screen.Cod, Screen.More
            )
            
            // Remaining placeholders
            (Screen.bottomTabs + Screen.moreItems).filter { it !in implementedScreens }.forEach { screen ->
                composable(screen.route) {
                    PlaceholderScreen(title = stringResource(screen.title), stage = screen.stage)
                }
            }
        }
    }
}
