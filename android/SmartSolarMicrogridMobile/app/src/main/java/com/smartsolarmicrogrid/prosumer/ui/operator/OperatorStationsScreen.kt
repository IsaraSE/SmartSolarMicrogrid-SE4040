package com.smartsolarmicrogrid.prosumer.ui.operator

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.smartsolarmicrogrid.prosumer.ui.station.getStationImageRes
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
import androidx.compose.material.icons.filled.Inbox
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsolarmicrogrid.prosumer.data.model.Station
import com.smartsolarmicrogrid.prosumer.ui.station.StationViewModel
import com.smartsolarmicrogrid.prosumer.ui.station.StationListState

private val GreenDark = Color(0xFF145A32)
private val GreenPrimary = Color(0xFF1B8A4A)
private val GreenLight = Color(0xFF2ECC71)

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Gradient Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(GreenDark, GreenPrimary, GreenLight.copy(alpha = 0.9f))
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Stations",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                // Invisible spacer for centering
                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar - floating card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text("Search stations...", color = Color(0xFFCBD5E1), fontSize = 15.sp)
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = Color(0xFF1A1A2E))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Map / List Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(4.dp)
            ) {
                // List Toggle
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (!stationViewModel.isMapView)
                                Modifier.shadow(2.dp, RoundedCornerShape(10.dp)).background(Color.White)
                            else Modifier.background(Color.Transparent)
                        )
                        .clickable { stationViewModel.isMapView = false },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.FormatListBulleted, 
                            contentDescription = null, 
                            tint = if (!stationViewModel.isMapView) GreenPrimary else Color(0xFF94A3B8), 
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "List", 
                            color = if (!stationViewModel.isMapView) GreenPrimary else Color(0xFF94A3B8), 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Map Toggle
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (stationViewModel.isMapView)
                                Modifier.shadow(2.dp, RoundedCornerShape(10.dp)).background(GreenPrimary)
                            else Modifier.background(Color.Transparent)
                        )
                        .clickable { stationViewModel.isMapView = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Map, 
                            contentDescription = null, 
                            tint = if (stationViewModel.isMapView) Color.White else Color(0xFF94A3B8), 
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Map", 
                            color = if (stationViewModel.isMapView) Color.White else Color(0xFF94A3B8), 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Station List
            when (state) {
                is StationListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
                is StationListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red, modifier = Modifier.padding(16.dp))
                    }
                }
                is StationListState.Loaded -> {
                    if (filteredStations.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Inbox, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(56.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No stations found", color = Color(0xFF94A3B8), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    } else {
                        if (stationViewModel.isMapView) {
                            // Map View
                            val sriLanka = LatLng(7.8731, 80.7718)
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(sriLanka, 7f)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 24.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            ) {
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
                                verticalArrangement = Arrangement.spacedBy(12.dp)
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

@Composable
private fun OperatorStationCard(station: Station, onClick: (() -> Unit)? = null) {
    val isOnline = station.status.uppercase() == "ACTIVE" || station.status.uppercase() == "AVAILABLE" || station.status == "0"
    
    Card(
        onClick = { onClick?.invoke() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Station Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                val imgRes = getStationImageRes(station)
                Image(
                    painter = painterResource(id = imgRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Status indicator overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) Color(0xFF43A047) else Color(0xFFE53935))
                        .border(2.dp, Color.White, CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    station.stationName, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp, 
                    color = Color(0xFF1A1A2E),
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val city = station.address.split(",").firstOrNull() ?: station.address
                Text(city, fontSize = 13.sp, color = Color(0xFF94A3B8), maxLines = 1)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Status + capacity chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Status chip
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Text(
                            if (isOnline) "Active" else "Inactive",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOnline) Color(0xFF43A047) else Color(0xFFE53935),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    
                    // Capacity chip
                    val capStr = if (station.capacity % 1 == 0.0) station.capacity.toInt().toString() else station.capacity.toString()
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                    ) {
                        Text(
                            "$capStr kW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(22.dp))
        }
    }
}
