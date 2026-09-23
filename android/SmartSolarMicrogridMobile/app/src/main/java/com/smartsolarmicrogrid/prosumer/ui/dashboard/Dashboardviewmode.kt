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

data class ActivityItem(
    val reservation: Reservation,
    val title: String,
    val subtitle: String,
    val status: String,
    val date: String
)

data class DashboardData(
    val prosumerName: String,
    val pendingCount: Int,
    val upcomingCount: Int,
    val completedCount: Int,
    val totalEnergyTraded: Double,
    val upcomingReservation: Reservation?,
    val recentActivity: List<ActivityItem>
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

    fun loadDashboard() {
        dashboardState = DashboardState.Loading
        val session = sessionDb.getSession()
        val nic = session?.nic ?: "MOCK-NIC-000"
        val name = session?.fullName ?: "Prosumer"

        viewModelScope.launch {
            try {
                // Fetch all categories of reservations
                val pendingResponse = RetrofitClient.apiService.getPendingReservations(nic)
                val currentResponse = RetrofitClient.apiService.getCurrentReservations(nic)
                val historyResponse = RetrofitClient.apiService.getReservationHistory(nic)

                val pending = if (pendingResponse.isSuccessful) pendingResponse.body()?.data ?: emptyList() else emptyList()
                val current = if (currentResponse.isSuccessful) currentResponse.body()?.data ?: emptyList() else emptyList()
                val history = if (historyResponse.isSuccessful) historyResponse.body()?.data ?: emptyList() else emptyList()

                // Calculate upcoming logic
                val upcomingList = current.filter { it.status == "APPROVED" && isFutureOrToday(it.bookingDate) }
                    .sortedWith(compareBy({ it.bookingDate }, { it.startTime }))
                
                // For energy calculation
                val completedList = history.filter { it.status == "COMPLETED" }

                // Determine next upcoming
                val nextUpcoming = upcomingList.firstOrNull()

                // Merge all reservations uniquely
                val allReservations = (pending + current + history).distinctBy { it.reservationId }
                
                // Sort by most recently updated/created for a true "Recent Activity" feed
                val sortedRecent = allReservations
                    .sortedByDescending { it.updatedAt ?: it.createdAt ?: it.bookingDate }
                    .take(5)
                
                val activityItems = sortedRecent.map { res ->
                    val actionStr = when (res.status) {
                        "PENDING" -> "Reservation requested"
                        "APPROVED" -> "Reservation approved"
                        "CANCELLED" -> "Reservation cancelled"
                        "COMPLETED" -> "Reservation completed"
                        else -> "Reservation updated"
                    }
                    val dateStr = res.updatedAt ?: res.createdAt ?: res.bookingDate
                    val name = res.stationName ?: formatStationName(res.stationId)
                    ActivityItem(
                        reservation = res,
                        title = actionStr,
                        subtitle = "$name • ${formatActivityDate(res.bookingDate)} • ${formatTime(res.startTime)} - ${formatTime(res.endTime)}",
                        status = res.status,
                        date = dateStr
                    )
                }
                
                // Construct Data
                val data = DashboardData(
                    prosumerName = name,
                    pendingCount = pending.size,
                    upcomingCount = upcomingList.size,
                    completedCount = completedList.size,
                    totalEnergyTraded = completedList.size * 2.5,
                    upcomingReservation = nextUpcoming,
                    recentActivity = activityItems
                )
                
                dashboardState = DashboardState.Loaded(data)
                
            } catch (e: Exception) {
                // Fallback or error state
                dashboardState = DashboardState.Error("Failed to load dashboard data.")
            }
        }
    }

    private fun isFutureOrToday(bookingDate: String): Boolean {
        return try {
            val datePart = bookingDate.substringBefore("T")
            !LocalDate.parse(datePart).isBefore(LocalDate.now())
        } catch (e: Exception) {
            true
        }
    }
    
    private fun formatStationName(stationId: String): String {
        return when (stationId) {
            "ST001" -> "Colombo Solar Hub"
            "ST002" -> "Kandy Solar Hub"
            "ST003" -> "Galle Solar Hub"
            else -> "Station $stationId"
        }
    }
    
    private fun formatActivityDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return try {
            val datePart = dateString.substringBefore("T")
            val date = LocalDate.parse(datePart)
            val day = date.dayOfMonth
            val month = date.month.name.substring(0, 3).lowercase().replaceFirstChar { it.uppercase() }
            val year = date.year
            "$day $month $year"
        } catch (e: Exception) {
            dateString
        }
    }
    
    private fun formatTime(timeStr: String?): String {
        if (timeStr.isNullOrEmpty()) return ""
        return try {
            val parts = timeStr.split(":")
            val hour = parts[0].toInt()
            val min = parts[1]
            val amPm = if (hour >= 12) "PM" else "AM"
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            "$displayHour:$min $amPm"
        } catch (e: Exception) {
            timeStr
        }
    }
}
