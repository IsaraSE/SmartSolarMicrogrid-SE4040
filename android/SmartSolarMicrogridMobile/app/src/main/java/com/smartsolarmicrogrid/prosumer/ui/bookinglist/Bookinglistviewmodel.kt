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
enum class BookingTab(val label: String, val status: String?) {
    ALL("All", null),
    PENDING("Pending", "PENDING"),
    APPROVED("Approved", "APPROVED"),
    COMPLETED("Completed", "COMPLETED")
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
    var selectedTab by mutableStateOf(BookingTab.ALL)
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
        loadTab(BookingTab.ALL)
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
                if (response.isSuccessful && response.body()?.data != null) {
                    searchState = BookingListState.Loaded(response.body()!!.data!!)
                } else {
                    searchState = BookingListState.Error(response.message() ?: "Error searching reservations")
                }
            } catch (e: Exception) {
                searchState = BookingListState.Error(e.message ?: "Failed to connect to server")
            }
        }
    }

    /** Loads All, Pending, Approved, or Completed reservations for the logged-in Prosumer. */
    private fun loadTab(tab: BookingTab) {
        listState = BookingListState.Loading
        val nic = currentNic()
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.searchReservations(
                    nic = nic,
                    status = tab.status
                )
                
                if (response.isSuccessful && response.body()?.data != null) {
                    listState = BookingListState.Loaded(response.body()!!.data!!)
                } else {
                    listState = BookingListState.Error(response.message() ?: "Failed to fetch reservations")
                }
            } catch (e: Exception) {
                listState = BookingListState.Error(e.message ?: "Failed to connect to server")
            }
        }
    }

}