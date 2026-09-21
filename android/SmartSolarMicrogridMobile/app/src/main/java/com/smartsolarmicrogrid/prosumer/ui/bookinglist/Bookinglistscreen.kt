package com.smartsolarmicrogrid.prosumer.ui.bookinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsolarmicrogrid.prosumer.data.model.Reservation

private val SolarGreen = Color(0xFF2E7D32)
private val SurfaceGray = Color(0xFFF5F5F5)

// Define dummy reservations to match the mockup
private val demoReservations = listOf(
    Reservation(
        reservationId = "RES-20260922-001",
        prosumerNic = "123456789V",
        stationId = "Colombo Solar Hub",
        slotId = "slot1",
        bookingDate = "22 Sep 2026",
        startTime = "08:00 AM - 10:00 AM",
        status = "PENDING"
    ),
    Reservation(
        reservationId = "RES-20260918-002",
        prosumerNic = "123456789V",
        stationId = "Kandy Solar Hub",
        slotId = "slot2",
        bookingDate = "18 Sep 2026",
        startTime = "10:00 AM - 12:00 PM",
        status = "APPROVED"
    ),
    Reservation(
        reservationId = "RES-20260910-003",
        prosumerNic = "123456789V",
        stationId = "Galle Solar Hub",
        slotId = "slot3",
        bookingDate = "10 Sep 2026",
        startTime = "02:00 PM - 04:00 PM",
        status = "COMPLETED"
    ),
    Reservation(
        reservationId = "RES-20260905-004",
        prosumerNic = "123456789V",
        stationId = "Jaffna Solar Hub",
        slotId = "slot4",
        bookingDate = "05 Sep 2026",
        startTime = "04:00 PM - 06:00 PM",
        status = "CANCELLED"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    onBookingSelected: (Reservation) -> Unit,
    onBack: () -> Unit,
    bookingListViewModel: BookingListViewModel = viewModel()
) {
    val tabs = listOf("All", "Pending", "Approved", "Completed")
    var selectedTabIndex by remember { mutableStateOf(1) } // Default to "Pending"

    val filteredList = when (selectedTabIndex) {
        0 -> demoReservations
        1 -> demoReservations.filter { it.status == "PENDING" }
        2 -> demoReservations.filter { it.status == "APPROVED" }
        3 -> demoReservations.filter { it.status == "COMPLETED" }
        else -> demoReservations
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings", fontWeight = FontWeight.SemiBold) },
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
        ) {
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = SolarGreen,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = SolarGreen,
                            height = 3.dp
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) SolarGreen else Color.Gray
                            )
                        }
                    )
                }
            }

            // List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { reservation ->
                    BookingCard(
                        reservation = reservation,
                        onClick = {
                            bookingListViewModel.selectReservation(reservation)
                            onBookingSelected(reservation)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookingCard(reservation: Reservation, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.EventNote, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(reservation.stationId, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(reservation.bookingDate, fontSize = 14.sp, color = Color.Gray)
                        Text(reservation.startTime, fontSize = 14.sp, color = Color.Gray)
                    }
                }
                StatusBadge(status = reservation.status)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val backgroundColor = when (status) {
        "APPROVED" -> Color(0xFFE8F5E9)
        "PENDING" -> Color(0xFFFFF3E0)
        "COMPLETED" -> Color(0xFFE3F2FD)
        else -> Color(0xFFFFEBEE)
    }
    val textColor = when (status) {
        "APPROVED" -> SolarGreen
        "PENDING" -> Color(0xFFFFA726)
        "COMPLETED" -> Color(0xFF1E88E5)
        else -> Color(0xFFE53935)
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = status,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}