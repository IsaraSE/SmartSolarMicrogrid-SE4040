package com.smartsolarmicrogrid.prosumer.ui.operator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsolarmicrogrid.prosumer.data.model.Reservation
import com.smartsolarmicrogrid.prosumer.ui.bookinglist.BookingListViewModel

private val OpBrandGreen = Color(0xFF0C8A44)
private val OpSurfaceGray = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorReservationDetailsScreen(
    reservation: Reservation,
    onBack: () -> Unit,
    onViewProsumer: (String) -> Unit,
    operatorViewModel: OperatorViewModel,
    bookingListViewModel: BookingListViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var showApproveDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    // Removed the LaunchedEffect here because we immediately navigate back to the reservations list,
    // which has its own LaunchedEffect to catch the actionMessage, show the snackbar, and refresh.

    val statusText = reservation.status.lowercase().replaceFirstChar { it.uppercase() }
    val isPending = reservation.status.equals("PENDING", ignoreCase = true)
    val isApproved = reservation.status.equals("APPROVED", ignoreCase = true)
    val isCancelledOrCompleted = !isPending && !isApproved

    // 12-hour rule calculation
    val canCancel = remember(reservation) {
        if (isCancelledOrCompleted) return@remember false
        try {
            val timeStr = if (reservation.startTime.count { it == ':' } == 1) "${reservation.startTime}:00" else reservation.startTime
            val dateTimeStr = "${reservation.bookingDate} $timeStr"
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val scheduledTime = java.time.LocalDateTime.parse(dateTimeStr, formatter)
            val now = java.time.LocalDateTime.now()
            java.time.Duration.between(now, scheduledTime).toHours() >= 12
        } catch (e: Exception) {
            true // fallback, backend will reject anyway
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reservation Details", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OpBrandGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = OpSurfaceGray,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status Badge
                    val (bgColor, textColor) = when (statusText.lowercase()) {
                        "pending" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
                        "approved" -> Pair(Color(0xFFDCFCE7), Color(0xFF166534))
                        "cancelled" -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
                        else -> Pair(Color(0xFFE2E8F0), Color(0xFF475569))
                    }
                    Box(
                        modifier = Modifier
                            .background(bgColor, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Details List
                    Column(modifier = Modifier.fillMaxWidth()) {
                        DetailRow("Reservation ID", reservation.reservationNumber ?: reservation.reservationId)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow("Prosumer NIC", reservation.prosumerNic)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow("Station", reservation.stationName ?: reservation.stationId)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow("Date", reservation.bookingDate)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val formattedTime = try {
                            val st = java.time.LocalTime.parse(reservation.startTime)
                            val stStr = st.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))
                            if (reservation.endTime != null) {
                                val et = java.time.LocalTime.parse(reservation.endTime)
                                "$stStr - ${et.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))}"
                            } else {
                                stStr
                            }
                        } catch (e: Exception) {
                            reservation.startTime
                        }
                        DetailRow("Time", formattedTime)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val energyText = reservation.energyAmount?.let { "$it kWh" } ?: "5 kWh"
                        DetailRow("Energy Amount", energyText)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val formattedRequestedOn = try {
                            if (reservation.createdAt != null) {
                                val parsedDate = java.time.ZonedDateTime.parse(reservation.createdAt).withZoneSameInstant(java.time.ZoneId.systemDefault())
                                parsedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
                            } else {
                                "N/A"
                            }
                        } catch (e: Exception) {
                            reservation.createdAt ?: "N/A"
                        }
                        DetailRow("Requested On", formattedRequestedOn)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (canCancel) {
                        // 12-Hour Policy Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFFFFA726))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "This reservation can be cancelled because it is more than 12 hours before the start time.",
                                fontSize = 12.sp,
                                color = Color(0xFFE65100)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    } else if (isPending || isApproved) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Cancellation is disabled because the reservation is within 12 hours of the start time.",
                                fontSize = 12.sp,
                                color = Color(0xFFB91C1C)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Action Buttons
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (isPending) {
                            Button(
                                onClick = { showApproveDialog = true },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OpBrandGreen)
                            ) {
                                Text("Approve", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        
                        if (canCancel) {
                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Text("Cancel", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                }
            }
        }

        if (showApproveDialog) {
            AlertDialog(
                onDismissRequest = { showApproveDialog = false },
                title = { Text("Approve Reservation") },
                text = { Text("Are you sure you want to approve this reservation?") },
                confirmButton = {
                    TextButton(onClick = {
                        showApproveDialog = false
                        operatorViewModel.approveReservation(reservation)
                        onBack()
                    }) { Text("Confirm", color = OpBrandGreen, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showApproveDialog = false }) { Text("Cancel", color = Color.Gray) }
                }
            )
        }

        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text("Cancel Reservation") },
                text = { Text("Are you sure you want to cancel this reservation? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        showCancelDialog = false
                        operatorViewModel.cancelReservation(reservation.reservationId)
                        onBack()
                    }) { Text("Confirm", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) { Text("Close", color = Color.Gray) }
                }
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}
