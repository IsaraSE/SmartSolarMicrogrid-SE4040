package com.smartsolarmicrogrid.prosumer.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
private val PendingOrange = Color(0xFFFFA726)
private val PendingOrangeLight = Color(0xFFFFF3E0)

@Composable
fun BookingSummaryScreen(
    reservation: Reservation,
    onDone: () -> Unit,
    onModify: () -> Unit, // Keep for compatibility if needed
    onCancel: () -> Unit  // Keep for compatibility if needed
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = SolarGreen,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Booking Confirmed", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SolarGreen)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Your booking request has been submitted successfully!",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Status Badge
        Row(
            modifier = Modifier
                .background(PendingOrangeLight, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Status", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Text("PENDING", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PendingOrange)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "You will be notified once it is approved by the Grid Operator.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Details
        Column(modifier = Modifier.fillMaxWidth()) {
            SummaryRow("Station", "Colombo Solar Hub")
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow("Date", reservation.bookingDate)
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow("Time", reservation.startTime)
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow("Energy Amount", "5 kWh")
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow("Purpose", "Sell Energy")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDone, // Goes to My Bookings
            colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Go to My Bookings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onCancel, // Let's use onCancel to act as 'Back to Home' since we don't want to change NavGraph signature too much. Wait, NavGraph passes 'cancel_booking' for onCancel. Let's just fix it in NavGraph instead of reusing onCancel.
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SolarGreen),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SolarGreen)
        ) {
            Text("Back to Home", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}