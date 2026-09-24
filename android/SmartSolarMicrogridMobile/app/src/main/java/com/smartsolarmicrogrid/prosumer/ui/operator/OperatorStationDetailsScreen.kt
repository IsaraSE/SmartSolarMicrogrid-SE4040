package com.smartsolarmicrogrid.prosumer.ui.operator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.smartsolarmicrogrid.prosumer.ui.station.getStationImageRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsolarmicrogrid.prosumer.ui.station.StationViewModel

@Composable
fun OperatorStationDetailsScreen(
    onNavigateToSlots: () -> Unit,
    onBack: () -> Unit,
    stationViewModel: StationViewModel
) {
    val station = stationViewModel.selectedStation
    val greenBg = Color(0xFF0C8A44)

    if (station == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Station not found.")
        }
        return
    }

    val isOnline = station.status.uppercase() == "ACTIVE" || station.status.uppercase() == "AVAILABLE" || station.status == "0"
    val statusText = if (isOnline) "Active" else "Inactive"
    val statusColor = if (isOnline) Color(0xFF4CAF50) else Color(0xFFF44336)
    
    // Formatting capacity
    val capStr = if (station.capacity % 1 == 0.0) {
        station.capacity.toInt().toString()
    } else {
        station.capacity.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(greenBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .padding(top = 16.dp), // Approximate safe area
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Station Details",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 48.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // Main content card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Station Image
                val imgRes = getStationImageRes(station)
                Image(
                    painter = painterResource(id = imgRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Title and Status Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        station.stationName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        modifier = Modifier.weight(1f)
                    )
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(statusColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(statusText, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Details List Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, Color(0xFFF0F0F0))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Location
                        DetailRow(
                            icon = Icons.Filled.LocationOn,
                            label = "Location",
                            value = station.address.split(",").firstOrNull() ?: station.address
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        
                        // Capacity
                        DetailRow(
                            icon = Icons.Filled.Bolt,
                            label = "Capacity",
                            value = "$capStr kW"
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        
                        // Battery Slots
                        DetailRow(
                            icon = Icons.Filled.BatteryChargingFull,
                            label = "Battery Slots",
                            value = station.batterySlotCount.toString()
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        
                        // Operating Hours
                        DetailRow(
                            icon = Icons.Filled.Schedule,
                            label = "Operating Hours",
                            value = "${station.operatingStartTime} - ${station.operatingEndTime}"
                        )
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        
                        // Status
                        DetailRow(
                            icon = Icons.Filled.BarChart,
                            label = "Status",
                            value = statusText,
                            valueColor = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(32.dp))

                // View Slots Button
                Button(
                    onClick = onNavigateToSlots,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = greenBg)
                ) {
                    Text("View Slots", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color(0xFF111827)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 16.sp, color = Color(0xFF6B7280), modifier = Modifier.weight(1f))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}
