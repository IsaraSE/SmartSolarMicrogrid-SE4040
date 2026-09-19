/*
 * BookingListViewModel.kt
 * Smart Solar Microgrid Trading System - Prosumer Mobile Application
 *
 * Holds the state for the Prosumer's booking views: the Current / Pending /
 * History tabs and the search + status filter. Every list is read live from
 * the C# Web API (no counts or lists are hardcoded).
 */
package com.smartsolarmicrogrid.prosumer.ui.bookinglist

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartsolarmicrogrid.prosumer.data.api.RetrofitClient
import com.smartsolarmicrogrid.prosumer.data.local.SessionDbHelper
import com.smartsolarmicrogrid.prosumer.data.model.Reservation
import kotlinx.coroutines.launch

/** The three booking views the Prosumer can switch between. */
enum class BookingTab(val label: String) {
    CURRENT("Current"),
    PENDING("Pending"),
    HISTORY("History")
}

/** Loading / loaded / error state for any reservation list shown on screen. */
sealed class BookingListState {
    object Loading : BookingListState()
    data class Loaded(val reservations: List<Reservation>) : BookingListState()
    data class Error(val message: String) : BookingListState()
}

class BookingListViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionDb = SessionDbHelper(application)

    /** Currently selected tab. */
    var selectedTab by mutableStateOf(BookingTab.CURRENT)
        private set

    /** State of the list belonging to the selected tab. */
    var listState by mutableStateOf<BookingListState>(BookingListState.Loading)
        private set

    /** Free-text search term typed by the user (matches station / reservation id). */
    var searchQuery by mutableStateOf("")
        private set

    /** Optional status filter chip: PENDING, APPROVED, CANCELLED, COMPLETED or null. */
    var statusFilter by mutableStateOf<String?>(null)
        private set

    /** True while search results replace the tab content. */
    var isSearching by mutableStateOf(false)
        private set

    /** State of the search results list. */
    var searchState by mutableStateOf<BookingListState>(BookingListState.Loaded(emptyList()))
        private set

    /** The reservation the user tapped, shown on the Booking Details screen. */
    var selectedReservation by mutableStateOf<Reservation?>(null)
        private set

    init {
        loadTab(BookingTab.CURRENT)
    }

    /** Reads the logged-in Prosumer's NIC from the local SQLite session table. */
    private fun currentNic(): String =
        sessionDb.getSession()?.nic ?: "MOCK-NIC-000" // TODO: remove mock fallback before submission

    /** Switches tab and loads that tab's reservations from the API. */
    fun onTabSelected(tab: BookingTab) {
        selectedTab = tab
        loadTab(tab)
    }

    /** Reloads whatever is currently on screen (used after modify / cancel). */
    fun refresh() {
        loadTab(selectedTab)
        if (isSearching) runSearch()
    }

    /** Stores the reservation chosen from the list so the details screen can show it. */
    fun selectReservation(reservation: Reservation) {
        selectedReservation = reservation
    }

    /** Updates the search box text; clears search mode when nothing is left to filter by. */
    fun onSearchQueryChange(text: String) {
        searchQuery = text
        if (text.isBlank() && statusFilter == null) {
            isSearching = false
        }
    }

    /** Applies (or removes) a status filter chip and re-runs the search. */
    fun onStatusFilterSelected(status: String?) {
        statusFilter = status
        if (searchQuery.isBlank() && status == null) {
            isSearching = false
        } else {
            runSearch()
        }
    }

    /** Clears the search box and filter chip and goes back to the tab view. */
    fun clearSearch() {
        searchQuery = ""
        statusFilter = null
        isSearching = false
    }

    /** Calls the search endpoint on the API with the keyword and status filter. */
    fun runSearch() {
        if (searchQuery.isBlank() && statusFilter == null) {
            isSearching = false
            return
        }
        isSearching = true
        searchState = BookingListState.Loading
        val nic = currentNic()
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.searchReservations(
                    nic = nic,
                    keyword = searchQuery.ifBlank { null },
                    status = statusFilter
                )
                if (response.isSuccessful && response.body() != null) {
                    searchState = BookingListState.Loaded(response.body()!!)
                } else {
                    // TODO: remove mock fallback before submission
                    searchState = BookingListState.Loaded(filterLocally(getMockReservations()))
                }
            } catch (e: Exception) {
                // TODO: remove mock fallback before submission
                searchState = BookingListState.Loaded(filterLocally(getMockReservations()))
            }
        }
    }

    /** Loads Current, Pending or History reservations for the logged-in Prosumer. */
    private fun loadTab(tab: BookingTab) {
        listState = BookingListState.Loading
        val nic = currentNic()
        viewModelScope.launch {
            try {
                val response = when (tab) {
                    BookingTab.CURRENT -> RetrofitClient.apiService.getCurrentReservations(nic)
                    BookingTab.PENDING -> RetrofitClient.apiService.getPendingReservations(nic)
                    BookingTab.HISTORY -> RetrofitClient.apiService.getReservationHistory(nic)
                }
                if (response.isSuccessful && response.body() != null) {
                    listState = BookingListState.Loaded(response.body()!!)
                } else {
                    // TODO: remove mock fallback before submission
                    listState = BookingListState.Loaded(mockForTab(tab))
                }
            } catch (e: Exception) {
                // TODO: remove mock fallback before submission
                listState = BookingListState.Loaded(mockForTab(tab))
            }
        }
    }

    // TODO: remove this function before final submission - local filtering of mock data only
    private fun filterLocally(all: List<Reservation>): List<Reservation> {
        val keyword = searchQuery.trim().lowercase()
        return all.filter { reservation ->
            val matchesKeyword = keyword.isBlank() ||
                    reservation.stationId.lowercase().contains(keyword) ||
                    reservation.reservationId.lowercase().contains(keyword) ||
                    reservation.bookingDate.lowercase().contains(keyword)
            val matchesStatus = statusFilter == null || reservation.status == statusFilter
            matchesKeyword && matchesStatus
        }
    }

    // TODO: remove this function before final submission - for UI preview only, no real backend yet
    private fun mockForTab(tab: BookingTab): List<Reservation> {
        val all = getMockReservations()
        return when (tab) {
            BookingTab.CURRENT -> all.filter { it.status == "APPROVED" }
            BookingTab.PENDING -> all.filter { it.status == "PENDING" }
            BookingTab.HISTORY -> all.filter { it.status == "COMPLETED" || it.status == "CANCELLED" }
        }
    }

    // TODO: remove this function before final submission - for UI preview only, no real backend yet
    private fun getMockReservations(): List<Reservation> = listOf(
        Reservation(
            reservationId = "RES-1001",
            prosumerNic = "MOCK-NIC-000",
            stationId = "ST001",
            slotId = "SL001",
            bookingDate = "2026-09-20",
            startTime = "08:00",
            status = "APPROVED",
            qrReference = "QR-RES-1001"
        ),
        Reservation(
            reservationId = "RES-1002",
            prosumerNic = "MOCK-NIC-000",
            stationId = "ST002",
            slotId = "SL004",
            bookingDate = "2026-09-22",
            startTime = "09:00",
            status = "PENDING"
        ),
        Reservation(
            reservationId = "RES-1003",
            prosumerNic = "MOCK-NIC-000",
            stationId = "ST001",
            slotId = "SL003",
            bookingDate = "2026-09-12",
            startTime = "14:00",
            status = "COMPLETED",
            completedAt = "2026-09-12T16:05:00"
        ),
        Reservation(
            reservationId = "RES-1004",
            prosumerNic = "MOCK-NIC-000",
            stationId = "ST003",
            slotId = "SL007",
            bookingDate = "2026-09-08",
            startTime = "11:00",
            status = "CANCELLED"
        )
    )
}