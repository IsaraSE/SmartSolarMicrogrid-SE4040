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

    // ---------- Reservations ----------
    @POST("api/reservations")
    suspend fun createReservation(@Body request: CreateReservationRequest): Response<ApiResponse<Reservation>>

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
}
