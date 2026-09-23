package com.smartsolarmicrogrid.prosumer.ui.operator

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsolarmicrogrid.prosumer.data.model.CreateSlotRequest
import com.smartsolarmicrogrid.prosumer.ui.station.SlotListState
import com.smartsolarmicrogrid.prosumer.ui.station.StationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorAddSlotScreen(
    onBack: () -> Unit,
    stationViewModel: StationViewModel
) {
    val station = stationViewModel.selectedStation
    val slots = (stationViewModel.slotListState as? SlotListState.Loaded)?.slots ?: emptyList()
    val greenBg = Color(0xFF0C8A44)
    val context = LocalContext.current

    if (station == null) {
        onBack()
        return
    }

    var dateCal by remember { mutableStateOf(Calendar.getInstance()) }
    var startTimeCal by remember { mutableStateOf(Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 0) }) }
    var endTimeCal by remember { mutableStateOf(Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 10); set(Calendar.MINUTE, 0) }) }
    var batterySlots by remember { mutableStateOf(station.batterySlotCount.toString()) }
    var selectedSlotName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }

    // Generate acronym and slots
    val acronym = station.stationName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
    val createdNames = slots.map { it.slotName }
    
    val availableSlotOptions = (1..station.batterySlotCount).map { i ->
        val suffix = i.toString().padStart(3, '0')
        val name = "$acronym $suffix"
        val isCreated = createdNames.contains(name)
        Pair(name, isCreated)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(greenBg)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Add Time Slot",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Main Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Slot Name
                Text("Slot Name", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedSlotName.ifEmpty { "Select Slot" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = greenBg
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        availableSlotOptions.forEach { (name, isCreated) ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = if (isCreated) "$name (Already Created)" else name,
                                        color = if (isCreated) Color.Gray else Color.Black
                                    ) 
                                },
                                onClick = {
                                    if (!isCreated) {
                                        selectedSlotName = name
                                        isDropdownExpanded = false
                                    }
                                },
                                enabled = !isCreated
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))

                // Date
                Text("Date", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dateFormat.format(dateCal.time),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Date",
                            tint = Color(0xFF64748B)
                        )
                    },
                    modifier = Modifier.fillMaxWidth().clickable {
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                dateCal.set(y, m, d)
                                startTimeCal.set(y, m, d)
                                endTimeCal.set(y, m, d)
                            },
                            dateCal.get(Calendar.YEAR),
                            dateCal.get(Calendar.MONTH),
                            dateCal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = greenBg,
                        disabledTextColor = Color.Black
                    ),
                    enabled = false // to enforce click on the modifier instead of keyboard opening
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Start Time
                Text("Start Time", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = timeFormat.format(startTimeCal.time),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_recent_history),
                            contentDescription = "Select Start Time",
                            tint = Color(0xFF64748B)
                        )
                    },
                    modifier = Modifier.fillMaxWidth().clickable {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                startTimeCal.set(Calendar.HOUR_OF_DAY, h)
                                startTimeCal.set(Calendar.MINUTE, m)
                            },
                            startTimeCal.get(Calendar.HOUR_OF_DAY),
                            startTimeCal.get(Calendar.MINUTE),
                            false
                        ).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = greenBg,
                        disabledTextColor = Color.Black
                    ),
                    enabled = false
                )

                Spacer(modifier = Modifier.height(20.dp))

                // End Time
                Text("End Time", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = timeFormat.format(endTimeCal.time),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_recent_history),
                            contentDescription = "Select End Time",
                            tint = Color(0xFF64748B)
                        )
                    },
                    modifier = Modifier.fillMaxWidth().clickable {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                endTimeCal.set(Calendar.HOUR_OF_DAY, h)
                                endTimeCal.set(Calendar.MINUTE, m)
                            },
                            endTimeCal.get(Calendar.HOUR_OF_DAY),
                            endTimeCal.get(Calendar.MINUTE),
                            false
                        ).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = greenBg,
                        disabledTextColor = Color.Black
                    ),
                    enabled = false
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Battery Slots / Capacity
                Text("Battery Slots", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = batterySlots,
                    onValueChange = { batterySlots = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = greenBg
                    )
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                // Optional Note
                Text("Note (Optional)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = greenBg
                    ),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (selectedSlotName.isEmpty()) {
                            Toast.makeText(context, "Please select a slot name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val capacity = batterySlots.toDoubleOrNull()
                        if (capacity == null) {
                            Toast.makeText(context, "Please enter a valid capacity", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Adjust the dates for the UTC string
                        val sTime = startTimeCal.time
                        val eTime = endTimeCal.time
                        
                        val req = CreateSlotRequest(
                            stationId = station.stationId,
                            slotName = selectedSlotName,
                            startDateTime = isoFormat.format(sTime),
                            endDateTime = isoFormat.format(eTime),
                            capacity = capacity
                        )
                        stationViewModel.createSlot(req, onSuccess = {
                            Toast.makeText(context, "Slot created successfully", Toast.LENGTH_SHORT).show()
                            stationViewModel.selectStationAndLoadAllSlots(station)
                            onBack()
                        }, onError = { err ->
                            Toast.makeText(context, "Error: $err", Toast.LENGTH_SHORT).show()
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = greenBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Slot", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
