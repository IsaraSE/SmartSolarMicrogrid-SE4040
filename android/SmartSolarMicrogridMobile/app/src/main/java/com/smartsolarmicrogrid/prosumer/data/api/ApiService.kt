package com.smartsolarmicrogrid.prosumer.data.api

import com.smartsolarmicrogrid.prosumer.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ---------- Auth ----------
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ---------- Prosumer account ----------
    @POST("api/prosumers/register")
    suspend fun registerProsumer(@Body request: RegisterRequest): Response<Prosumer>

    @GET("api/prosumers/{nic}")
    suspend fun getProsumer(@Path("nic") nic: String): Response<Prosumer>

    @PUT("api/prosumers/{nic}")
    suspend fun updateProsumer(@Path("nic") nic: String, @Body request: Prosumer): Response<Prosumer>

    @PUT("api/prosumers/{nic}/deactivate")
    suspend fun deactivateProsumer(@Path("nic") nic: String): Response<Unit>

    // ---------- Stations & Slots ----------
    @GET("api/stations")
    suspend fun getStations(): Response<List<Station>>

    @GET("api/stations/{id}/available-slots")
    suspend fun getAvailableSlots(@Path("id") stationId: String): Response<List<Slot>>

    // ---------- Reservations ----------
    @POST("api/reservations")
    suspend fun createReservation(@Body request: CreateReservationRequest): Response<Reservation>

    @PUT("api/reservations/{id}")
    suspend fun updateReservation(@Path("id") id: String, @Body request: CreateReservationRequest): Response<Reservation>

    @PUT("api/reservations/{id}/cancel")
    suspend fun cancelReservation(@Path("id") id: String): Response<Reservation>

    @GET("api/reservations/current/{prosumerNic}")
    suspend fun getCurrentReservations(@Path("prosumerNic") nic: String): Response<List<Reservation>>

    @GET("api/reservations/pending/{prosumerNic}")
    suspend fun getPendingReservations(@Path("prosumerNic") nic: String): Response<List<Reservation>>

    @GET("api/reservations/history/{prosumerNic}")
    suspend fun getReservationHistory(@Path("prosumerNic") nic: String): Response<List<Reservation>>
}