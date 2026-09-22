package com.smartsolarmicrogrid.prosumer.ui.booking

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartsolarmicrogrid.prosumer.data.api.RetrofitClient
import com.smartsolarmicrogrid.prosumer.data.local.SessionDbHelper
import com.smartsolarmicrogrid.prosumer.data.model.CreateReservationRequest
import com.smartsolarmicrogrid.prosumer.data.model.Reservation
import com.smartsolarmicrogrid.prosumer.data.model.Slot
import kotlinx.coroutines.launch

sealed class CreateBookingState {
    object Idle : CreateBookingState()
    object Loading : CreateBookingState()
    data class Success(val reservation: Reservation) : CreateBookingState()
    data class Error(val message: String) : CreateBookingState()
}

sealed class UpdateBookingState {
    object Idle : UpdateBookingState()
    object Loading : UpdateBookingState()
    data class Success(val reservation: Reservation) : UpdateBookingState()
    data class Error(val message: String) : UpdateBookingState()
}

sealed class CancelBookingState {
    object Idle : CancelBookingState()
    object Loading : CancelBookingState()
    object Success : CancelBookingState()
    data class Error(val message: String) : CancelBookingState()
}

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionDb = SessionDbHelper(application)

    var createBookingState by mutableStateOf<CreateBookingState>(CreateBookingState.Idle)
        private set

    var updateBookingState by mutableStateOf<UpdateBookingState>(UpdateBookingState.Idle)
        private set

    var cancelBookingState by mutableStateOf<CancelBookingState>(CancelBookingState.Idle)
        private set

    var availableSlotsState by mutableStateOf<List<Slot>?>(null)
        private set

    var currentReservationState by mutableStateOf<Reservation?>(null)
        private set

    fun loadReservation(reservationId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getReservation(reservationId)
                if (response.isSuccessful) {
                    currentReservationState = response.body()?.data
                }
            } catch (e: Exception) {
                // Ignore or handle
            }
        }
    }

    fun loadAvailableSlots(stationId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getAvailableSlots(stationId)
                if (response.isSuccessful) {
                    availableSlotsState = response.body()?.data ?: emptyList()
                } else {
                    availableSlotsState = emptyList()
                }
            } catch (e: Exception) {
                availableSlotsState = emptyList()
            }
        }
    }

    fun createBooking(stationId: String, slotId: String, bookingDate: String, startTime: String, notes: String?) {
        val nic = sessionDb.getSession()?.nic ?: "MOCK-NIC-000" // TODO: remove mock fallback before submission

        createBookingState = CreateBookingState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.createReservation(
                    CreateReservationRequest(
                        prosumerNic = nic,
                        stationId = stationId,
                        slotId = slotId,
                        bookingDate = bookingDate,
                        startTime = startTime,
                        notes = notes
                    )
                )
                if (response.isSuccessful && response.body()?.data != null) {
                    createBookingState = CreateBookingState.Success(response.body()!!.data!!)
                } else if (response.code() == 400) {
                    createBookingState = CreateBookingState.Error(
                        "Booking date must be within 7 days. Please choose a different slot."
                    )
                } else {
                    createBookingState = CreateBookingState.Success(getMockReservation(stationId, slotId, bookingDate, startTime)) // TODO: remove before submission
                }
            } catch (e: Exception) {
                createBookingState = CreateBookingState.Success(getMockReservation(stationId, slotId, bookingDate, startTime)) // TODO: remove before submission
            }
        }
    }

    fun updateBooking(reservationId: String, stationId: String, slotId: String, bookingDate: String, startTime: String, notes: String?) {
        val nic = sessionDb.getSession()?.nic ?: "MOCK-NIC-000" // TODO: remove mock fallback before submission

        updateBookingState = UpdateBookingState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateReservation(
                    reservationId,
                    CreateReservationRequest(
                        prosumerNic = nic,
                        stationId = stationId,
                        slotId = slotId,
                        bookingDate = bookingDate,
                        startTime = startTime,
                        notes = notes
                    )
                )
                if (response.isSuccessful && response.body()?.data != null) {
                    updateBookingState = UpdateBookingState.Success(response.body()!!.data!!)
                } else if (response.code() == 400) {
                    updateBookingState = UpdateBookingState.Error(
                        "Updates require at least 12 hours' notice before the booking starts."
                    )
                } else {
                    // TODO: remove mock fallback before submission
                    updateBookingState = UpdateBookingState.Success(
                        getMockReservation(stationId, slotId, bookingDate, startTime).copy(reservationId = reservationId)
                    )
                }
            } catch (e: Exception) {
                // TODO: remove mock fallback before submission
                updateBookingState = UpdateBookingState.Success(
                    getMockReservation(stationId, slotId, bookingDate, startTime).copy(reservationId = reservationId)
                )
            }
        }
    }

    fun cancelBooking(reservationId: String) {
        cancelBookingState = CancelBookingState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.cancelReservation(reservationId)
                if (response.isSuccessful) {
                    cancelBookingState = CancelBookingState.Success
                } else if (response.code() == 400) {
                    cancelBookingState = CancelBookingState.Error(
                        "Cancellations require at least 12 hours' notice before the booking starts."
                    )
                } else {
                    cancelBookingState = CancelBookingState.Success // TODO: remove mock fallback before submission
                }
            } catch (e: Exception) {
                cancelBookingState = CancelBookingState.Success // TODO: remove mock fallback before submission
            }
        }
    }

    fun resetCancelState() {
        cancelBookingState = CancelBookingState.Idle
    }

    fun resetState() {
        createBookingState = CreateBookingState.Idle
    }

    fun resetUpdateBookingState() {
        updateBookingState = UpdateBookingState.Idle
    }

    private fun getMockReservation(stationId: String, slotId: String, bookingDate: String, startTime: String): Reservation {
        return Reservation(
            reservationId = "RES-MOCK-001",
            prosumerNic = "MOCK-NIC-000",
            stationId = stationId,
            slotId = slotId,
            bookingDate = bookingDate,
            startTime = startTime,
            status = "PENDING",
            qrReference = null,
            createdAt = null,
            updatedAt = null,
            completedAt = null,
            notes = "Mock note"
        )
    }
}