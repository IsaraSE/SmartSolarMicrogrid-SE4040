/*
 * ApiService.kt
 * Smart Solar Microgrid Trading System - Prosumer Mobile Application
 *
 * Retrofit definition of every REST endpoint the Prosumer app calls on the
 * C# Web API. The app never touches MongoDB directly - all business logic and
 * validation (7-day rule, 12-hour rule) is enforced by the service.
 */
package com.smartsolarmicrogrid.prosumer.data.api

import com.smartsolarmicrogrid.prosumer.data.model.*
import retrofit2.Response
import retrofit2.http.*

// Note: the backend wraps every response in ApiResponse<T> ({ success, message, data }),
// so the actual payload for each call is inside `data`.
interface ApiService {

    // ---------- Auth ----------
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @PUT("api/auth/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Any>>

    // ---------- Prosumer account ----------
    @POST("api/prosumers/register")
    suspend fun registerProsumer(@Body request: RegisterRequest): Response<ApiResponse<Prosumer>>

    @GET("api/prosumers/{nic}")
    suspend fun getProsumer(@Path("nic") nic: String): Response<ApiResponse<Prosumer>>

    @PUT("api/prosumers/{nic}")
    suspend fun updateProsumer(@Path("nic") nic: String, @Body request: Prosumer): Response<ApiResponse<Prosumer>>

    @PUT("api/prosumers/{nic}/deactivate")
    suspend fun deactivateProsumer(@Path("nic") nic: String): Response<ApiResponse<Unit>>

    // ---------- Stations & Slots ----------
    @GET("api/stations")
    suspend fun getStations(): Response<ApiResponse<List<Station>>>

    @GET("api/stations/{id}/available-slots")
    suspend fun getAvailableSlots(@Path("id") stationId: String): Response<ApiResponse<List<Slot>>>

    @GET("api/slots/station/{stationId}")
    suspend fun getAllSlotsByStationId(@Path("stationId") stationId: String): Response<ApiResponse<List<Slot>>>

    @PUT("api/slots/{id}")
    suspend fun updateSlot(@Path("id") id: String, @Body request: UpdateSlotRequest): Response<ApiResponse<Slot>>

    // ---------- Reservations ----------
    @POST("api/reservations")
    suspend fun createReservation(@Body request: CreateReservationRequest): Response<ApiResponse<Reservation>>

    @GET("api/reservations/search")
    suspend fun getReservationsByStationId(@Query("stationId") stationId: String): Response<ApiResponse<List<Reservation>>>

    @PUT("api/reservations/{id}")
    suspend fun updateReservation(@Path("id") id: String, @Body request: CreateReservationRequest): Response<ApiResponse<Reservation>>

    @PUT("api/reservations/{id}/cancel")
    suspend fun cancelReservation(@Path("id") id: String): Response<ApiResponse<Reservation>>

    @GET("api/reservations/current/{prosumerNic}")
    suspend fun getCurrentReservations(@Path("prosumerNic") nic: String): Response<ApiResponse<List<Reservation>>>

    @GET("api/reservations/pending/{prosumerNic}")
    suspend fun getPendingReservations(@Path("prosumerNic") nic: String): Response<ApiResponse<List<Reservation>>>

    @GET("api/reservations/history/{prosumerNic}")
    suspend fun getReservationHistory(@Path("prosumerNic") nic: String): Response<ApiResponse<List<Reservation>>>

    /** Server-side search / filter used by the Booking List screen. */
    @GET("api/reservations/search")
    suspend fun searchReservations(
        @Query("nic") nic: String,
        @Query("keyword") keyword: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<Reservation>>>

    /** Single reservation, used to refresh details and read the QR reference. */
    @GET("api/reservations/{id}")
    suspend fun getReservation(@Path("id") id: String): Response<ApiResponse<Reservation>>

    // ---------- Grid operator ----------

    /** Reservations awaiting operator action across all prosumers. */
    @GET("api/reservations/operator/pending")
    suspend fun getOperatorPendingReservations(): Response<ApiResponse<List<Reservation>>>

    /** Operator approves a pending reservation, which issues its transaction QR. */
    @PUT("api/reservations/{id}/approve")
    suspend fun approveReservation(@Path("id") id: String): Response<ApiResponse<Reservation>>

    /** Verifies a scanned QR reference against the server. */
    @POST("api/qr/verify")
    suspend fun verifyQr(@Body request: QrVerifyRequest): Response<ApiResponse<QrVerificationResult>>

    /** Finalises the energy transfer once the QR has been verified. */
    @PUT("api/reservations/{id}/complete")
    suspend fun completeReservation(@Path("id") id: String): Response<ApiResponse<Reservation>>
}
