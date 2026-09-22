package com.smartsolarmicrogrid.prosumer.data.model

data class Reservation(
    val reservationId: String,
    val reservationNumber: String? = null,
    val prosumerNic: String,
    val stationId: String,
    val stationName: String? = null,
    val slotId: String,
    val bookingDate: String,
    val startTime: String,
    val endTime: String? = null,
    val status: String,          // PENDING, APPROVED, CANCELLED, COMPLETED
    val qrReference: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val completedAt: String? = null,
    val notes: String? = null,
    val energyAmount: Double? = null
)

data class CreateReservationRequest(
    val prosumerNic: String,
    val stationId: String,
    val slotId: String,
    val bookingDate: String,
    val startTime: String,
    val notes: String? = null
)