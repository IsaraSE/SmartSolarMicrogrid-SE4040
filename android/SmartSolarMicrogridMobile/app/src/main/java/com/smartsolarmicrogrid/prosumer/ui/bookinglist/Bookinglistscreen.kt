/*
 * BookingListScreen.kt
 * Smart Solar Microgrid Trading System - Prosumer Mobile Application
 *
 * Shows the Prosumer's reservations in three tabs (Current, Pending, History)
 * with a keyword search box and status filter chips. All data is fetched from
 * the C# Web API through BookingListViewModel.
 */
package com.smartsolarmicrogrid.prosumer.ui.bookinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsolarmicrogrid.prosumer.data.model.Reservation

private val SolarGreenDark = Color(0xFF1B5E20)
private val SolarGreen = Color(0xFF2E7D32)
private val SolarAmber = Color(0xFFFFA000)
private val SurfaceGray = Color(0xFFF5F5F5)

/** Status values the user can filter by. */
private val StatusFilters = listOf("PENDING", "APPROVED", "COMPLETED", "CANCELLED")

@Composable
fun BookingListScreen(
    onBookingSelected: (Reservation) -> Unit,
    onBack: () -> Unit,
    bookingListViewModel: BookingListViewModel = viewModel()
) {
    val state = if (bookingListViewModel.isSearching) {
        bookingListViewModel.searchState
    } else {
        bookingListViewModel.listState
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SolarGreenDark, SolarGreen, SurfaceGray),
                    startY = 0f,
                    endY = 520f
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

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
                Column(modifier = Modifier.weight(1f)) {
                    Text("My Bookings", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Current, pending and past reservations",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = { bookingListViewModel.refresh() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.White)
                }
            }

            // Search box
            OutlinedTextField(
                value = bookingListViewModel.searchQuery,
                onValueChange = { bookingListViewModel.onSearchQueryChange(it) },
                placeholder = { Text("Search by station, date or booking ID") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (bookingListViewModel.searchQuery.isNotBlank()) {
                        IconButton(onClick = { bookingListViewModel.clearSearch() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    } else {
                        IconButton(onClick = { bookingListViewModel.runSearch() }) {
                            Icon(Icons.Filled.ArrowForward, contentDescription = "Search")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = SolarAmber,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusFilters.forEach { status ->
                    val selected = bookingListViewModel.statusFilter == status
                    FilterPill(
                        label = status.lowercase().replaceFirstChar { it.uppercase() },
                        selected = selected,
                        onClick = {
                            bookingListViewModel.onStatusFilterSelected(if (selected) null else status)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tabs (hidden while search results are shown)
            if (!bookingListViewModel.isSearching) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .padding(4.dp)
                ) {
                    BookingTab.values().forEach { tab ->
                        val selected = bookingListViewModel.selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(11.dp))
                                .background(if (selected) Color.White else Color.Transparent)
                                .clickable { bookingListViewModel.onTabSelected(tab) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.label,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) SolarGreenDark else Color.White
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Search results",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results
            when (state) {
                is BookingListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SolarGreen)
                    }
                }

                is BookingListState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.CloudOff, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(state.message, color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { bookingListViewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }

                is BookingListState.Loaded -> {
                    if (state.reservations.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.EventBusy, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No bookings to show here", color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.reservations) { reservation ->
                                BookingCard(
                                    reservation = reservation,
                                    onClick = { onBookingSelected(reservation) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** A single reservation row in the list. */
@Composable
private fun BookingCard(reservation: Reservation, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SolarGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.BatteryChargingFull, contentDescription = null, tint = SolarGreen)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Station ${reservation.stationId}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${reservation.bookingDate}  •  ${reservation.startTime}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = reservation.reservationId,
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }
            StatusBadge(status = reservation.status)
        }
    }
}

/** Coloured pill showing the reservation status. */
@Composable
fun StatusBadge(status: String) {
    val background = when (status) {
        "APPROVED" -> SolarGreen.copy(alpha = 0.14f)
        "PENDING" -> SolarAmber.copy(alpha = 0.18f)
        "COMPLETED" -> Color(0xFF1565C0).copy(alpha = 0.12f)
        else -> Color.Red.copy(alpha = 0.10f)
    }
    val textColor = when (status) {
        "APPROVED" -> SolarGreen
        "PENDING" -> Color(0xFFB77400)
        "COMPLETED" -> Color(0xFF1565C0)
        else -> Color(0xFFC62828)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = status, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

/** Small selectable chip used for the status filters. */
@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) SolarAmber else Color.White.copy(alpha = 0.22f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color(0xFF3E2600) else Color.White
        )
    }
}