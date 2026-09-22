package com.smartsolarmicrogrid.prosumer.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsolarmicrogrid.prosumer.data.model.Reservation

private val SolarGreenDark = Color(0xFF1B5E20)
private val SolarGreen = Color(0xFF2E7D32)
private val SurfaceGray = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyBookingScreen(
    reservation: Reservation,
    onUpdated: () -> Unit,
    onBack: () -> Unit,
    bookingViewModel: BookingViewModel = viewModel()
) {
    var note by remember { mutableStateOf(reservation.notes ?: "") } // Initialize with existing note

    val state = bookingViewModel.updateBookingState
    val availableSlots = bookingViewModel.availableSlotsState

    LaunchedEffect(Unit) {
        bookingViewModel.resetUpdateBookingState()
        bookingViewModel.loadAvailableSlots(reservation.stationId)
    }

    LaunchedEffect(state) {
        if (state is UpdateBookingState.Success) {
            onUpdated()
        }
    }

    var expandedDate by remember { mutableStateOf(false) }
    var expandedSlot by remember { mutableStateOf(false) }

    val availableDates = remember(availableSlots) {
        availableSlots?.map { it.getFormattedDate() }?.distinct() ?: emptyList()
    }

    var selectedDateStr by remember(availableDates) {
        mutableStateOf(availableDates.firstOrNull() ?: "")
    }

    val slotsForSelectedDate = remember(availableSlots, selectedDateStr) {
        availableSlots?.filter { it.getFormattedDate() == selectedDateStr && it.status == "AVAILABLE" } ?: emptyList()
    }

    var selectedSlot by remember(slotsForSelectedDate) {
        mutableStateOf(slotsForSelectedDate.firstOrNull())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modify Booking", fontWeight = FontWeight.SemiBold) },
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
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "Select New Date", 
                fontSize = 14.sp, 
                fontWeight = FontWeight.SemiBold, 
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = expandedDate,
                onExpandedChange = { expandedDate = !expandedDate }
            ) {
                OutlinedTextField(
                    value = selectedDateStr.ifEmpty { if (availableSlots == null) "Loading..." else "No dates available" },
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Color.DarkGray) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDate) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = SolarGreen
                    ),
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedDate,
                    onDismissRequest = { expandedDate = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    availableDates.forEach { date ->
                        DropdownMenuItem(
                            text = { Text(date) },
                            onClick = {
                                selectedDateStr = date
                                expandedDate = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Select New Time Slot", 
                fontSize = 14.sp, 
                fontWeight = FontWeight.SemiBold, 
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = expandedSlot,
                onExpandedChange = { expandedSlot = !expandedSlot }
            ) {
                val slotText = selectedSlot?.let { 
                    val cap = if (it.capacity > 0) it.capacity else 5.0
                    "${it.getFormattedStartTime()} - ${it.getFormattedEndTime()} ($cap kWh)" 
                } ?: "No slots available"
                
                OutlinedTextField(
                    value = slotText,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSlot) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = SolarGreen
                    ),
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedSlot,
                    onDismissRequest = { expandedSlot = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    slotsForSelectedDate.forEach { slot ->
                        DropdownMenuItem(
                            text = { 
                                val cap = if (slot.capacity > 0) slot.capacity else 5.0
                                Text("${slot.getFormattedStartTime()} - ${slot.getFormattedEndTime()} ($cap kWh)") 
                            },
                            onClick = {
                                selectedSlot = slot
                                expandedSlot = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Note (Optional)", 
                fontSize = 14.sp, 
                fontWeight = FontWeight.SemiBold, 
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= 100) note = it },
                placeholder = { Text("Add a note", color = Color.Gray) },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = SolarGreen
                ),
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
            Text(
                text = "${note.length}/100",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )

            if (state is UpdateBookingState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA))
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedSlot?.let { slot ->
                        bookingViewModel.updateBooking(
                            reservationId = reservation.reservationId,
                            stationId = reservation.stationId,
                            slotId = slot.slotId,
                            bookingDate = slot.startDateTime.substringBefore("T"),
                            startTime = slot.startDateTime.substringAfter("T").substringBefore("Z"),
                            notes = note.takeIf { it.isNotBlank() }
                        )
                    }
                },
                enabled = state !is UpdateBookingState.Loading && selectedSlot != null,
                colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state is UpdateBookingState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Update Booking", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}