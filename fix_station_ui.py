import os

path = "/Users/isara/4Y_2S/EAD/SmartSolarMicrogrid-SE4040/android/SmartSolarMicrogridMobile/app/src/main/java/com/smartsolarmicrogrid/prosumer/ui/station/StationListScreen.kt"

content = """package com.smartsolarmicrogrid.prosumer.ui.station

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
    val filterOptions = listOf("All", "Colombo", "Kandy", "Galle", "More")
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
                            val isMore = option == "More"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) SolarGreenLight else Color.Transparent)
                                    .border(1.dp, if (isSelected) SolarGreenLight else Color(0xFFEEEEEE), RoundedCornerShape(20.dp))
                                    .clickable { if (!isMore) selectedFilter = option }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!isMore) {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            tint = if (isSelected) SolarGreenDark else Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        option,
                                        color = if (isSelected) SolarGreenDark else Color.DarkGray,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                    if (isMore) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
                                    }
                                }
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
                                val matchesFilter = if (selectedFilter == "All") true else it.address.contains(selectedFilter, ignoreCase = true) || it.stationName.contains(selectedFilter, ignoreCase = true)
                                matchesSearch && matchesFilter
                            }

                            if (isMapView) {
                                // Map View
                                val sriLanka = LatLng(7.8731, 80.7718)
                                val cameraPositionState = rememberCameraPositionState {
                                    position = CameraPosition.fromLatLngZoom(sriLanka, 7f)
                                }
                                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(bottom = 24.dp).clip(RoundedCornerShape(16.dp))) {
                                    GoogleMap(
                                        modifier = Modifier.fillMaxSize(),
                                        cameraPositionState = cameraPositionState
                                    ) {
                                        stations.forEach { station ->
                                            Marker(
                                                state = MarkerState(position = LatLng(station.latitude, station.longitude)),
                                                title = station.stationName,
                                                snippet = "${station.capacity} kWh • ${station.batterySlotCount} slots",
                                                onClick = {
                                                    onStationSelected(station)
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
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image Placeholder (Fetching a solar panel image)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            ) {
                // Using different sample images based on station ID for demo purposes
                val imgUrl = when(station.stationId) {
                    "ST001" -> "https://images.unsplash.com/photo-1509391366360-2e959784a276?q=80&w=400&auto=format&fit=crop"
                    "ST002" -> "https://images.unsplash.com/photo-1508514177221-188b1cf16e9d?q=80&w=400&auto=format&fit=crop"
                    "ST003" -> "https://images.unsplash.com/photo-1521618755572-156ae0cdd74d?q=80&w=400&auto=format&fit=crop"
                    else -> "https://images.unsplash.com/photo-1548611635-b6e7827d7d4a?q=80&w=400&auto=format&fit=crop"
                }
                NetworkImage(url = imgUrl, modifier = Modifier.fillMaxSize())
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text(station.stationName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                    // Mock distance
                    val distance = when(station.stationId) {
                        "ST001" -> "1.2 km"
                        "ST002" -> "102 km"
                        "ST003" -> "119 km"
                        else -> "10 km"
                    }
                    Text(distance, fontSize = 12.sp, color = Color.Gray)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(station.address, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val isAvailable = station.status.uppercase() != "MAINTENANCE"
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isAvailable) SolarGreen else SolarAmber))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isAvailable) "Available" else "Limited", fontSize = 12.sp, color = if (isAvailable) SolarGreenDark else SolarAmber, fontWeight = FontWeight.SemiBold)
                        Text("  •  ${station.batterySlotCount - 2} / ${station.batterySlotCount} slots", fontSize = 11.sp, color = Color.Gray) // Mocking available slots
                    }
                    
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(SolarGreenLight).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("View Slots", color = SolarGreenDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = SolarGreenDark, modifier = Modifier.size(14.dp))
                        }
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
"""

with open(path, "w") as f:
    f.write(content)
print("StationListScreen.kt updated successfully.")
