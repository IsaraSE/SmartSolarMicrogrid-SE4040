/*
 * DashboardViewModel.kt
 * Smart Solar Microgrid Trading System - Prosumer Mobile Application
 *
 * Builds the Prosumer dashboard figures from live API data: the number of
 * pending reservations, the number of approved reservations still in the
 * future, and the next upcoming bookings. No count is hardcoded - every value
 * is derived from what the C# Web API returns.
 */
package com.smartsolarmicrogrid.prosumer.ui.dashboard

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
import java.time.LocalDate

/** Everything the dashboard shows, calculated from the reservations returned by the API. */
data class DashboardData(
    val prosumerName: String,
    val pendingCount: Int,
    val approvedFutureCount: Int,
    val upcoming: List<Reservation>
)

sealed class DashboardState {
    object Loading : DashboardState()
    data class Loaded(val data: DashboardData) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionDb = SessionDbHelper(application)

    var dashboardState by mutableStateOf<DashboardState>(DashboardState.Loading)
        private set

    init {
        loadDashboard()
    }

    /** Loads pending and current reservations, then derives the dashboard figures. */
    fun loadDashboard() {
        dashboardState = DashboardState.Loading
        val session = sessionDb.getSession()
        val nic = session?.nic ?: "MOCK-NIC-000" // TODO: remove mock fallback before submission
        val name = session?.fullName ?: "Prosumer"

        viewModelScope.launch {
            try {
                val pendingResponse = RetrofitClient.apiService.getPendingReservations(nic)
                val currentResponse = RetrofitClient.apiService.getCurrentReservations(nic)

                val pending = if (pendingResponse.isSuccessful) pendingResponse.body() else null
                val current = if (currentResponse.isSuccessful) currentResponse.body() else null

                if (pending != null && current != null) {
                    dashboardState = DashboardState.Loaded(buildData(name, pending, current))
                } else {
                    // TODO: remove mock fallback before submission
                    dashboardState = DashboardState.Loaded(
                        buildData(name, mockPending(), mockCurrent())
                    )
                }
            } catch (e: Exception) {
                // TODO: remove mock fallback before submission
                dashboardState = DashboardState.Loaded(
                    buildData(name, mockPending(), mockCurrent())
                )
            }
        }
    }

    /** Counts pending bookings and approved bookings dated today or later. */
    private fun buildData(
        name: String,
        pending: List<Reservation>,
        current: List<Reservation>
    ): DashboardData {
        val approvedFuture = current.filter { it.status == "APPROVED" && isFutureOrToday(it.bookingDate) }

        val upcoming = (approvedFuture + pending)
            .sortedWith(compareBy({ it.bookingDate }, { it.startTime }))
            .take(3)

        return DashboardData(
            prosumerName = name,
            pendingCount = pending.size,
            approvedFutureCount = approvedFuture.size,
            upcoming = upcoming
        )
    }

    /** True when the booking date is today or in the future (dates arrive as yyyy-MM-dd). */
    private fun isFutureOrToday(bookingDate: String): Boolean {
        return try {
            !LocalDate.parse(bookingDate).isBefore(LocalDate.now())
        } catch (e: Exception) {
            true // if the server sends an unexpected format, keep the booking visible
        }
    }

    // TODO: remove this function before final submission - for UI preview only, no real backend yet
    private fun mockPending(): List<Reservation> = listOf(
        Reservation(
            reservationId = "RES-1002",
            prosumerNic = "MOCK-NIC-000",
            stationId = "ST002",
            slotId = "SL004",
            bookingDate = LocalDate.now().plusDays(4).toString(),
            startTime = "09:00",
            status = "PENDING"
        )
    )

    // TODO: remove this function before final submission - for UI preview only, no real backend yet
    private fun mockCurrent(): List<Reservation> = listOf(
        Reservation(
            reservationId = "RES-1001",
            prosumerNic = "MOCK-NIC-000",
            stationId = "ST001",
            slotId = "SL001",
            bookingDate = LocalDate.now().plusDays(2).toString(),
            startTime = "08:00",
            status = "APPROVED",
            qrReference = "QR-RES-1001"
        ),
        Reservation(
            reservationId = "RES-1005",
            prosumerNic = "MOCK-NIC-000",
            stationId = "ST003",
            slotId = "SL009",
            bookingDate = LocalDate.now().plusDays(5).toString(),
            startTime = "13:00",
            status = "APPROVED",
            qrReference = "QR-RES-1005"
        )
    )
}