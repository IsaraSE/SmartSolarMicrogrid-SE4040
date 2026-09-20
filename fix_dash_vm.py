import os

path = "/Users/isara/4Y_2S/EAD/SmartSolarMicrogrid-SE4040/android/SmartSolarMicrogridMobile/app/src/main/java/com/smartsolarmicrogrid/prosumer/ui/dashboard/Dashboardviewmode.kt"

content = """package com.smartsolarmicrogrid.prosumer.ui.dashboard

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

                // Merge activity feed
                val activityItems = mutableListOf<ActivityItem>()
                
                // Add pending activities
                pending.forEach { res ->
                    activityItems.add(ActivityItem(
                        title = "Reservation pending",
                        subtitle = "${formatStationName(res.stationId)} • ${formatActivityDate(res.bookingDate)}",
                        status = "PENDING",
                        date = res.bookingDate
                    ))
                }
                
                // Add approved activities
                current.filter { it.status == "APPROVED" }.forEach { res ->
                    activityItems.add(ActivityItem(
                        title = "Reservation approved",
                        subtitle = "${formatStationName(res.stationId)} • ${formatActivityDate(res.bookingDate)}",
                        status = "APPROVED",
                        date = res.bookingDate
                    ))
                }

                // Add completed activities
                completedList.forEach { res ->
                    activityItems.add(ActivityItem(
                        title = "Reservation completed",
                        subtitle = "${formatStationName(res.stationId)} • ${formatActivityDate(res.bookingDate)}",
                        status = "COMPLETED",
                        date = res.bookingDate
                    ))
                }
                
                // Sort by most recent date (descending)
                val sortedActivity = activityItems.sortedByDescending { it.date }.take(5)
                
                // Construct Data
                val data = DashboardData(
                    prosumerName = name,
                    pendingCount = pending.size,
                    upcomingCount = upcomingList.size,
                    completedCount = completedList.size,
                    totalEnergyTraded = completedList.size * 2.5,
                    upcomingReservation = nextUpcoming,
                    recentActivity = sortedActivity
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
            !LocalDate.parse(bookingDate).isBefore(LocalDate.now())
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
    
    private fun formatActivityDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString)
            val day = date.dayOfMonth
            val month = date.month.name.substring(0, 3).lowercase().replaceFirstChar { it.uppercase() }
            val year = date.year
            "$day $month $year"
        } catch (e: Exception) {
            dateString
        }
    }
}
"""

with open(path, "w") as f:
    f.write(content)
print("DashboardViewModel updated for new UI requirements.")
