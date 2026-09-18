package com.smartsolarmicrogrid.prosumer.ui.station

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsolarmicrogrid.prosumer.data.model.Slot

private val SolarGreen = Color(0xFF2E7D32)

@Composable
fun SlotListScreen(
    onSlotSelected: (Slot) -> Unit,
    onBack: () -> Unit,
    stationViewModel: StationViewModel = viewModel()
) {
    val station = stationViewModel.selectedStation
    val state = stationViewModel.slotListState

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text("Available Slots", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                if (station != null) {
                    Text(station.stationName, fontSize = 13.sp, color = Color.Gray)
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
                val availableSlots = state.slots.filter { it.status == "AVAILABLE" }
                if (availableSlots.isEmpty()) {
                    Text("No available slots for this station right now.", color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(availableSlots) { slot ->
                            SlotCard(slot = slot, onClick = { onSlotSelected(slot) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotCard(slot: Slot, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Schedule, contentDescription = null, tint = SolarGreen)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(slot.date, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("${slot.startTime} - ${slot.endTime}", fontSize = 12.sp, color = Color.Gray)
            }
            AssistChip(
                onClick = {},
                label = { Text("AVAILABLE", fontSize = 10.sp) },
                colors = AssistChipDefaults.assistChipColors(containerColor = SolarGreen.copy(alpha = 0.15f))
            )
        }
    }
}