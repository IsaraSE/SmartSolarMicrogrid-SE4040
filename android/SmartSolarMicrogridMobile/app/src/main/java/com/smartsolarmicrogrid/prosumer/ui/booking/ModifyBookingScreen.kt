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

@Composable
fun ModifyBookingScreen(
    reservation: Reservation,
    onUpdated: () -> Unit,
    onBack: () -> Unit,
    bookingViewModel: BookingViewModel = viewModel()
) {
    var bookingDate by remember { mutableStateOf(reservation.bookingDate) }
    var startTime by remember { mutableStateOf(reservation.startTime) }

    val state = bookingViewModel.updateBookingState

    LaunchedEffect(state) {
        if (state is UpdateBookingState.Success) {
            onUpdated()
        }
    }

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
        Column(modifier = Modifier.fillMaxSize()) {

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
                Text("Modify Booking", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Reservation ${reservation.reservationId}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = bookingDate,
                            onValueChange = { bookingDate = it },
                            label = { Text("Booking Date (YYYY-MM-DD)") },
                            leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = SolarGreen) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time (HH:MM)") },
                            leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null, tint = SolarGreen) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (state is UpdateBookingState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA))
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        bookingViewModel.updateBooking(
                            reservationId = reservation.reservationId,
                            stationId = reservation.stationId,
                            slotId = reservation.slotId,
                            bookingDate = bookingDate,
                            startTime = startTime
                        )
                    },
                    enabled = state !is UpdateBookingState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (state is UpdateBookingState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}