/*
 * QrVerification.kt
 * Smart Solar Microgrid Trading System - Prosumer Mobile Application
 *
 * Request and response models for grid operator QR verification. The scanned
 * reference is always checked against the server - the scan alone is not proof.
 */
package com.smartsolarmicrogrid.prosumer.data.model

/** Sent to the API with the reference read from the prosumer's QR code. */
data class QrVerifyRequest(
    val qrReference: String
)

/** What the server returns after checking the reference against the reservation records. */
data class QrVerificationResult(
    val valid: Boolean,
    val message: String? = null,
    val reservationId: String? = null,
    val prosumerNic: String? = null,
    val prosumerName: String? = null,
    val stationId: String? = null,
    val slotId: String? = null,
    val bookingDate: String? = null,
    val startTime: String? = null,
    val status: String? = null
)