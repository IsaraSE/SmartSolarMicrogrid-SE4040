/*
 * OperatorViewModel.kt
 * Smart Solar Microgrid Trading System - Prosumer Mobile Application
 *
 * State for the grid operator mode: reservations awaiting action, approving a
 * booking, verifying a scanned QR against the server, and completing the
 * energy transfer once verification succeeds.
 */
package com.smartsolarmicrogrid.prosumer.ui.operator

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartsolarmicrogrid.prosumer.data.api.RetrofitClient
import com.smartsolarmicrogrid.prosumer.data.local.SessionDbHelper
import com.smartsolarmicrogrid.prosumer.data.model.QrVerificationResult
import com.smartsolarmicrogrid.prosumer.data.model.QrVerifyRequest
import com.smartsolarmicrogrid.prosumer.data.model.Reservation
import com.smartsolarmicrogrid.prosumer.data.model.Station
import kotlinx.coroutines.launch

/** Pending reservation list state. */
sealed class PendingListState {
    object Loading : PendingListState()
    data class Loaded(val reservations: List<Reservation>) : PendingListState()
    data class Error(val message: String) : PendingListState()
}

/** Station list state, used by the map screen. */
sealed class StationMapState {
    object Loading : StationMapState()
    data class Loaded(val stations: List<Station>) : StationMapState()
    data class Error(val message: String) : StationMapState()
}

/** State of a QR verification round trip. */
sealed class VerifyState {
    object Idle : VerifyState()
    object Loading : VerifyState()
    data class Result(val result: QrVerificationResult) : VerifyState()
    data class Error(val message: String) : VerifyState()
}

/** State of the "complete energy transfer" action. */
sealed class CompleteState {
    object Idle : CompleteState()
    object Loading : CompleteState()
    data class Success(val reservation: Reservation) : CompleteState()
    data class Error(val message: String) : CompleteState()
}

class OperatorViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionDb = SessionDbHelper(application)

    var operatorName by mutableStateOf(sessionDb.getSession()?.fullName ?: "Grid Operator")
        private set

    var pendingState by mutableStateOf<PendingListState>(PendingListState.Loading)
        private set

    var stationState by mutableStateOf<StationMapState>(StationMapState.Loading)
        private set

    var verifyState by mutableStateOf<VerifyState>(VerifyState.Idle)
        private set

    var completeState by mutableStateOf<CompleteState>(CompleteState.Idle)
        private set

    /** Transfers this operator has completed in the current session. */
    var completedCount by mutableStateOf(0)
        private set

    /** Message shown after approving a reservation. */
    var actionMessage by mutableStateOf<String?>(null)
        private set

    /** Live count of reservations waiting for approval. */
    val pendingCount: Int
        get() = (pendingState as? PendingListState.Loaded)?.reservations?.size ?: 0

    /** Live count of microgrid stations the operator covers. */
    val stationCount: Int
        get() = (stationState as? StationMapState.Loaded)?.stations?.size ?: 0

    init {
        loadPendingReservations()
        loadStations()
    }

    /** Loads every reservation waiting for operator action. */
    fun loadPendingReservations() {
        pendingState = PendingListState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getOperatorPendingReservations()
                if (response.isSuccessful && response.body() != null) {
                    pendingState = PendingListState.Loaded(response.body()!!)
                } else {
                    // TODO: remove mock fallback before submission
                    pendingState = PendingListState.Loaded(getMockPending())
                }
            } catch (e: Exception) {
                // TODO: remove mock fallback before submission
                pendingState = PendingListState.Loaded(getMockPending())
            }
        }
    }

    /** Loads the microgrid stations plotted on the operator map. */
    fun loadStations() {
        stationState = StationMapState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getStations()
                if (response.isSuccessful && response.body() != null) {
                    stationState = StationMapState.Loaded(response.body()!!)
                } else {
                    // TODO: remove mock fallback before submission
                    stationState = StationMapState.Loaded(getMockStations())
                }
            } catch (e: Exception) {
                // TODO: remove mock fallback before submission
                stationState = StationMapState.Loaded(getMockStations())
            }
        }
    }

    /** Reloads everything the dashboard shows. */
    fun refreshAll() {
        loadPendingReservations()
        loadStations()
    }

    /** Approves a pending reservation so the prosumer receives a transaction QR. */
    fun approveReservation(reservation: Reservation) {
        actionMessage = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.approveReservation(reservation.reservationId)
                actionMessage = if (response.isSuccessful) {
                    "Reservation ${reservation.reservationId} approved."
                } else {
                    "Could not approve reservation ${reservation.reservationId}."
                }
            } catch (e: Exception) {
                actionMessage = "Network error while approving the reservation."
            }
            loadPendingReservations()
        }
    }

    /** Clears the transient action message once it has been shown. */
    fun clearActionMessage() {
        actionMessage = null
    }

    /**
     * Sends the scanned reference to the API. The reservation details shown to the
     * operator come from the server response, never from the QR content itself.
     */
    fun verifyQrReference(reference: String) {
        verifyState = VerifyState.Loading
        completeState = CompleteState.Idle
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.verifyQr(QrVerifyRequest(reference))
                if (response.isSuccessful && response.body() != null) {
                    verifyState = VerifyState.Result(response.body()!!)
                } else if (response.code() == 404) {
                    verifyState = VerifyState.Result(
                        QrVerificationResult(valid = false, message = "No reservation matches this QR code.")
                    )
                } else {
                    // TODO: remove mock fallback before submission
                    verifyState = VerifyState.Result(getMockVerification(reference))
                }
            } catch (e: Exception) {
                // TODO: remove mock fallback before submission
                verifyState = VerifyState.Result(getMockVerification(reference))
            }
        }
    }

    /** Marks the reservation as COMPLETED after a successful verification. */
    fun completeTransfer(reservationId: String) {
        completeState = CompleteState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.completeReservation(reservationId)
                if (response.isSuccessful && response.body() != null) {
                    completeState = CompleteState.Success(response.body()!!)
                    completedCount += 1
                    loadPendingReservations()
                } else {
                    completeState = CompleteState.Error(
                        "Could not complete the transfer. Please try again."
                    )
                }
            } catch (e: Exception) {
                completeState = CompleteState.Error("Network error. Please try again.")
            }
        }
    }

    /** Resets the scan flow so the operator can scan the next prosumer. */
    fun resetScan() {
        verifyState = VerifyState.Idle
        completeState = CompleteState.Idle
    }

    // TODO: remove this function before final submission - for UI preview only, no real backend yet
    private fun getMockPending(): List<Reservation> = listOf(
        Reservation(
            reservationId = "RES-2001",
            prosumerNic = "200112345678",
            stationId = "ST001",
            slotId = "SL002",
            bookingDate = "2026-09-21",
            startTime = "09:00",
            status = "PENDING"
        ),
        Reservation(
            reservationId = "RES-2002",
            prosumerNic = "199845671234",
            stationId = "ST002",
            slotId = "SL005",
            bookingDate = "2026-09-22",
            startTime = "14:00",
            status = "PENDING"
        )
    )

    // TODO: remove this function before final submission - for UI preview only, no real backend yet
    private fun getMockStations(): List<Station> = listOf(
        Station("ST001", "Negombo Solar Hub", "Main Street, Negombo", 7.2083, 79.8358,
            120.0, 8, "06:00", "20:00", "ACTIVE"),
        Station("ST002", "Colombo Grid Node", "Galle Road, Colombo 03", 6.9271, 79.8612,
            200.0, 12, "06:00", "22:00", "ACTIVE"),
        Station("ST003", "Gampaha Microgrid", "Station Road, Gampaha", 7.0917, 79.9999,
            90.0, 6, "07:00", "19:00", "ACTIVE")
    )

    // TODO: remove this function before final submission - for UI preview only, no real backend yet
    private fun getMockVerification(reference: String): QrVerificationResult {
        // Mock rule: references starting with QR- look valid, anything else does not.
        return if (reference.startsWith("QR-")) {
            QrVerificationResult(
                valid = true,
                message = "Reservation verified.",
                reservationId = reference.removePrefix("QR-"),
                prosumerNic = "200112345678",
                prosumerName = "Mock Prosumer",
                stationId = "ST001",
                slotId = "SL001",
                bookingDate = "2026-09-20",
                startTime = "08:00",
                status = "APPROVED"
            )
        } else {
            QrVerificationResult(valid = false, message = "This QR code is not recognised.")
        }
    }
}