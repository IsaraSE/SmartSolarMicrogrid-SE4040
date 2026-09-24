/*
 * BottomNavBar.kt
 * HelioGrid - Premium Bottom Navigation
 */
package com.smartsolarmicrogrid.prosumer.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

private val GreenDark = Color(0xFF145A32)
private val GreenPrimary = Color(0xFF1B8A4A)

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard.route, "Home", Icons.Filled.SpaceDashboard),
    BottomNavItem(Screen.Stations.route, "Stations", Icons.Filled.SolarPower),
    BottomNavItem(Screen.BookingList.route, "Bookings", Icons.Filled.EventNote),
    BottomNavItem(Screen.Profile.route, "Profile", Icons.Filled.Person)
)

private val operatorNavItems = listOf(
    BottomNavItem(Screen.OperatorHome.route, "Home", Icons.Filled.Home),
    BottomNavItem(Screen.OperatorStations.route, "Stations", Icons.Filled.SolarPower),
    BottomNavItem(Screen.OperatorScan.route, "Scan", Icons.Filled.QrCodeScanner),
    BottomNavItem(Screen.OperatorReservations.route, "Bookings", Icons.Filled.EventNote),
    BottomNavItem(Screen.Profile.route, "Profile", Icons.Filled.Person)
)

@Composable
fun WithBottomBar(
    navController: NavHostController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = { PremiumBottomBar(navController, currentRoute, bottomNavItems, Screen.Dashboard.route) },
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
        bottomBar = { PremiumBottomBar(navController, currentRoute, operatorNavItems, Screen.OperatorHome.route) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            content()
        }
    }
}

@Composable
private fun PremiumBottomBar(
    navController: NavHostController,
    currentRoute: String,
    items: List<BottomNavItem>,
    homeRoute: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = item.route == currentRoute

                val animatedIconColor by animateColorAsState(
                    targetValue = if (selected) Color.White else Color(0xFF94A3B8),
                    animationSpec = tween(300),
                    label = "iconColor"
                )
                val animatedTextColor by animateColorAsState(
                    targetValue = if (selected) GreenPrimary else Color(0xFFCBD5E1),
                    animationSpec = tween(300),
                    label = "textColor"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(homeRoute) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon with pill background when selected
                    Box(
                        modifier = Modifier
                            .size(if (selected) 42.dp else 36.dp)
                            .clip(CircleShape)
                            .then(
                                if (selected)
                                    Modifier.background(
                                        Brush.linearGradient(listOf(GreenDark, GreenPrimary))
                                    )
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = animatedIconColor,
                            modifier = Modifier.size(if (selected) 22.dp else 22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        item.label,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = animatedTextColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
