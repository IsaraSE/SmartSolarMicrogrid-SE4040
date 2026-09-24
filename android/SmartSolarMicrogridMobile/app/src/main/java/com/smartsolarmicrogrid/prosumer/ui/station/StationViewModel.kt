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
import com.google.android.gms.maps.model.LatLng
import android.location.Location

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

    var isMapView by mutableStateOf(false)

    var selectedStation by mutableStateOf<Station?>(null)
        private set

    var userLocation by mutableStateOf<LatLng?>(null)
        private set

    init {
        loadStations()
    }

    fun updateUserLocation(latLng: LatLng) {
        userLocation = latLng
        // Re-evaluate list if we just got location to inject nearby stations if needed
        val currentState = stationListState
        if (currentState is StationListState.Loaded) {
            injectNearbyStationsIfNeeded(currentState.stations)
        }
    }

    // Helper to calculate distance in meters
    fun calculateDistance(stationLat: Double, stationLng: Double): Float? {
        val uLoc = userLocation ?: return null
        val results = FloatArray(1)
        Location.distanceBetween(uLoc.latitude, uLoc.longitude, stationLat, stationLng, results)
        return results[0]
    }

    private fun injectNearbyStationsIfNeeded(currentStations: List<Station>) {
        val uLoc = userLocation ?: return
        
        // Count how many are within ~25km
        val nearbyCount = currentStations.count { 
            (calculateDistance(it.latitude, it.longitude) ?: Float.MAX_VALUE) < 25000f 
        }

        if (nearbyCount < 2) {
            // Inject a couple of dummy stations near the user
            val dummy1 = Station(
                stationId = "DUMMY_NEAR_1",
                stationName = "Local Grid Hub Alpha",
                address = "Nearby Location 1",
                latitude = uLoc.latitude + 0.015,
                longitude = uLoc.longitude + 0.015,
                capacity = 50.0,
                batterySlotCount = 10,
                operatingStartTime = "06:00",
                operatingEndTime = "22:00",
                status = "ACTIVE"
            )
            val dummy2 = Station(
                stationId = "DUMMY_NEAR_2",
                stationName = "Local Grid Hub Beta",
                address = "Nearby Location 2",
                latitude = uLoc.latitude - 0.02,
                longitude = uLoc.longitude + 0.01,
                capacity = 100.0,
                batterySlotCount = 20,
                operatingStartTime = "00:00",
                operatingEndTime = "23:59",
                status = "ACTIVE"
            )
            
            val updatedList = currentStations.toMutableList()
            if (updatedList.none { it.stationId == "DUMMY_NEAR_1" }) {
                updatedList.add(dummy1)
                updatedList.add(dummy2)
                stationListState = StationListState.Loaded(updatedList)
            }
        }
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
                val stations = response.body()?.data
                if (response.isSuccessful && stations != null) {
                    stationListState = StationListState.Loaded(stations)
                    if (userLocation != null) {
                        injectNearbyStationsIfNeeded(stations)
                    }
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

    fun selectStationAndLoadAllSlots(station: Station) {
        selectedStation = station
        slotListState = SlotListState.Loading
        viewModelScope.launch {
            try {
                val slotsResponse = RetrofitClient.apiService.getAllSlotsByStationId(station.stationId)
                val reservationsResponse = try {
                    RetrofitClient.apiService.getReservationsByStationId(station.stationId)
                } catch (e: Exception) {
                    null
                }
                
                val slots = slotsResponse.body()?.data
                val reservations = reservationsResponse?.body()?.data ?: emptyList()
                
                if (slotsResponse.isSuccessful && slots != null) {
                    val mergedSlots = slots.map { slot ->
                        val activeRes = reservations.find { r -> 
                            r.slotId == slot.slotId && 
                            (r.status.uppercase() == "PENDING" || r.status == "0" || 
                             r.status.uppercase() == "APPROVED" || r.status == "1")
                        }
                        
                        var effectiveStatus = slot.status.uppercase()
                        if (activeRes != null) {
                            val resStatus = activeRes.status.uppercase()
                            if (resStatus == "PENDING" || resStatus == "0") effectiveStatus = "3" // PENDING
                            if (resStatus == "APPROVED" || resStatus == "1") effectiveStatus = "1" // RESERVED
                        }
                        
                        slot.copy(status = effectiveStatus)
                    }
                    slotListState = SlotListState.Loaded(mergedSlots)
                } else {
                    slotListState = SlotListState.Error("Could not load slots")
                }
            } catch (e: Exception) {
                slotListState = SlotListState.Error("Network error: ${e.message}")
            }
        }
    }

    fun toggleSlotStatus(slot: com.smartsolarmicrogrid.prosumer.data.model.Slot) {
        val currentStatus = slot.status.uppercase()
        val newStatus = if (currentStatus == "AVAILABLE" || currentStatus == "0") 2 else 0 // 0=AVAILABLE, 2=UNAVAILABLE
        
        val request = com.smartsolarmicrogrid.prosumer.data.model.UpdateSlotRequest(
            startDateTime = slot.startDateTime,
            endDateTime = slot.endDateTime,
            status = newStatus,
            capacity = slot.capacity
        )

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateSlot(slot.slotId, request)
                if (response.isSuccessful) {
                    // Reload the slots for the currently selected station
                    selectedStation?.let { selectStationAndLoadAllSlots(it) }
                }
            } catch (e: Exception) {
                // handle error or show toast
            }
        }
    }

    fun createSlot(
        request: com.smartsolarmicrogrid.prosumer.data.model.CreateSlotRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.createSlot(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess()
                } else {
                    onError(response.body()?.message ?: "Failed to create slot")
                }
            } catch (e: Exception) {
                onError("Network error: ${e.message}")
            }
        }
    }
}