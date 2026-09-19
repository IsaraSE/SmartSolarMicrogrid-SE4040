package com.smartsolarmicrogrid.prosumer.ui.station

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartsolarmicrogrid.prosumer.data.api.RetrofitClient
import com.smartsolarmicrogrid.prosumer.data.model.Slot
import com.smartsolarmicrogrid.prosumer.data.model.Station
import kotlinx.coroutines.launch

sealed class StationListState {
    object Loading : StationListState()
    data class Loaded(val stations: List<Station>) : StationListState()
    data class Error(val message: String) : StationListState()
}

sealed class SlotListState {
    object Idle : SlotListState()
    object Loading : SlotListState()
    data class Loaded(val slots: List<Slot>) : SlotListState()
    data class Error(val message: String) : SlotListState()
}

class StationViewModel(application: Application) : AndroidViewModel(application) {

    var stationListState by mutableStateOf<StationListState>(StationListState.Loading)
        private set

    var slotListState by mutableStateOf<SlotListState>(SlotListState.Idle)
        private set

    var selectedStation by mutableStateOf<Station?>(null)
        private set

    init {
        loadStations()
    }

    var selectedSlot by mutableStateOf<Slot?>(null)
        private set

    fun selectSlot(slot: Slot) {
        selectedSlot = slot
    }

    fun loadStations() {
        stationListState = StationListState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getStations()
                if (response.isSuccessful && response.body() != null) {
                    stationListState = StationListState.Loaded(response.body()!!)
                } else {
                    stationListState = StationListState.Error("Could not load stations")
                }
            } catch (e: Exception) {
                stationListState = StationListState.Error("Network error: ${e.message}")
            }
        }
    }

    fun selectStationAndLoadSlots(station: Station) {
        selectedStation = station
        slotListState = SlotListState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getAvailableSlots(station.stationId)
                if (response.isSuccessful && response.body() != null) {
                    slotListState = SlotListState.Loaded(response.body()!!)
                } else {
                    slotListState =
                        SlotListState.Loaded(getMockSlots(station.stationId)) // TODO: remove before submission
                }
            } catch (e: Exception) {
                slotListState =
                    SlotListState.Loaded(getMockSlots(station.stationId)) // TODO: remove before submission
            }
        }
    }

    // TODO: remove this function before final submission — for UI preview only, no real backend yet
    private fun getMockSlots(stationId: String): List<Slot> {
        return listOf(
            Slot(
                slotId = "SL001",
                stationId = stationId,
                date = "2026-09-20",
                startTime = "08:00",
                endTime = "10:00",
                status = "AVAILABLE"
            ),
            Slot(
                slotId = "SL002",
                stationId = stationId,
                date = "2026-09-20",
                startTime = "10:00",
                endTime = "12:00",
                status = "AVAILABLE"
            ),
            Slot(
                slotId = "SL003",
                stationId = stationId,
                date = "2026-09-21",
                startTime = "14:00",
                endTime = "16:00",
                status = "AVAILABLE"
            ),
            Slot(
                slotId = "SL004",
                stationId = stationId,
                date = "2026-09-22",
                startTime = "09:00",
                endTime = "11:00",
                status = "AVAILABLE"
            )
        )
    }
}