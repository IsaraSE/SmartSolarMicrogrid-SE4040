package com.smartsolarmicrogrid.prosumer.ui.bookinglist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsolarmicrogrid.prosumer.data.model.Reservation
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val SolarGreen = Color(0xFF2E7D32)
private val SurfaceGray = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    onBookingSelected: (Reservation) -> Unit,
    onBack: () -> Unit,
    bookingListViewModel: BookingListViewModel = viewModel()
) {
    val selectedTabIndex = bookingListViewModel.selectedTab.ordinal
    val listState = bookingListViewModel.listState

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
        containerColor = Color.White
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
                edgePadding = 16.dp,
                divider = {},
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
                BookingTab.values().forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { bookingListViewModel.onTabSelected(tab) },
                        text = {
                            Text(
                                tab.label,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) SolarGreen else Color.Gray,
                                fontSize = 15.sp
                            )
                        }
                    )
                }
            }
            
            Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            // List
            when (listState) {
                is BookingListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SolarGreen)
                    }
                }
                is BookingListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(listState.message, color = Color.Red)
                    }
                }
                is BookingListState.Loaded -> {
                    val filteredList = listState.reservations
                    if (filteredList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No bookings found.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
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
        }
    }
}

@Composable
private fun BookingCard(reservation: Reservation, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status-colored icon background
                    val statusColor = getStatusColor(reservation.status)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.EventNote,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = reservation.stationName ?: reservation.stationId,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF263238)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val formattedDate = try {
                            LocalDate.parse(reservation.bookingDate).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        } catch (e: Exception) {
                            reservation.bookingDate
                        }
                        
                        val formattedTime = try {
                            val st = LocalTime.parse(reservation.startTime)
                            val et = LocalTime.parse(reservation.endTime)
                            "${st.format(DateTimeFormatter.ofPattern("hh:mm a"))} – ${et.format(DateTimeFormatter.ofPattern("hh:mm a"))}"
                        } catch (e: Exception) {
                            "${reservation.startTime} – ${reservation.endTime}"
                        }

                        Text(formattedDate, fontSize = 14.sp, color = Color(0xFF78909C), fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(formattedTime, fontSize = 14.sp, color = Color(0xFF78909C), fontWeight = FontWeight.Medium)
                    }
                }
                
                StatusBadge(status = reservation.status)
            }
        }
    }
}

private fun getStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "APPROVED" -> Color(0xFF66BB6A)
        "PENDING" -> Color(0xFFFFB74D)
        "COMPLETED" -> Color(0xFF90CAF9)
        "CANCELLED" -> Color(0xFFEF5350)
        else -> Color(0xFFBDBDBD)
    }
}

@Composable
fun StatusBadge(status: String) {
    val backgroundColor = getStatusColor(status).copy(alpha = 0.1f)
    val textColor = getStatusColor(status)

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = status.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}