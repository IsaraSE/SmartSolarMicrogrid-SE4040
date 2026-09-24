package com.smartsolarmicrogrid.prosumer.ui.station

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsolarmicrogrid.prosumer.R

private val GreenDark = Color(0xFF145A32)
private val GreenPrimary = Color(0xFF1B8A4A)
private val GreenLight = Color(0xFF2ECC71)

@Composable
fun StationDetailsScreen(
    onNavigateToSlots: () -> Unit,
    onBack: () -> Unit,
    stationViewModel: StationViewModel = viewModel()
) {
    val station = stationViewModel.selectedStation

    if (station == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Station details not available", color = Color(0xFF94A3B8))
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Image with overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                val imageRes = getStationImageRes(station)
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Station Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay at top for back button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )

                // Back button
                Box(
                    modifier = Modifier
                        .padding(top = 48.dp, start = 20.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(22.dp))
                }

                // Station status badge
                val isActive = station.status.uppercase() == "ACTIVE" || station.status.uppercase() == "AVAILABLE" || station.status == "0"
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) Color(0xFF43A047) else Color(0xFFE53935)
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 20.dp)
                ) {
                    Text(
                        if (isActive) "● Active" else "● Inactive",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Content Card overlapping image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Station Name
                    Text(
                        text = station.stationName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A2E)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Location Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("2.5 km away", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A2E))
                            Text(station.address, fontSize = 13.sp, color = Color(0xFF94A3B8), lineHeight = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Row - 3 cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val slotState = stationViewModel.slotListState
                        val availableSlotsCount = when (slotState) {
                            is SlotListState.Loaded -> slotState.slots.count { it.status.uppercase() == "AVAILABLE" }.toString()
                            is SlotListState.Loading -> "..."
                            else -> "-"
                        }

                        StationInfoCard(
                            icon = Icons.Filled.BatteryChargingFull,
                            label = "Capacity",
                            value = "${station.capacity} kWh",
                            iconTint = Color(0xFF1E88E5),
                            iconBg = Color(0xFFE3F2FD),
                            modifier = Modifier.weight(1f)
                        )
                        StationInfoCard(
                            icon = Icons.Filled.CheckCircle,
                            label = "Available",
                            value = availableSlotsCount,
                            iconTint = Color(0xFF43A047),
                            iconBg = Color(0xFFE8F5E9),
                            modifier = Modifier.weight(1f)
                        )
                        StationInfoCard(
                            icon = Icons.Filled.Schedule,
                            label = "Hours",
                            value = "${station.operatingStartTime}\n${station.operatingEndTime}",
                            iconTint = Color(0xFFFF9800),
                            iconBg = Color(0xFFFFF3E0),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // CTA Button
                    Button(
                        onClick = onNavigateToSlots,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(listOf(GreenDark, GreenPrimary)),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("View Available Slots", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun StationInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E), textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
        }
    }
}
