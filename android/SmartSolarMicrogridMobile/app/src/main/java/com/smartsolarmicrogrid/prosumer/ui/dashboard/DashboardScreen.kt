/*
 * DashboardScreen.kt
 * Smart Solar Microgrid Trading System - Prosumer Mobile Application
 *
 * Prosumer dashboard: pending reservation count, approved future reservation
 * count and the next upcoming bookings, all read live from the C# Web API.
 */
package com.smartsolarmicrogrid.prosumer.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsolarmicrogrid.prosumer.data.model.Reservation
import com.smartsolarmicrogrid.prosumer.ui.bookinglist.StatusBadge

private val SolarGreenDark = Color(0xFF1B5E20)
private val SolarGreen = Color(0xFF2E7D32)
private val SolarAmber = Color(0xFFFFA000)
private val SurfaceGray = Color(0xFFF5F5F5)

@Composable
fun DashboardScreen(
    onViewAllBookings: () -> Unit,
    onBack: () -> Unit,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val state = dashboardViewModel.dashboardState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SolarGreenDark, SolarGreen, SurfaceGray),
                    startY = 0f,
                    endY = 480f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dashboard", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Your reservation summary",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = { dashboardViewModel.loadDashboard() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.White)
                }
            }

            when (state) {
                is DashboardState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                is DashboardState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.CloudOff, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(state.message, color = Color.White, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { dashboardViewModel.loadDashboard() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Retry", color = SolarGreenDark)
                        }
                    }
                }

                is DashboardState.Loaded -> {
                    val data = state.data

                    // Greeting
                    Text(
                        text = "Hello, ${data.prosumerName}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // The two counts required by the rubric
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = Icons.Filled.HourglassTop,
                            value = data.pendingCount.toString(),
                            label = "Pending reservations",
                            accent = SolarAmber,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = Icons.Filled.EventAvailable,
                            value = data.approvedFutureCount.toString(),
                            label = "Approved upcoming",
                            accent = SolarGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Upcoming bookings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Upcoming bookings",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1B1B1B)
                        )
                        TextButton(onClick = onViewAllBookings) {
                            Text("View all", color = SolarGreen, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (data.upcoming.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.EventBusy, contentDescription = null, tint = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No upcoming bookings", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            data.upcoming.forEach { reservation ->
                                UpcomingBookingCard(reservation)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onViewAllBookings,
                        colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Filled.EventNote, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Go to My Bookings")
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

/** One of the two headline count cards. */
@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B1B1B))
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

/** A compact row for one of the next bookings. */
@Composable
private fun UpcomingBookingCard(reservation: Reservation) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SolarGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.BatteryChargingFull, contentDescription = null, tint = SolarGreen)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Station ${reservation.stationId}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "${reservation.bookingDate}  •  ${reservation.startTime}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            StatusBadge(status = reservation.status)
        }
    }
}