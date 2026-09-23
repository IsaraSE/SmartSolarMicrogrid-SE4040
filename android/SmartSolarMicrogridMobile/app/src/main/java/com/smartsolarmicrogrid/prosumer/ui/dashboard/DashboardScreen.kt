package com.smartsolarmicrogrid.prosumer.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsolarmicrogrid.prosumer.data.model.Reservation
import com.smartsolarmicrogrid.prosumer.ui.bookinglist.StatusBadge
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val SolarGreenDark = Color(0xFF1B5E20)
private val SolarGreen = Color(0xFF2E7D32)
private val SolarGreenLight = Color(0xFFE8F5E9)
private val SolarAmber = Color(0xFFFFA000)
private val SolarAmberLight = Color(0xFFFFF8E1)
private val SolarBlue = Color(0xFF1976D2)
private val SolarBlueLight = Color(0xFFE3F2FD)

@Composable
fun DashboardScreen(
    onNavigateToStations: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onBookingSelected: (Reservation) -> Unit,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val state = dashboardViewModel.dashboardState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SolarGreenDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(48.dp))

            // Top Header (Green Area)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.WbSunny, contentDescription = null, tint = SolarAmber, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Smart Solar Microgrid", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Prosumer Portal", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
                
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White) // Mockup shows a white bell with no red dot, or maybe a white bell? Wait, mockup 2 has a white bell with no red dot.
                            // Actually it looks like a white bell. I'll just leave it as white bell.
                            // Oh wait, the previous mockup had a red dot. Let's just remove the red dot.
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // The Massive White Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (state is DashboardState.Loading) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SolarGreen)
                        }
                    } else if (state is DashboardState.Loaded) {
                        val data = state.data

                        Spacer(modifier = Modifier.height(24.dp))

                        // Greeting & Profile
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Hello,", color = Color.DarkGray, fontSize = 16.sp)
                                Text(data.prosumerName, color = SolarGreenDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Good to see you!", color = Color.Gray, fontSize = 14.sp)
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onNavigateToProfile() }) {
                                Box(
                                    modifier = Modifier.size(56.dp).clip(CircleShape).background(SolarGreenLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Person, contentDescription = null, tint = SolarGreenDark, modifier = Modifier.size(40.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = SolarGreenLight)
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("View Profile", fontSize = 11.sp, color = SolarGreenDark, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp), tint = SolarGreenDark)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Stats Grid (3 cards)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCardV2(
                                icon = Icons.Filled.AccessTime,
                                value = data.pendingCount.toString(),
                                label = "Pending\nReservations",
                                bgColor = SolarAmberLight,
                                iconColor = Color.White,
                                iconBgColor = SolarAmber,
                                modifier = Modifier.weight(1f)
                            )
                            StatCardV2(
                                icon = Icons.Filled.Event,
                                value = data.upcomingCount.toString(),
                                label = "Upcoming\nReservations",
                                bgColor = SolarGreenLight,
                                iconColor = Color.White,
                                iconBgColor = SolarGreen,
                                modifier = Modifier.weight(1f)
                            )
                            StatCardV2(
                                icon = Icons.Filled.Check,
                                value = data.completedCount.toString(),
                                label = "Completed\nReservations",
                                bgColor = SolarBlueLight,
                                iconColor = Color.White,
                                iconBgColor = SolarBlue,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Quick Actions
                        Text("Quick Actions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(horizontal = 24.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Dark Green Button
                            Button(
                                onClick = onNavigateToStations,
                                colors = ButtonDefaults.buttonColors(containerColor = SolarGreenDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Filled.EventNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Book a Slot", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            
                            // Light Green Button
                            Button(
                                onClick = onNavigateToMap,
                                colors = ButtonDefaults.buttonColors(containerColor = SolarGreenLight),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = SolarGreenDark, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Find Stations", color = SolarGreenDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = SolarGreenDark, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Upcoming Reservation Section
                        SectionHeader("My Upcoming Reservation", "View All", onNavigateToBookings)
                        if (data.upcomingReservation != null) {
                            UpcomingReservationCardV2(data.upcomingReservation) {
                                onBookingSelected(data.upcomingReservation)
                            }
                        } else {
                            Text("No upcoming reservations.", color = Color.Gray, modifier = Modifier.padding(horizontal = 24.dp), fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Recent Activity
                        SectionHeader("Recent Activity", "View All", onNavigateToBookings)
                        
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            data.recentActivity.forEachIndexed { index, item ->
                                ActivityRowV2(item) {
                                    onBookingSelected(item.reservation)
                                }
                                if (index < data.recentActivity.size - 1) {
                                    HorizontalDivider(color = Color(0xFFEEEEEE), modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                            if (data.recentActivity.isEmpty()) {
                                Text("No recent activity.", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp), fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onAction() }) {
            Text(actionText, color = SolarGreenDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = SolarGreenDark, modifier = Modifier.size(16.dp))
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun StatCardV2(icon: ImageVector, value: String, label: String, bgColor: Color, iconColor: Color, iconBgColor: Color, modifier: Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.height(130.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(iconBgColor), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = Color.DarkGray, lineHeight = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun UpcomingReservationCardV2(reservation: Reservation, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val stationName = reservation.stationName ?: when (reservation.stationId) {
                    "ST001" -> "Colombo Solar Hub"
                    "ST002" -> "Kandy Solar Hub"
                    "ST003" -> "Galle Solar Hub"
                    else -> "Station ${reservation.stationId}"
                }
                Text(stationName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                StatusBadge(status = reservation.status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val formattedDate = try {
                LocalDate.parse(reservation.bookingDate).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            } catch(e:Exception) { reservation.bookingDate }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(formattedDate, fontSize = 14.sp, color = Color.DarkGray)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val formattedStart = try {
                val parts = reservation.startTime?.split(":") ?: listOf("0", "0")
                val h = parts[0].toInt()
                val m = parts[1]
                val ap = if (h >= 12) "PM" else "AM"
                val dh = if (h == 0) 12 else if (h > 12) h - 12 else h
                "$dh:$m $ap"
            } catch (e: Exception) { reservation.startTime ?: "" }

            val formattedEnd = try {
                val parts = reservation.endTime?.split(":") ?: listOf("0", "0")
                val h = parts[0].toInt()
                val m = parts[1]
                val ap = if (h >= 12) "PM" else "AM"
                val dh = if (h == 0) 12 else if (h > 12) h - 12 else h
                "$dh:$m $ap"
            } catch (e: Exception) { reservation.endTime ?: "" }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("$formattedStart - $formattedEnd", fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun ActivityRowV2(item: ActivityItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bgColor = when (item.status.uppercase()) {
            "PENDING" -> SolarAmber
            "CANCELLED" -> Color(0xFFE53935) // Red
            "COMPLETED" -> Color(0xFF1976D2) // Blue
            else -> SolarGreen
        }
        
        val icon = when (item.status.uppercase()) {
            "PENDING" -> Icons.Filled.AccessTime
            "CANCELLED" -> Icons.Filled.Close
            "COMPLETED" -> Icons.Filled.CheckCircle
            else -> Icons.Filled.Check
        }

        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.subtitle, fontSize = 13.sp, color = Color.Gray)
        }
        
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}
