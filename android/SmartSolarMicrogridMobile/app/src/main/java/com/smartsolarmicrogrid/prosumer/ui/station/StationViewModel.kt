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

    fun loadStations() {
        stationListState = StationListState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getStations()
                val stations = response.body()?.data
                if (response.isSuccessful && stations != null) {
                    stationListState = StationListState.Loaded(stations)
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
                val slots = response.body()?.data
                if (response.isSuccessful && slots != null) {
                    slotListState = SlotListState.Loaded(slots)
                } else {
                    slotListState = SlotListState.Error("Could not load slots")
                }
            } catch (e: Exception) {
                slotListState = SlotListState.Error("Network error: ${e.message}")
            }
        }
    }
}