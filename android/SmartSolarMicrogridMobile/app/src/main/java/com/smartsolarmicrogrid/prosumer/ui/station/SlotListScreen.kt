package com.smartsolarmicrogrid.prosumer.ui.station

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsolarmicrogrid.prosumer.data.model.Slot

private val SolarGreen = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotListScreen(
    onSlotSelected: (Slot) -> Unit,
    onBack: () -> Unit,
    stationViewModel: StationViewModel = viewModel()
) {
    val state = stationViewModel.slotListState
    var selectedSlot by remember { mutableStateOf<Slot?>(null) }
    
    // State for the Date Dropdown
    var expanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("All Dates") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Slots", fontWeight = FontWeight.SemiBold) },
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
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Button(
                    onClick = { selectedSlot?.let { onSlotSelected(it) } },
                    enabled = selectedSlot != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            
            // Date Selector Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().clickable(
                        enabled = state is SlotListState.Loaded
                    ) { expanded = true },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.LightGray),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = SolarGreen)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Select Date", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = selectedDate, 
                                fontSize = 16.sp, 
                                color = Color.Black, 
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
                    }
                }

                if (state is SlotListState.Loaded) {
                    val uniqueDates = state.slots
                        .filter { it.status == "AVAILABLE" || it.status == "0" }
                        .map { it.getFormattedDate() }
                        .distinct()
                        .sorted()

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Dates") },
                            onClick = {
                                selectedDate = "All Dates"
                                selectedSlot = null
                                expanded = false
                            }
                        )
                        uniqueDates.forEach { date ->
                            DropdownMenuItem(
                                text = { Text(date) },
                                onClick = {
                                    selectedDate = date
                                    selectedSlot = null // Reset selected slot when date changes
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (state) {
                is SlotListState.Idle, is SlotListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SolarGreen)
                    }
                }
                is SlotListState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
                is SlotListState.Loaded -> {
                    val availableSlots = state.slots
                        .filter { it.status == "AVAILABLE" || it.status == "0" }
                        .filter { selectedDate == "All Dates" || it.getFormattedDate() == selectedDate }
                        .sortedBy { it.startDateTime }

                    if (availableSlots.isEmpty()) {
                        Text("No available slots for this date.", color = Color.Gray)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(availableSlots) { slot ->
                                SlotCard(
                                    slot = slot,
                                    isSelected = selectedSlot == slot,
                                    onClick = { selectedSlot = slot }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotCard(slot: Slot, isSelected: Boolean, onClick: () -> Unit) {
    val cardBg = if (isSelected) SolarGreen.copy(alpha = 0.05f) else Color.White
    val borderColor = if (isSelected) SolarGreen else Color(0xFFE0E0E0)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = SolarGreen,
                    unselectedColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slot.getFormattedDate(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${slot.getFormattedStartTime()} - ${slot.getFormattedEndTime()}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                val displayCapacity = if (slot.capacity > 0.0) slot.capacity else 5.0
                Text(
                    text = "$displayCapacity kWh",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = "Available",
                    fontSize = 12.sp,
                    color = SolarGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}