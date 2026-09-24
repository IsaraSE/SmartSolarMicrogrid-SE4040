package com.smartsolarmicrogrid.prosumer.ui.station

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.smartsolarmicrogrid.prosumer.data.model.Station
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val GreenDark = Color(0xFF145A32)
private val GreenPrimary = Color(0xFF1B8A4A)
private val GreenLight = Color(0xFF2ECC71)
private val SolarAmber = Color(0xFFFFA000)

@Composable
fun StationListScreen(
    onStationSelected: (Station) -> Unit,
    onBack: () -> Unit,
    stationViewModel: StationViewModel = viewModel()
) {
    val state = stationViewModel.stationListState

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Near Me", "Available")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Gradient Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
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
                    .padding(horizontal = 20.dp),
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
                        Text("Find Stations", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("HelioGrid", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
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

            // Floating Search Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                            Text("Search by city, station name...", color = Color(0xFFCBD5E1), fontSize = 15.sp)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filterOptions) { option ->
                    val isSelected = selectedFilter == option
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) GreenPrimary else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp),
                        modifier = Modifier.clickable { selectedFilter = option }
                    ) {
                        Text(
                            option,
                            color = if (isSelected) Color.White else Color(0xFF64748B),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        Icon(Icons.Filled.FormatListBulleted, contentDescription = null, tint = if (!stationViewModel.isMapView) GreenPrimary else Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("List", color = if (!stationViewModel.isMapView) GreenPrimary else Color(0xFF94A3B8), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

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
                        Icon(Icons.Filled.Map, contentDescription = null, tint = if (stationViewModel.isMapView) Color.White else Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Map", color = if (stationViewModel.isMapView) Color.White else Color(0xFF94A3B8), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (state) {
                is StationListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
                is StationListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(state.message, color = Color(0xFF64748B), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { stationViewModel.loadStations() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues()
                            ) {
                                Box(
                                    modifier = Modifier.background(Brush.horizontalGradient(listOf(GreenDark, GreenPrimary)), RoundedCornerShape(14.dp)).padding(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                is StationListState.Loaded -> {
                    val stations = state.stations.filter {
                        val matchesSearch = it.stationName.contains(searchQuery, ignoreCase = true) || it.address.contains(searchQuery, ignoreCase = true)
                        
                        val isActive = it.status.uppercase() == "ACTIVE" || it.status.uppercase() == "AVAILABLE" || it.status == "0"
                        
                        val mockReserved = (it.stationName.length) % (it.batterySlotCount + 1)
                        val availableSlots = it.batterySlotCount - mockReserved
                        
                        val matchesFilter = when (selectedFilter) {
                            "Near Me" -> it.stationId == "ST001" || it.stationId == "ST002"
                            "Available" -> availableSlots > 0 && isActive
                            else -> true
                        }
                        
                        matchesSearch && matchesFilter
                    }.sortedByDescending { 
                        it.status.uppercase() == "ACTIVE" || it.status.uppercase() == "AVAILABLE" || it.status == "0"
                    }

                    if (stationViewModel.isMapView) {
                        val sriLanka = LatLng(7.8731, 80.7718)
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(sriLanka, 7f)
                        }
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(bottom = 24.dp).clip(RoundedCornerShape(20.dp))) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState
                            ) {
                                stations.forEach { station ->
                                    val isStationActive = station.status.uppercase() == "ACTIVE" || station.status.uppercase() == "AVAILABLE" || station.status == "0"
                                    val isMaintenance = station.status.uppercase() == "MAINTENANCE"
                                    val isAvailable = isStationActive && !isMaintenance

                                    Marker(
                                        state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                                        title = station.stationName,
                                        snippet = if (isAvailable) "${station.capacity} kWh • ${station.batterySlotCount} slots" else "Currently Unavailable",
                                        icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                                            if (isAvailable) com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN 
                                            else com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
                                        ),
                                        onClick = {
                                            if (isAvailable) {
                                                onStationSelected(station)
                                            } else {
                                                android.widget.Toast.makeText(context, "This station is currently under maintenance or inactive.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                            true
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        if (stations.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.Inbox, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(56.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("No stations found", color = Color(0xFF94A3B8), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(stations) { station ->
                                    StationCardV2(station = station, onClick = { onStationSelected(station) })
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StationCardV2(station: Station, onClick: () -> Unit) {
    val isStationActive = station.status.uppercase() == "ACTIVE" || station.status.uppercase() == "AVAILABLE" || station.status == "0"
    
    Card(
        onClick = { if (isStationActive) onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isStationActive) 1f else 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Station Image with status dot
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                val imgRes = getStationImageRes(station)
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = imgRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Online dot
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (isStationActive) Color(0xFF43A047) else Color(0xFFE53935))
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
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val city = station.address.split(",").firstOrNull() ?: station.address
                    Text(city, fontSize = 13.sp, color = Color(0xFF94A3B8), maxLines = 1, modifier = Modifier.weight(1f, fill = false))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Distance chip
                    val distance = when(station.stationId) {
                        "ST001" -> "1.2 km"
                        "ST002" -> "10.2 km"
                        "ST003" -> "11.9 km"
                        else -> "2.1 km"
                    }
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                    ) {
                        Text(distance, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }

                    // Status chip
                    val isMaintenance = station.status.uppercase() == "MAINTENANCE"
                    val mockReserved = (station.stationName.length) % (station.batterySlotCount + 1)
                    val availableSlots = if (isMaintenance || !isStationActive) 0 else (station.batterySlotCount - mockReserved)
                    
                    val (statusText, bgColor, textColor) = when {
                        !isStationActive -> Triple("Unavailable", Color(0xFFF1F5F9), Color(0xFF64748B))
                        isMaintenance -> Triple("Maintenance", Color(0xFFF1F5F9), Color(0xFF64748B))
                        availableSlots == 0 -> Triple("Fully Booked", Color(0xFFFFEBEE), Color(0xFFC62828))
                        availableSlots <= 2 -> Triple("Almost Full", Color(0xFFFFF3E0), SolarAmber)
                        else -> Triple("Available", Color(0xFFE8F5E9), Color(0xFF43A047))
                    }
                    
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor)
                    ) {
                        Text(statusText, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                }
            }
            
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun NetworkImage(url: String, modifier: Modifier) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            try {
                val connection = java.net.URL(url).openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()
                val decoded = android.graphics.BitmapFactory.decodeStream(inputStream)
                bitmap = decoded.asImageBitmap()
            } catch (e: Exception) { 
                e.printStackTrace() 
            }
        }
    }
    
    if (bitmap != null) {
        Image(bitmap = bitmap!!, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop)
    } else {
        Box(modifier = modifier.background(Color.LightGray))
    }
}
