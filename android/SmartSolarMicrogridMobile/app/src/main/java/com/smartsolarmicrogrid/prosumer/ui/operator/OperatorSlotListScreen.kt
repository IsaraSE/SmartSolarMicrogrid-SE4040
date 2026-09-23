package com.smartsolarmicrogrid.prosumer.ui.operator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsolarmicrogrid.prosumer.data.model.Slot
import com.smartsolarmicrogrid.prosumer.ui.station.SlotListState
import com.smartsolarmicrogrid.prosumer.ui.station.StationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OperatorSlotListScreen(
    onBack: () -> Unit,
    stationViewModel: StationViewModel,
    onAddSlotClick: () -> Unit = {}
) {
    val state = stationViewModel.slotListState
    val greenBg = Color(0xFF0C8A44)
    
    // For date formatting
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val todayDateStr = sdf.format(Date())
    
    var selectedDate by remember { mutableStateOf("All Dates") }
    var selectedStatus by remember { mutableStateOf("All") }
    var expanded by remember { mutableStateOf(false) }

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
                "Time Slots",
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
                    .padding(20.dp)
            ) {
                // Date Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(Icons.Filled.Sync, contentDescription = null, tint = Color(0xFF1F2937))
                            Text(
                                text = selectedDate,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F2937)
                            )
                            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Color(0xFF1F2937))
                        }
                    }

                    if (state is SlotListState.Loaded) {
                        val uniqueDates = state.slots
                            .map { it.getFormattedDate() }
                            .distinct()
                            .sorted()

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Dates") },
                                onClick = {
                                    selectedDate = "All Dates"
                                    expanded = false
                                }
                            )
                            uniqueDates.forEach { date ->
                                DropdownMenuItem(
                                    text = { Text(date) },
                                    onClick = {
                                        selectedDate = date
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Filter Chips
                val statuses = listOf("All", "Available", "Reserved", "Pending", "Unavailable")
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(statuses) { status ->
                        val isSelected = selectedStatus == status
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) greenBg else Color(0xFFF3F4F6),
                            modifier = Modifier.clickable { selectedStatus = status }
                        ) {
                            Text(
                                text = status,
                                color = if (isSelected) Color.White else Color(0xFF4B5563),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Slots List
                Box(modifier = Modifier.weight(1f)) {
                    when (state) {
                        is SlotListState.Idle, is SlotListState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = greenBg)
                            }
                        }
                        is SlotListState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(state.message, color = Color.Red)
                            }
                        }
                        is SlotListState.Loaded -> {
                            val filteredSlots = state.slots
                                .filter { selectedDate == "All Dates" || it.getFormattedDate() == selectedDate }
                                .filter {
                                    val s = it.status.uppercase()
                                    when (selectedStatus) {
                                        "All" -> true
                                        "Available" -> s == "AVAILABLE" || s == "0"
                                        "Reserved" -> s == "RESERVED" || s == "1"
                                        "Pending" -> s == "PENDING" || s == "3"
                                        "Unavailable" -> s != "AVAILABLE" && s != "0" && s != "RESERVED" && s != "1" && s != "PENDING" && s != "3"
                                        else -> true
                                    }
                                }
                                .sortedBy { it.startDateTime }

                            if (filteredSlots.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                    Text("No slots found for this date.", color = Color.Gray, modifier = Modifier.padding(top = 40.dp))
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(bottom = 20.dp)
                                ) {
                                    items(filteredSlots) { slot ->
                                        OperatorSlotCard(
                                            slot = slot,
                                            onToggle = { stationViewModel.toggleSlotStatus(slot) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Slot Button
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddSlotClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = greenBg)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Slot", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun OperatorSlotCard(slot: Slot, onToggle: () -> Unit) {
    val status = slot.status.uppercase()
    // "0" or "AVAILABLE" usually mean available in our system
    val isAvailable = status == "AVAILABLE" || status == "0"
    val isUnavailable = status != "AVAILABLE" && status != "0" && status != "RESERVED" && status != "1" && status != "PENDING" && status != "3"
    val canToggle = isAvailable || isUnavailable

    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var pendingStatusChange by remember { mutableStateOf<String?>(null) }

    val (statusText, bgColor, textColor) = when {
        isAvailable -> Triple("Available", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        status == "RESERVED" || status == "1" -> Triple("Reserved", Color(0xFFFFEBEE), Color(0xFFC62828))
        status == "PENDING" || status == "3" -> Triple("Pending", Color(0xFFFFF8E1), Color(0xFFF57F17))
        else -> Triple("Unavailable", Color(0xFFF3F4F6), Color(0xFF4B5563))
    }

    if (showDialog && pendingStatusChange != null) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false
                pendingStatusChange = null
            },
            title = {
                Text(text = "Change Slot Status", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Are you sure you want to change this slot from $statusText to ${pendingStatusChange}?",
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onToggle()
                    showDialog = false
                    pendingStatusChange = null
                }) {
                    Text("Yes, Change", color = Color(0xFF0C8A44), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDialog = false
                    pendingStatusChange = null
                }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${slot.getFormattedStartTime()} \u2013 ${slot.getFormattedEndTime()}", // En dash
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF111827)
                )
                Spacer(modifier = Modifier.height(4.dp))
                val displayCapacity = if (slot.capacity > 0.0) slot.capacity else 5.0
                Text(
                    text = "$displayCapacity kWh Capacity",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = bgColor,
                        modifier = Modifier
                            .width(110.dp)
                            .clickable(enabled = canToggle) { expanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = statusText,
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            if (canToggle) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Filled.ArrowDropDown,
                                    contentDescription = "Change Status",
                                    tint = textColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    
                    if (canToggle) {
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Available", fontWeight = if (isAvailable) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    expanded = false
                                    if (!isAvailable) {
                                        pendingStatusChange = "Available"
                                        showDialog = true
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Unavailable", fontWeight = if (isUnavailable) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    expanded = false
                                    if (!isUnavailable) {
                                        pendingStatusChange = "Unavailable"
                                        showDialog = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
