package com.smartsolarmicrogrid.prosumer.ui.operator

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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

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
    val greenBg = Color(0xFF0C8A44)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC)) // Light gray background for the bottom part
    ) {
        // Green Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(greenBg)
        ) {
            // Subtle background decoration (solar panel)
            Icon(
                imageVector = Icons.Filled.SolarPower,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.08f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 40.dp, y = 20.dp)
                    .size(160.dp)
                    .rotate(-15f)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .padding(top = 16.dp), // Safe area inset approx
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF33A867)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Engineering,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Welcome text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Good Morning,",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Operator",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Bell Icon
                IconButton(onClick = onLogout) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = "Alerts",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Main scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 150.dp) // Start overlapping the header slightly
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stats Grid
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatTile(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.SolarPower, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(28.dp))
                            }
                        },
                        value = operatorViewModel.stationCount.toString(),
                        label = "Active Stations",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF3E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.PendingActions, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(28.dp))
                            }
                        },
                        value = operatorViewModel.pendingCount.toString(),
                        label = "Pending\nReservations",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatTile(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE3F2FD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.BatteryChargingFull, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(24.dp))
                                }
                            }
                        },
                        value = operatorViewModel.availableSlotCount.toString(),
                        label = "Available Slots",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFEBEE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.BatterySaver, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(28.dp))
                            }
                        },
                        value = operatorViewModel.reservedSlotCount.toString(),
                        label = "Reserved Slots",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions Header
            Text(
                "Quick Actions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1B),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Filled.Map,
                    text = "Station Map",
                    color = Color(0xFF2196F3),
                    bgColor = Color(0xFFE3F2FD),
                    onClick = onViewMap,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Filled.PendingActions,
                    text = "Pending Reservations",
                    color = Color(0xFFFF9800),
                    bgColor = Color(0xFFFFF3E0),
                    onClick = onViewReservations,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Recent Activity Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Activity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1B)
                )
                Text(
                    "View All",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = greenBg,
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
                        HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp))
                    }
                }
                
                if (operatorViewModel.recentActivity.isEmpty()) {
                    Text("No recent activity.", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun StatTile(
    icon: @Composable () -> Unit,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth() // Let the card determine its own height based on content
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp), // Increase vertical padding to give it space
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon()
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 13.sp, color = Color(0xFF6B7280), textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun OperatorActivityRow(item: OperatorActivityItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (iconColor, iconBgColor) = when (item.status.uppercase()) {
            "PENDING", "0" -> Pair(Color(0xFFFF9800), Color(0xFFFFF3E0)) // Amber
            "CANCELLED" -> Pair(Color(0xFFE53935), Color(0xFFFFEBEE)) // Red
            "COMPLETED", "2" -> Pair(Color(0xFF2196F3), Color(0xFFE3F2FD)) // Blue
            else -> Pair(Color(0xFF4CAF50), Color(0xFFE8F5E9)) // Green
        }
        
        val icon = when (item.status.uppercase()) {
            "PENDING", "0" -> Icons.Filled.AccessTime
            "CANCELLED" -> Icons.Filled.Close
            "COMPLETED", "2" -> Icons.Filled.CheckCircle
            else -> Icons.Filled.Check
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text(item.subtitle, fontSize = 14.sp, color = Color(0xFF6B7280))
        }
        Text(item.date, fontSize = 13.sp, color = Color(0xFF6B7280))
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    text: String,
    color: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text, 
                fontSize = 13.sp, 
                fontWeight = FontWeight.SemiBold, 
                color = Color(0xFF374151),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}
