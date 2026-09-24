package com.smartsolarmicrogrid.prosumer.ui.station

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsolarmicrogrid.prosumer.R
import kotlin.math.abs

private val SolarGreen = Color(0xFF1B6A27) // Darker green to match UI
private val TextDark = Color(0xFF1A1A1A)
private val TextGrey = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailsScreen(
    onNavigateToSlots: () -> Unit,
    onBack: () -> Unit,
    stationViewModel: StationViewModel = viewModel()
) {
    val station = stationViewModel.selectedStation

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Station Details", fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SolarGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = Color.White
            ) {
                Button(
                    onClick = onNavigateToSlots,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Available Slots", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        if (station != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Determine station image based on ID or name
                val imageRes = getStationImageRes(station)

                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Station Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = station.stationName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Location Distance
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Distance",
                            tint = TextGrey,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2.5 km away",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextGrey
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Address
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Address",
                            tint = TextGrey,
                            modifier = Modifier.size(18.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = station.address,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextGrey
                        )
                    }

                    HorizontalDivider(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )

                    // Details section
                    DetailRow(
                        icon = Icons.Filled.BatteryChargingFull,
                        label = "Total Capacity",
                        value = "${station.capacity} kWh"
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    val slotState = stationViewModel.slotListState
                    val availableSlotsCount = when (slotState) {
                        is SlotListState.Loaded -> slotState.slots.count { it.status.uppercase() == "AVAILABLE" }.toString()
                        is SlotListState.Loading -> "..."
                        else -> "-"
                    }

                    DetailRow(
                        icon = Icons.Filled.CheckCircle,
                        label = "Available Slots",
                        value = availableSlotsCount
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    DetailRow(
                        icon = Icons.Filled.Schedule,
                        label = "Operating Hours",
                        value = "${station.operatingStartTime} - ${station.operatingEndTime}"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Station details not available")
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextGrey,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 13.sp, color = TextGrey, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
    }
}
