package com.smartsolarmicrogrid.prosumer.ui.bookinglist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsolarmicrogrid.prosumer.data.model.Reservation

private val SolarGreen = Color(0xFF2E7D32)
private val SurfaceGray = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    reservation: Reservation,
    onModify: () -> Unit,
    onCancel: () -> Unit,
    onShowQr: () -> Unit,
    onBack: () -> Unit
) {
    val isPending = reservation.status == "PENDING"
    val isApproved = reservation.status == "APPROVED"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Details", fontWeight = FontWeight.SemiBold) },
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
        containerColor = SurfaceGray
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
                    // Status Badge Centered
                    StatusBadge(status = reservation.status)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Details List
                    Column(modifier = Modifier.fillMaxWidth()) {
                        DetailRow("Reservation ID", reservation.reservationNumber ?: reservation.reservationId)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow("Station", reservation.stationName ?: reservation.stationId)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow("Date", reservation.bookingDate)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val formattedTime = try {
                            val st = java.time.LocalTime.parse(reservation.startTime)
                            val et = java.time.LocalTime.parse(reservation.endTime)
                            "${st.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))} – ${et.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))}"
                        } catch (e: Exception) {
                            "${reservation.startTime} – ${reservation.endTime}"
                        }
                        
                        DetailRow("Time", formattedTime)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow("Energy Amount", "5 kWh")
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        DetailRow("Notes", reservation.notes ?: "-")
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val formattedRequestedOn = try {
                            val parsedDate = java.time.ZonedDateTime.parse(reservation.createdAt).withZoneSameInstant(java.time.ZoneId.systemDefault())
                            parsedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
                        } catch (e: Exception) {
                            reservation.createdAt ?: "N/A"
                        }
                        
                        DetailRow("Requested On", formattedRequestedOn)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isPending) {
                        // Pending Status Explanation Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF1976D2))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Your reservation is currently awaiting Grid Operator approval. Once approved, you will be issued a QR code for station access.",
                                fontSize = 12.sp,
                                color = Color(0xFF0D47A1)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

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
                                text = "You can modify or cancel this booking until 12 hours before the start time.",
                                fontSize = 12.sp,
                                color = Color(0xFFE65100)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = onModify,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, SolarGreen),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SolarGreen)
                            ) {
                                Text("Modify", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onCancel,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFD32F2F)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                            ) {
                                Text("Cancel", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (isApproved) {
                        // QR Section
                        Icon(
                            Icons.Filled.QrCode2,
                            contentDescription = "QR Code",
                            modifier = Modifier.size(150.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Show this QR code to the Grid Operator at the station.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = onShowQr, // Using onShowQr as Download for demo
                            colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Download QR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}