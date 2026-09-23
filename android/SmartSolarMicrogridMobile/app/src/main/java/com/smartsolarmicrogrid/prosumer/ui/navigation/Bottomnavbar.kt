/*
 * BottomNavBar.kt
 * Smart Solar Microgrid Trading System - Prosumer Mobile Application
 */
package com.smartsolarmicrogrid.prosumer.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

private val SolarGreen = Color(0xFF2E7D32)

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard.route, "Dashboard", Icons.Filled.SpaceDashboard),
    BottomNavItem(Screen.Stations.route, "Stations", Icons.Filled.SolarPower),
    BottomNavItem(Screen.BookingList.route, "Bookings", Icons.Filled.EventNote),
    BottomNavItem(Screen.Profile.route, "Profile", Icons.Filled.Person)
)

private val operatorNavItems = listOf(
    BottomNavItem(Screen.OperatorHome.route, "Home", Icons.Filled.Home),
    BottomNavItem(Screen.OperatorStations.route, "Stations", Icons.Filled.SolarPower),
    BottomNavItem(Screen.OperatorScan.route, "Scan QR", Icons.Filled.QrCodeScanner),
    BottomNavItem(Screen.OperatorReservations.route, "Reservations", Icons.Filled.EventNote),
    BottomNavItem(Screen.Profile.route, "Profile", Icons.Filled.Person)
)

@Composable
fun WithBottomBar(
    navController: NavHostController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = { AppBottomBar(navController, currentRoute, bottomNavItems, Screen.Dashboard.route) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            content()
        }
    }
}

@Composable
fun WithOperatorBottomBar(
    navController: NavHostController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = { AppBottomBar(navController, currentRoute, operatorNavItems, Screen.OperatorHome.route) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            content()
        }
    }
}

@Composable
private fun AppBottomBar(
    navController: NavHostController,
    currentRoute: String,
    items: List<BottomNavItem>,
    homeRoute: String
) {
    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            val selected = item.route == currentRoute
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(homeRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SolarGreen,
                    selectedTextColor = SolarGreen,
                    indicatorColor = SolarGreen.copy(alpha = 0.12f),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}
