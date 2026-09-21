package com.smartsolarmicrogrid.prosumer.ui.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.smartsolarmicrogrid.prosumer.data.model.Slot
import com.smartsolarmicrogrid.prosumer.data.model.Station

private val SolarGreen = Color(0xFF1B6B3F) // Darker green from the image
private val ButtonGreen = Color(0xFF1B6B3F)
private val PendingOrange = Color(0xFFF57C00)
private val PendingOrangeLight = Color(0xFFFFF3E0)
private val LightGrayBorder = Color(0xFFE0E0E0)
private val TextGray = Color(0xFF757575)

@Composable
fun BookingSummaryScreen(
    reservation: Reservation,
    station: Station,
    slot: Slot,
    onDone: () -> Unit,
    onBackToHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top dark green background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .background(
                    color = SolarGreen,
                    shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)
                )
        )

        // The overlapping white card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 80.dp, bottom = 40.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Green checkmark icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(color = SolarGreen, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Booking Confirmed",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = SolarGreen
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Your booking request has been\nsubmitted successfully!",
                    fontSize = 15.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Status Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LightGrayBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextGray,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .background(PendingOrangeLight, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "PENDING",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PendingOrange
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "You will be notified once it is approved\nby the Grid Operator.",
                        fontSize = 13.sp,
                        color = TextGray,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Details List
                Column(modifier = Modifier.fillMaxWidth()) {
                    SummaryRow("Station", station.stationName)
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = LightGrayBorder, thickness = 1.dp)
                    SummaryRow("Date", slot.getFormattedDate())
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = LightGrayBorder, thickness = 1.dp)
                    SummaryRow("Time", "${slot.getFormattedStartTime()} - ${slot.getFormattedEndTime()}")
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = LightGrayBorder, thickness = 1.dp)
                    val displayCapacity = if (slot.capacity > 0.0) slot.capacity else 5.0
                    SummaryRow("Energy Amount", "$displayCapacity kWh")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons
                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Go to My Bookings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onBackToHome,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    border = BorderStroke(1.dp, ButtonGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ButtonGreen)
                ) {
                    Text("Back to Home", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = TextGray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}