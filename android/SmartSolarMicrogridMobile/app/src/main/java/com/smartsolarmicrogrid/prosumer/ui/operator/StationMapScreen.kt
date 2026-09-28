/*
 * StationMapScreen.kt
 * Smart Solar Microgrid Trading System - Prosumer Mobile Application
 *
 * Plots the microgrid stations on Google Maps using the latitude and longitude
 * stored in SolarStationInfo. Markers are built from API data - none are
 * hardcoded - and selecting one shows that station's details.
 */
package com.smartsolarmicrogrid.prosumer.ui.operator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.smartsolarmicrogrid.prosumer.data.model.Station

@Composable
fun StationMapScreen(
    operatorViewModel: OperatorViewModel = viewModel()
) {
    val state = operatorViewModel.stationState
    var selectedStation by remember { mutableStateOf<Station?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        when (state) {
            is StationMapState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceGray),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SolarGreen)
                }
            }

            is StationMapState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceGray)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.CloudOff, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(state.message, color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { operatorViewModel.loadStations() },
                        colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }

            is StationMapState.Loaded -> {
                val stations = state.stations

                // Centre the camera on the first station, falling back to Colombo.
                val start = stations.firstOrNull()
                    ?.let { LatLng(it.latitude, it.longitude) }
                    ?: LatLng(6.9271, 79.8612)

                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(start, 9f)
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = false),
                    uiSettings = MapUiSettings(zoomControlsEnabled = true, mapToolbarEnabled = false),
                    onMapClick = { selectedStation = null }
                ) {
                    stations.forEach { station ->
                        Marker(
                            state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                            title = station.stationName,
                            snippet = station.address,
                            onClick = {
                                selectedStation = station
                                false // let the default info window open as well
                            }
                        )
                    }
                }

                // Floating header over the map
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(SolarGreenDark.copy(alpha = 0.92f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Nearby Stations",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${stations.size} microgrid nodes on the map",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = { operatorViewModel.loadStations() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                }

                // Station details for the selected marker
                selectedStation?.let { station ->
                    StationDetailsCard(
                        station = station,
                        onDismiss = { selectedStation = null },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

/** Details shown when the operator taps a station marker. */
@Composable
private fun StationDetailsCard(
    station: Station,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SolarGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.SolarPower, contentDescription = null, tint = SolarGreen)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(station.stationName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(station.address, fontSize = 11.sp, color = Color.Gray)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = SurfaceGray)
            Spacer(modifier = Modifier.height(12.dp))

            StationRow(Icons.Filled.Bolt, "Capacity", "${station.capacity} kW/h")
            StationRow(Icons.Filled.BatteryChargingFull, "Battery slots", station.batterySlotCount.toString())
            StationRow(
                Icons.Filled.Schedule,
                "Operating hours",
                "${station.operatingStartTime} - ${station.operatingEndTime}"
            )
            StationRow(Icons.Filled.Info, "Status", station.status)
            StationRow(
                Icons.Filled.LocationOn,
                "Coordinates",
                "${station.latitude}, ${station.longitude}"
            )
        }
    }
}

/** One labelled line in the station details card. */
@Composable
private fun StationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = SolarGreen, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}