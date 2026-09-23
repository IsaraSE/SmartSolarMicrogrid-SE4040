package com.smartsolarmicrogrid.prosumer.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.smartsolarmicrogrid.prosumer.data.model.Station

private val SolarGreen = Color(0xFF2E7D32)
private val SurfaceGray = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBookingScreen(
    station: Station,
    slot: Slot,
    onBookingConfirmed: (com.smartsolarmicrogrid.prosumer.data.model.Reservation) -> Unit,
    onBack: () -> Unit,
    bookingViewModel: BookingViewModel = viewModel()
) {
    val state = bookingViewModel.createBookingState
    var purpose by remember { mutableStateOf("Sell Energy (Feed to Grid)") }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is CreateBookingState.Success) {
            onBookingConfirmed(state.reservation)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Booking", fontWeight = FontWeight.SemiBold) },
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
                    onClick = {
                        bookingViewModel.createBooking(
                            stationId = station.stationId,
                            slotId = slot.slotId,
                            bookingDate = slot.startDateTime.substringBefore("T"),
                            startTime = slot.startDateTime.substringAfter("T").substringBefore("Z"),
                            notes = notes.takeIf { it.isNotBlank() }
                        )
                    },
                    enabled = state !is CreateBookingState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (state is CreateBookingState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Confirm Booking", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Booking Details Card
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailTextRow("Station", station.stationName)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (!slot.slotName.isNullOrEmpty()) {
                        DetailTextRow("Slot Name", slot.slotName)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    DetailTextRow("Date", slot.getFormattedDate())
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailTextRow("Time", "${slot.getFormattedStartTime()} - ${slot.getFormattedEndTime()}")
                    Spacer(modifier = Modifier.height(12.dp))
                    val displayCapacity = if (slot.capacity > 0.0) slot.capacity else 5.0
                    DetailTextRow("Energy Amount", "$displayCapacity kWh")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes Section
            Text("Notes (Optional)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Add any additional notes...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(8.dp)
            )

            if (state is CreateBookingState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    state.message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun DetailTextRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}