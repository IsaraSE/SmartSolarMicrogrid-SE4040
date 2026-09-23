package com.smartsolarmicrogrid.prosumer.ui.bookinglist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsolarmicrogrid.prosumer.data.model.Reservation
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val SolarGreen = Color(0xFF0C8A44)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    onBookingSelected: (Reservation) -> Unit,
    onBack: () -> Unit,
    bookingListViewModel: BookingListViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        bookingListViewModel.refresh()
    }

    val selectedTabIndex = bookingListViewModel.selectedTab.ordinal
    val listState = if (bookingListViewModel.isSearching) bookingListViewModel.searchState else bookingListViewModel.listState
    val tabs = BookingTab.values().filter { it != BookingTab.COMPLETED } // Match UI
    
    val reservations = (listState as? BookingListState.Loaded)?.reservations ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SolarGreen)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .padding(top = 16.dp), // Approximate safe area
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Bookings",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 48.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // Main Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filter Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tabs) { tab ->
                        val isSelected = bookingListViewModel.selectedTab == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) SolarGreen else Color(0xFFF1F5F9))
                                .clickable { bookingListViewModel.onTabSelected(tab) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tab.label,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                        val reservations = listState.reservations

                        if (reservations.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No reservations found.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                items(reservations) { reservation ->
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
}

@Composable
private fun BookingCard(reservation: Reservation, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reservation.reservationNumber ?: reservation.reservationId ?: "Unknown",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                
                // Status Pill
                val statusText = reservation.status.lowercase().replaceFirstChar { it.uppercase() }
                val (bgColor, textColor) = when (statusText.lowercase()) {
                    "pending" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
                    "approved" -> Pair(Color(0xFFDCFCE7), Color(0xFF166534))
                    "cancelled" -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
                    else -> Pair(Color(0xFFE2E8F0), Color(0xFF475569))
                }
                
                Box(
                    modifier = Modifier
                        .background(bgColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = statusText,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = reservation.stationName ?: "Unknown Station",
                fontSize = 14.sp,
                color = Color(0xFF475569)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val formattedDate = try {
                    LocalDate.parse(reservation.bookingDate).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                } catch (e: Exception) {
                    reservation.bookingDate
                }
                
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Date",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedDate,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                Divider(
                    color = Color(0xFFE2E8F0),
                    modifier = Modifier
                        .height(14.dp)
                        .width(1.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                
                val formattedTime = try {
                    val st = LocalTime.parse(reservation.startTime)
                    val stStr = st.format(DateTimeFormatter.ofPattern("hh:mm a"))
                    if (reservation.endTime != null) {
                        val et = LocalTime.parse(reservation.endTime)
                        "$stStr - ${et.format(DateTimeFormatter.ofPattern("hh:mm a"))}"
                    } else {
                        stStr
                    }
                } catch (e: Exception) {
                    reservation.startTime
                }
                
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = "Time",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedTime,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

fun getStatusColor(status: String): Color {
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