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

private val SolarGreenDark = Color(0xFF1B5E20)
private val SolarGreen = Color(0xFF2E7D32)
private val SolarGreenLight = Color(0xFFE8F5E9)
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
    var isMapView by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SolarGreenDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.WbSunny, contentDescription = null, tint = SolarAmber, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Find Stations", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Smart Solar Microgrid", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
                
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // White Card Content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Search Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(50.dp)
                            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text("Search by city, station name or area...", color = Color.Gray, fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filterOptions) { option ->
                            val isSelected = selectedFilter == option
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) SolarGreenDark else Color.Transparent)
                                    .border(1.dp, if (isSelected) SolarGreenDark else Color(0xFFEEEEEE), RoundedCornerShape(20.dp))
                                    .clickable { selectedFilter = option }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    option,
                                    color = if (isSelected) Color.White else Color.DarkGray,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                )
                            }
                        }
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
                                .background(if (!isMapView) Color.White else Color.Transparent)
                                .clickable { isMapView = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.FormatListBulleted, contentDescription = null, tint = if (!isMapView) SolarGreenDark else Color.Gray, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("List", color = if (!isMapView) SolarGreenDark else Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Map Toggle
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isMapView) SolarGreenDark else Color.Transparent)
                                .clickable { isMapView = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Map, contentDescription = null, tint = if (isMapView) Color.White else Color.Gray, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Map", color = if (isMapView) Color.White else Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    when (state) {
                        is StationListState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = SolarGreenDark)
                            }
                        }
                        is StationListState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(state.message, color = Color.Red)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = { stationViewModel.loadStations() }, colors = ButtonDefaults.buttonColors(containerColor = SolarGreenDark)) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                        is StationListState.Loaded -> {
                            val stations = state.stations.filter {
                                val matchesSearch = it.stationName.contains(searchQuery, ignoreCase = true) || it.address.contains(searchQuery, ignoreCase = true)
                                
                                val isActive = it.status.uppercase() == "ACTIVE" || it.status.uppercase() == "AVAILABLE" || it.status == "0"
                                
                                // Mock available slots for filtering
                                val mockReserved = (it.stationName.length) % (it.batterySlotCount + 1)
                                val availableSlots = it.batterySlotCount - mockReserved
                                
                                val matchesFilter = when (selectedFilter) {
                                    "Near Me" -> it.stationId == "ST001" || it.stationId == "ST002" // Mock "Near Me" condition
                                    "Available" -> availableSlots > 0 && isActive
                                    else -> true
                                }
                                
                                matchesSearch && matchesFilter
                            }.sortedByDescending { 
                                it.status.uppercase() == "ACTIVE" || it.status.uppercase() == "AVAILABLE" || it.status == "0"
                            }

                            if (isMapView) {
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
                                // List View
                                if (stations.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                        Text("No stations found.", color = Color.Gray, modifier = Modifier.padding(top = 40.dp))
                                    }
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(stations) { station ->
                                            StationCardV2(station = station, onClick = { onStationSelected(station) })
                                        }
                                        item { Spacer(modifier = Modifier.height(32.dp)) }
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
private fun StationCardV2(station: Station, onClick: () -> Unit) {
    val isStationActive = station.status.uppercase() == "ACTIVE" || station.status.uppercase() == "AVAILABLE" || station.status == "0"
    
    Card(
        onClick = { if (isStationActive) onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isStationActive) 2.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(16.dp))
            .alpha(if (isStationActive) 1f else 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image Placeholder (Using local drawable images)
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
                    painter = androidx.compose.ui.res.painterResource(id = imgRes),
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
                    fontSize = 16.sp, 
                    color = Color(0xFF1E293B),
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val city = station.address.split(",").firstOrNull() ?: station.address
                    Text(city, fontSize = 13.sp, color = Color(0xFF64748B), maxLines = 1)
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val distance = when(station.stationId) {
                        "ST001" -> "1.2 km"
                        "ST002" -> "10.2 km"
                        "ST003" -> "11.9 km"
                        else -> "2.1 km" // Mock distance matching the UI reference
                    }
                    Text(distance, fontSize = 13.sp, color = Color(0xFF64748B))
                    
                    val isMaintenance = station.status.uppercase() == "MAINTENANCE"
                    val mockReserved = (station.stationName.length) % (station.batterySlotCount + 1)
                    val availableSlots = if (isMaintenance || !isStationActive) 0 else (station.batterySlotCount - mockReserved)
                    
                    val (statusText, bgColor, textColor) = when {
                        !isStationActive -> Triple("Unavailable", Color(0xFFF1F5F9), Color(0xFF64748B)) // Gray
                        isMaintenance -> Triple("Maintenance", Color(0xFFF1F5F9), Color(0xFF64748B)) // Gray
                        availableSlots == 0 -> Triple("Fully Booked", Color(0xFFFFEBEE), Color(0xFFC62828)) // Red
                        availableSlots <= 2 -> Triple("Almost Full", Color(0xFFFFF3E0), SolarAmber) // Amber
                        else -> Triple("Available", SolarGreenLight, SolarGreen) // Green
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            statusText, 
                            color = textColor, 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
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
        Box(modifier = modifier.background(Color.LightGray)) // placeholder
    }
}
