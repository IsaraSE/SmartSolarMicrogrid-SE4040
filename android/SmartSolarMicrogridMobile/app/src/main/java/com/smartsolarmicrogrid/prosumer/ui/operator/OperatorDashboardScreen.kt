/*
 * OperatorDashboardScreen.kt
 * Smart Solar Microgrid Trading System - Prosumer Mobile Application
 *
 * Grid operator dashboard. Shows live operational figures (reservations
 * awaiting approval, stations covered, transfers completed this shift) and
 * quick actions into the scanner, reservations and map.
 */
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

@Composable
fun OperatorDashboardScreen(
    onScanQr: () -> Unit,
    onViewReservations: () -> Unit,
    onViewMap: () -> Unit,
    onLogout: () -> Unit,
    operatorViewModel: OperatorViewModel = viewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SolarGreenDark, SolarGreen, SurfaceGray),
                    startY = 0f,
                    endY = 560f
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
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Engineering, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Grid Operator", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    Text(
                        operatorViewModel.operatorName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { operatorViewModel.refreshAll() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.White)
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.Filled.Logout, contentDescription = "Log out", tint = Color.White)
                }
            }

            // Highlight card - the operator's main job
            Card(
                onClick = onScanQr,
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(SolarAmber.copy(alpha = 0.16f), Color.White)
                            )
                        )
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SolarAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Scan Transaction QR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Verify a prosumer and finalise the energy transfer",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                "Today at a glance",
                color = Color(0xFF1B1B1B),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    icon = Icons.Filled.PendingActions,
                    value = operatorViewModel.pendingCount.toString(),
                    label = "Awaiting approval",
                    accent = SolarAmber,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    icon = Icons.Filled.TaskAlt,
                    value = operatorViewModel.completedCount.toString(),
                    label = "Completed this shift",
                    accent = SolarGreen,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    icon = Icons.Filled.SolarPower,
                    value = operatorViewModel.stationCount.toString(),
                    label = "Stations covered",
                    accent = Color(0xFF00796B),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Operations",
                color = Color(0xFF1B1B1B),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionRow(
                    icon = Icons.Filled.FactCheck,
                    title = "Reservations",
                    subtitle = "Review and approve prosumer bookings",
                    badge = operatorViewModel.pendingCount.takeIf { it > 0 }?.toString(),
                    onClick = onViewReservations
                )
                ActionRow(
                    icon = Icons.Filled.Map,
                    title = "Nearby Stations",
                    subtitle = "See microgrid nodes on the map",
                    onClick = onViewMap
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/** A compact figure tile on the dashboard. */
@Composable
private fun StatTile(
    icon: ImageVector,
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B1B1B))
            Text(label, fontSize = 10.sp, color = Color.Gray, lineHeight = 13.sp)
        }
    }
}

/** A tappable operations row. */
@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                Icon(icon, contentDescription = null, tint = SolarGreen)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SolarAmber)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}