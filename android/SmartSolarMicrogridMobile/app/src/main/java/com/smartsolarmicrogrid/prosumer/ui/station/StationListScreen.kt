package com.smartsolarmicrogrid.prosumer.ui.station

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsolarmicrogrid.prosumer.data.model.Station

private val SolarGreenDark = Color(0xFF1B5E20)
private val SolarGreen = Color(0xFF2E7D32)
private val SolarAmber = Color(0xFFFFA000)
private val SurfaceGray = Color(0xFFF5F5F5)

@Composable
fun StationListScreen(
    onStationSelected: (Station) -> Unit,
    onBack: () -> Unit,
    stationViewModel: StationViewModel = viewModel()
) {
    val state = stationViewModel.stationListState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SolarGreenDark, SolarGreen, SurfaceGray),
                    startY = 0f,
                    endY = 420f
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

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
                Column {
                    Text("Microgrid Stations", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("Find a station near you", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                when (state) {
                    is StationListState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SolarGreen)
                        }
                    }
                    is StationListState.Error -> {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.WifiOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(state.message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { stationViewModel.loadStations() },
                                    colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    is StationListState.Loaded -> {
                        if (state.stations.isEmpty()) {
                            Text("No stations available right now.", color = Color.Gray, modifier = Modifier.padding(top = 24.dp))
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                            ) {
                                items(state.stations) { station ->
                                    StationCard(station = station, onClick = { onStationSelected(station) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StationCard(station: Station, onClick: () -> Unit) {
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
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(colors = listOf(SolarGreen, SolarGreenDark))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.SolarPower, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(station.stationName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(station.address, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.BatteryChargingFull, contentDescription = null, tint = SolarAmber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${station.capacity} kWh • ${station.batterySlotCount} slots",
                        fontSize = 11.sp,
                        color = SolarGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}