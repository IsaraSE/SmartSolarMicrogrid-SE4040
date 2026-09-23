package com.smartsolarmicrogrid.prosumer.ui.operator

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsolarmicrogrid.prosumer.data.model.Station
import com.smartsolarmicrogrid.prosumer.ui.station.StationViewModel
import com.smartsolarmicrogrid.prosumer.ui.station.StationListState

@Composable
fun OperatorStationsScreen(
    onBack: () -> Unit,
    onStationSelected: () -> Unit,
    stationViewModel: StationViewModel
) {
    val state = stationViewModel.stationListState
    var searchQuery by remember { mutableStateOf("") }
    
    val stations = if (state is StationListState.Loaded) state.stations else emptyList()
    
    val filteredStations = stations.filter {
        it.stationName.contains(searchQuery, ignoreCase = true) || 
        it.address.contains(searchQuery, ignoreCase = true)
    }

    val greenBg = Color(0xFF0C8A44)

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
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Stations",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 48.dp) // center alignment compensation
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // Main white rounded container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFFF7F9FC))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEFF1F5))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.width(12.dp))
                        if (searchQuery.isEmpty()) {
                            Text("Search stations...", color = Color(0xFF9CA3AF), fontSize = 16.sp)
                        }
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(start = 36.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = Color(0xFF1F2937))
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Map / List Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(48.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    // List Toggle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!stationViewModel.isMapView) Color.White else Color.Transparent)
                            .clickable { stationViewModel.isMapView = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FormatListBulleted, contentDescription = null, tint = if (!stationViewModel.isMapView) greenBg else Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("List", color = if (!stationViewModel.isMapView) greenBg else Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Map Toggle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (stationViewModel.isMapView) greenBg else Color.Transparent)
                            .clickable { stationViewModel.isMapView = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Map, contentDescription = null, tint = if (stationViewModel.isMapView) Color.White else Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Map", color = if (stationViewModel.isMapView) Color.White else Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Station List
                when (state) {
                    is StationListState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = greenBg)
                        }
                    }
                    is StationListState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.message, color = Color.Red, modifier = Modifier.padding(16.dp))
                        }
                    }
                    is StationListState.Loaded -> {
                        if (filteredStations.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                Text("No stations found.", color = Color.Gray, modifier = Modifier.padding(top = 40.dp))
                            }
                        } else {
                            if (stationViewModel.isMapView) {
                                // Map View
                                val sriLanka = LatLng(7.8731, 80.7718)
                                val cameraPositionState = rememberCameraPositionState {
                                    position = CameraPosition.fromLatLngZoom(sriLanka, 7f)
                                }
                                val context = androidx.compose.ui.platform.LocalContext.current
                                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(bottom = 24.dp).clip(RoundedCornerShape(16.dp))) {
                                    GoogleMap(
                                        modifier = Modifier.fillMaxSize(),
                                        cameraPositionState = cameraPositionState
                                    ) {
                                        filteredStations.forEach { station ->
                                            val isOnline = station.status.uppercase() == "ACTIVE" || station.status.uppercase() == "AVAILABLE" || station.status == "0"

                                            Marker(
                                                state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                                                title = station.stationName,
                                                snippet = if (isOnline) "${station.capacity} kW" else "Currently Inactive",
                                                icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                                                    if (isOnline) com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN 
                                                    else com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
                                                ),
                                                onClick = {
                                                    stationViewModel.selectStationAndLoadAllSlots(station)
                                                    onStationSelected()
                                                    true
                                                }
                                            )
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(filteredStations) { station ->
                                        OperatorStationCard(
                                            station = station,
                                            onClick = { 
                                                stationViewModel.selectStationAndLoadAllSlots(station)
                                                onStationSelected()
                                            }
                                        )
                                    }
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
private fun OperatorStationCard(station: Station, onClick: (() -> Unit)? = null) {
    val isOnline = station.status.uppercase() == "ACTIVE" || station.status.uppercase() == "AVAILABLE" || station.status == "0"
    
    Card(
        onClick = { onClick?.invoke() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Station Image
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            ) {
                val idHash = kotlin.math.abs(station.stationId.hashCode()) % 7
                val imgRes = when(idHash) {
                    0 -> com.smartsolarmicrogrid.prosumer.R.drawable.station_1
                    1 -> com.smartsolarmicrogrid.prosumer.R.drawable.station_2
                    2 -> com.smartsolarmicrogrid.prosumer.R.drawable.station_3
                    3 -> com.smartsolarmicrogrid.prosumer.R.drawable.station_4
                    4 -> com.smartsolarmicrogrid.prosumer.R.drawable.station_5
                    5 -> com.smartsolarmicrogrid.prosumer.R.drawable.station_6
                    else -> com.smartsolarmicrogrid.prosumer.R.drawable.station_7
                }
                Image(
                    painter = painterResource(id = imgRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    station.stationName, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 17.sp, 
                    color = Color(0xFF111827),
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val city = station.address.split(",").firstOrNull() ?: station.address
                Text(city, fontSize = 14.sp, color = Color(0xFF6B7280), maxLines = 1)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) Color(0xFF4CAF50) else Color(0xFFF44336))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isOnline) "Active" else "Inactive", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Medium,
                        color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(16.dp))
                // Format capacity without .0 if it's a whole number
                val capStr = if (station.capacity % 1 == 0.0) {
                    station.capacity.toInt().toString()
                } else {
                    station.capacity.toString()
                }
                Text("${capStr} kW", fontSize = 14.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
            }
        }
    }
}
