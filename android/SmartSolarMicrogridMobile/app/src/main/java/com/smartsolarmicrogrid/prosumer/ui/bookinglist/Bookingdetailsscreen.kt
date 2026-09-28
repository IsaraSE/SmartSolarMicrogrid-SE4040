/*
 * BookingDetailsScreen.kt
 * Smart Solar Microgrid Trading System - Prosumer Mobile Application
 *
 * Shows the full details of one reservation selected from the booking list and
 * offers the actions allowed for its status: modify, cancel and (once approved)
 * display the transaction QR code.
 */
package com.smartsolarmicrogrid.prosumer.ui.bookinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsolarmicrogrid.prosumer.data.model.Reservation

private val SolarGreenDark = Color(0xFF1B5E20)
private val SolarGreen = Color(0xFF2E7D32)
private val SurfaceGray = Color(0xFFF5F5F5)

@Composable
fun BookingDetailsScreen(
    reservation: Reservation,
    onModify: () -> Unit,
    onCancel: () -> Unit,
    onShowQr: () -> Unit,
    onBack: () -> Unit
) {
    // Only bookings that have not finished yet can still be modified or cancelled.
    val isChangeable = reservation.status == "PENDING" || reservation.status == "APPROVED"
    val isApproved = reservation.status == "APPROVED"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SolarGreenDark, SolarGreen, SurfaceGray),
                    startY = 0f,
                    endY = 420f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Booking Details", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text(
                        reservation.reservationId,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }

            // Status card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reservation status", fontSize = 13.sp, color = Color.Gray)
                        StatusBadge(status = reservation.status)
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = SurfaceGray)
                    Spacer(modifier = Modifier.height(14.dp))

                    DetailRow(Icons.Filled.SolarPower, "Station", reservation.stationId)
                    DetailRow(Icons.Filled.Schedule, "Slot", reservation.slotId)
                    DetailRow(Icons.Filled.CalendarMonth, "Booking date", reservation.bookingDate)
                    DetailRow(Icons.Filled.AccessTime, "Start time", reservation.startTime)
                    DetailRow(Icons.Filled.Badge, "Prosumer NIC", reservation.prosumerNic)
                    reservation.createdAt?.let { DetailRow(Icons.Filled.HistoryToggleOff, "Created", it) }
                    reservation.updatedAt?.let { DetailRow(Icons.Filled.Update, "Last updated", it) }
                    reservation.completedAt?.let { DetailRow(Icons.Filled.CheckCircle, "Completed", it) }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Actions
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                if (isApproved) {
                    Button(
                        onClick = onShowQr,
                        colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Filled.QrCode2, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Show Transaction QR")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (isChangeable) {
                    OutlinedButton(
                        onClick = onModify,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Modify Booking")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Filled.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel Booking")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Changes and cancellations are allowed only up to 12 hours before the booking starts. The server confirms this rule.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                } else {
                    Text(
                        text = "This booking is ${reservation.status.lowercase()} and can no longer be changed.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/** One labelled line inside the details card. */
@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = SolarGreen, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}