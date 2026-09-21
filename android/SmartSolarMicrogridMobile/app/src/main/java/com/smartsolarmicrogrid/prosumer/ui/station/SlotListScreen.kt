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
            
            // Date Selector (Demo UI)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
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
                        Text("22 Sep 2026", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
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
                    val availableSlots = state.slots.filter { it.status == "AVAILABLE" }
                    if (availableSlots.isEmpty()) {
                        Text("No available slots for this station right now.", color = Color.Gray)
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
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isSelected) SolarGreen else Color.LightGray),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = SolarGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${slot.startTime} - ${slot.endTime}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("5 kWh", fontSize = 12.sp, color = Color.Gray)
            }
            AssistChip(
                onClick = {},
                label = { Text("Available", fontSize = 10.sp, color = SolarGreen) },
                colors = AssistChipDefaults.assistChipColors(containerColor = SolarGreen.copy(alpha = 0.1f)),
                border = null
            )
        }
    }
}