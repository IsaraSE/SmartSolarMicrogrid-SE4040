package com.smartsolarmicrogrid.prosumer.ui.dashboard

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF145A32), SolarGreenDark, SolarGreen)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(48.dp))

            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.smartsolarmicrogrid.prosumer.R.drawable.heliogrid_logo),
                            contentDescription = "HelioGrid Logo",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("HelioGrid", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Prosumer Portal", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
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

                        Spacer(modifier = Modifier.height(28.dp))

                        // Stats Grid (3 cards)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCardV2(
                                icon = Icons.Filled.AccessTime,
                                value = data.pendingCount.toString(),
                                label = "Pending",
                                bgColor = Color(0xFFFFF8E1),
                                iconColor = Color.White,
                                iconBgColor = Color(0xFFFFB300),
                                modifier = Modifier.weight(1f)
                            )
                            StatCardV2(
                                icon = Icons.Filled.Event,
                                value = data.upcomingCount.toString(),
                                label = "Upcoming",
                                bgColor = Color(0xFFE8F5E9),
                                iconColor = Color.White,
                                iconBgColor = Color(0xFF43A047),
                                modifier = Modifier.weight(1f)
                            )
                            StatCardV2(
                                icon = Icons.Filled.CheckCircle,
                                value = data.completedCount.toString(),
                                label = "Completed",
                                bgColor = Color(0xFFE3F2FD),
                                iconColor = Color.White,
                                iconBgColor = Color(0xFF1E88E5),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Quick Actions
                        Text("Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), modifier = Modifier.padding(horizontal = 24.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Book a Slot - gradient button
                            Button(
                                onClick = onNavigateToStations,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(),
                                modifier = Modifier.weight(1f).height(56.dp).shadow(4.dp, RoundedCornerShape(16.dp))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(
                                        Brush.horizontalGradient(listOf(SolarGreenDark, SolarGreen)),
                                        RoundedCornerShape(16.dp)
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                                        Icon(Icons.Filled.EventNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Book a Slot", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            // Find Stations - outlined style
                            OutlinedButton(
                                onClick = onNavigateToMap,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, SolarGreen),
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = SolarGreen, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Find Stations", color = SolarGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconBgColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium, lineHeight = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun UpcomingReservationCardV2(reservation: Reservation, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
                Text(stationName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                StatusBadge(status = reservation.status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info chips row
            val formattedDate = try {
                LocalDate.parse(reservation.bookingDate).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            } catch(e:Exception) { reservation.bookingDate }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date chip
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = SolarGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(formattedDate, fontSize = 13.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                    }
                }
                
                // Time chip
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
                
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.AccessTime, contentDescription = null, tint = SolarGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("$formattedStart - $formattedEnd", fontSize = 13.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityRowV2(item: ActivityItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (iconColor, iconBgColor) = when (item.status.uppercase()) {
            "PENDING" -> Pair(Color(0xFFFF9800), Color(0xFFFFF3E0))
            "CANCELLED" -> Pair(Color(0xFFE53935), Color(0xFFFFEBEE))
            "COMPLETED" -> Pair(Color(0xFF1976D2), Color(0xFFE3F2FD))
            else -> Pair(SolarGreen, Color(0xFFE8F5E9))
        }
        
        val icon = when (item.status.uppercase()) {
            "PENDING" -> Icons.Filled.AccessTime
            "CANCELLED" -> Icons.Filled.Close
            "COMPLETED" -> Icons.Filled.CheckCircle
            else -> Icons.Filled.Check
        }

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
            Spacer(modifier = Modifier.height(3.dp))
            Text(item.subtitle, fontSize = 13.sp, color = Color(0xFF94A3B8))
        }
        
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(20.dp))
    }
}
