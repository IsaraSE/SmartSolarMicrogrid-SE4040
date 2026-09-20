package com.smartsolarmicrogrid.prosumer.ui.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsolarmicrogrid.prosumer.data.model.Reservation

private val SolarGreen = Color(0xFF2E7D32)

@Composable
fun CancelBookingScreen(
    reservation: Reservation,
    onCancelled: () -> Unit,
    onBack: () -> Unit,
    bookingViewModel: BookingViewModel = viewModel()
) {
    val state = bookingViewModel.cancelBookingState

    LaunchedEffect(state) {
        if (state is CancelBookingState.Success) {
            onCancelled()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.WarningAmber,
            contentDescription = null,
            tint = Color(0xFFE65100),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Cancel this booking?", fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(
            "Reservation ${reservation.reservationId} on ${reservation.bookingDate}",
            fontSize = 13.sp,
            color = Color.Gray
        )
        Text(
            "Cancellations require at least 12 hours' notice.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        if (state is CancelBookingState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA))
            ) {
                Text(state.message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { bookingViewModel.cancelBooking(reservation.reservationId) },
            enabled = state !is CancelBookingState.Loading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (state is CancelBookingState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Yes, Cancel Booking")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Keep Booking")
        }
    }
}