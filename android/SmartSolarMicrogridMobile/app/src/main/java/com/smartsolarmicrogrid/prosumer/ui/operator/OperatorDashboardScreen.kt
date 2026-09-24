package com.smartsolarmicrogrid.prosumer.ui.operator

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.clickable
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

private val GreenDark = Color(0xFF145A32)
private val GreenPrimary = Color(0xFF1B8A4A)
private val GreenLight = Color(0xFF2ECC71)

@Composable
fun OperatorDashboardScreen(
    onScanQr: () -> Unit,
    onViewReservations: () -> Unit,
    onViewAllActivity: () -> Unit,
    onViewMap: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToDetails: (com.smartsolarmicrogrid.prosumer.data.model.Reservation) -> Unit,
    operatorViewModel: OperatorViewModel = viewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Gradient Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(GreenDark, GreenPrimary, GreenLight.copy(alpha = 0.9f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Top Bar with Logo & Notification
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
                            .background(Color.White.copy(alpha = 0.18f)),
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
                        Text("Grid Operator", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Welcome Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Good Morning,",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Operator",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // White Content Area
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(top = 24.dp)) {

                    // Stats Grid - 2x2
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OperatorStatCard(
                                icon = Icons.Filled.SolarPower,
                                value = operatorViewModel.stationCount.toString(),
                                label = "Active Stations",
                                iconTint = Color(0xFF43A047),
                                iconBg = Color(0xFFE8F5E9),
                                modifier = Modifier.weight(1f)
                            )
                            OperatorStatCard(
                                icon = Icons.Filled.PendingActions,
                                value = operatorViewModel.pendingCount.toString(),
                                label = "Pending",
                                iconTint = Color(0xFFFFB300),
                                iconBg = Color(0xFFFFF8E1),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OperatorStatCard(
                                icon = Icons.Filled.BatteryChargingFull,
                                value = operatorViewModel.availableSlotCount.toString(),
                                label = "Available Slots",
                                iconTint = Color(0xFF1E88E5),
                                iconBg = Color(0xFFE3F2FD),
                                modifier = Modifier.weight(1f)
                            )
                            OperatorStatCard(
                                icon = Icons.Filled.BatterySaver,
                                value = operatorViewModel.reservedSlotCount.toString(),
                                label = "Reserved Slots",
                                iconTint = Color(0xFFE53935),
                                iconBg = Color(0xFFFFEBEE),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Quick Actions
                    Text(
                        "Quick Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Station Map - gradient button
                        Button(
                            onClick = onViewMap,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(),
                            modifier = Modifier.weight(1f).height(56.dp).shadow(4.dp, RoundedCornerShape(16.dp))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(
                                    Brush.horizontalGradient(listOf(GreenDark, GreenPrimary)),
                                    RoundedCornerShape(16.dp)
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Icon(Icons.Filled.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Station Map", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Pending Reservations - outlined style
                        OutlinedButton(
                            onClick = onViewReservations,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, GreenPrimary),
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PendingActions, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pending", color = GreenPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Recent Activity Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Activity",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E)
                        )
                        Text(
                            "View All",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary,
                            modifier = Modifier.clickable { onViewAllActivity() }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Recent Activity Feed
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        operatorViewModel.recentActivity.forEachIndexed { index, item ->
                            OperatorActivityRow(item) {
                                onNavigateToDetails(item.reservation)
                            }
                            if (index < operatorViewModel.recentActivity.size - 1) {
                                HorizontalDivider(
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                                )
                            }
                        }

                        if (operatorViewModel.recentActivity.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Filled.Inbox,
                                        contentDescription = null,
                                        tint = Color(0xFFCBD5E1),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("No recent activity", color = Color(0xFF94A3B8), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
private fun OperatorStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                label,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun OperatorActivityRow(item: OperatorActivityItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (iconColor, iconBgColor) = when (item.status.uppercase()) {
            "PENDING", "0" -> Pair(Color(0xFFFF9800), Color(0xFFFFF3E0))
            "CANCELLED" -> Pair(Color(0xFFE53935), Color(0xFFFFEBEE))
            "COMPLETED", "2" -> Pair(Color(0xFF1E88E5), Color(0xFFE3F2FD))
            else -> Pair(Color(0xFF43A047), Color(0xFFE8F5E9))
        }

        val icon = when (item.status.uppercase()) {
            "PENDING", "0" -> Icons.Filled.AccessTime
            "CANCELLED" -> Icons.Filled.Close
            "COMPLETED", "2" -> Icons.Filled.CheckCircle
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(item.subtitle, fontSize = 13.sp, color = Color(0xFF94A3B8))
        }
        Text(item.date, fontSize = 12.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Medium)
    }
}
