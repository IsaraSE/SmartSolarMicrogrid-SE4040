package com.smartsolarmicrogrid.prosumer.ui.operator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsolarmicrogrid.prosumer.data.model.Reservation
import com.smartsolarmicrogrid.prosumer.ui.bookinglist.BookingListState
import com.smartsolarmicrogrid.prosumer.ui.bookinglist.BookingListViewModel
import com.smartsolarmicrogrid.prosumer.ui.bookinglist.BookingTab
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val BrandGreen = Color(0xFF0C8A44)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorReservationsScreen(
    onScanQr: () -> Unit,
    onNavigateToDetails: (Reservation) -> Unit,
    operatorViewModel: OperatorViewModel = viewModel(),
    bookingListViewModel: BookingListViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Show the result of an approve action, then clear it, and refresh the list!
    LaunchedEffect(operatorViewModel.actionMessage) {
        operatorViewModel.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            operatorViewModel.clearActionMessage()
            bookingListViewModel.refresh() // Refresh list after approve
        }
    }

    val selectedTabIndex = bookingListViewModel.selectedTab.ordinal
    val listState = if (bookingListViewModel.isSearching) bookingListViewModel.searchState else bookingListViewModel.listState
    val tabs = BookingTab.values().filter { it != BookingTab.COMPLETED } // Match UI
    
    var selectedStationFilter by remember { mutableStateOf("All Stations") }
    var isStationDropdownExpanded by remember { mutableStateOf(false) }

    val reservations = (listState as? BookingListState.Loaded)?.reservations ?: emptyList()
    val availableStations = listOf("All Stations") + reservations.mapNotNull { it.stationName }.distinct().sorted()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandGreen)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .padding(top = 16.dp), // Approximate safe area
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Reservations",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
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
                    


                    // Search Bar
                    OutlinedTextField(
                        value = bookingListViewModel.searchQuery,
                        onValueChange = { bookingListViewModel.onSearchQueryChange(it) },
                        placeholder = { Text("Search by Station or Prosumer NIC", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

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
                                    .background(if (isSelected) BrandGreen else Color(0xFFF1F5F9))
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

                    // Station Dropdown
                    ExposedDropdownMenuBox(
                        expanded = isStationDropdownExpanded,
                        onExpandedChange = { isStationDropdownExpanded = !isStationDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedStationFilter,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(Icons.Outlined.LocationOn, contentDescription = "Location", tint = Color(0xFF475569))
                            },
                            trailingIcon = {
                                Icon(Icons.Default.UnfoldMore, contentDescription = "Expand", tint = Color(0xFF0F172A))
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE2E8F0),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = isStationDropdownExpanded,
                            onDismissRequest = { isStationDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            availableStations.forEach { station ->
                                val isSelected = station == selectedStationFilter
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = station,
                                            color = Color(0xFF0F172A),
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    },
                                    trailingIcon = {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = BrandGreen)
                                        }
                                    },
                                    modifier = Modifier.background(
                                        if (isSelected) BrandGreen.copy(alpha = 0.05f) else Color.Transparent
                                    ),
                                    onClick = {
                                        selectedStationFilter = station
                                        isStationDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // List
                    when (listState) {
                        is BookingListState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = BrandGreen)
                            }
                        }
                        is BookingListState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(listState.message, color = Color.Red)
                            }
                        }
                        is BookingListState.Loaded -> {
                            val loadedReservations = listState.reservations
                            
                            // Client-side filter to support real-time station and search filtering
                            val searchQuery = bookingListViewModel.searchQuery.lowercase()
                            val filteredList = loadedReservations.filter {
                                val matchesStation = selectedStationFilter == "All Stations" || it.stationName == selectedStationFilter
                                val matchesSearch = searchQuery.isBlank() || 
                                    it.prosumerNic.lowercase().contains(searchQuery) || 
                                    (it.stationName ?: "").lowercase().contains(searchQuery) ||
                                    (it.reservationNumber ?: it.reservationId).lowercase().contains(searchQuery)
                                matchesStation && matchesSearch
                            }

                            if (filteredList.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No reservations found.", color = Color.Gray)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    items(filteredList) { reservation ->
                                        OperatorBookingCard(
                                            reservation = reservation,
                                            onClick = { onNavigateToDetails(reservation) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun OperatorBookingCard(reservation: Reservation, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NIC ${reservation.prosumerNic}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                }
                
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
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "${reservation.stationName ?: "Unknown Station"} • ${reservation.reservationNumber ?: "#RES${reservation.reservationId.takeLast(4).uppercase()}"}",
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
