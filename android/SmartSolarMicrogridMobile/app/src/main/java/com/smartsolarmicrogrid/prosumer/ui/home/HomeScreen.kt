package com.smartsolarmicrogrid.prosumer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SolarGreenDark = Color(0xFF1B5E20)
private val SolarGreen = Color(0xFF2E7D32)
private val SolarAmber = Color(0xFFFFA000)
private val SurfaceGray = Color(0xFFF5F5F5)

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToStations: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SolarGreenDark, SolarGreen, SurfaceGray),
                    startY = 0f,
                    endY = 500f
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome back",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Prosumer",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = Color.White
                        )
                    }
                }
            }

            // Hero energy card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f)),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "HelioGrid",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Trade energy, track your bookings",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SolarAmber.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.WbSunny,
                            contentDescription = null,
                            tint = SolarAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Menu section on the light background
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Quick Actions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1B1B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                HomeMenuCard(
                    icon = Icons.Filled.SolarPower,
                    title = "Find Stations",
                    subtitle = "Browse microgrid stations and available slots",
                    onClick = onNavigateToStations
                )
                Spacer(modifier = Modifier.height(12.dp))
                HomeMenuCard(
                    icon = Icons.Filled.EventNote,
                    title = "My Bookings",
                    subtitle = "View current, pending and past reservations",
                    onClick = onNavigateToBookings
                )
                Spacer(modifier = Modifier.height(12.dp))
                HomeMenuCard(
                    icon = Icons.Filled.Dashboard,
                    title = "Dashboard",
                    subtitle = "See your reservation summary at a glance",
                    onClick = onNavigateToDashboard
                )
            }
        }
    }
}

@Composable
private fun HomeMenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SolarGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = SolarGreen)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onNavigateToProfile = {},
        onNavigateToStations = {},
        onNavigateToBookings = {},
        onNavigateToDashboard = {}
    )
}