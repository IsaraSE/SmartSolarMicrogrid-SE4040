package com.smartsolarmicrogrid.prosumer.data.model

data class Station(
    val stationId: String,
    val stationName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val capacity: Double,
    val batterySlotCount: Int,
    val operatingStartTime: String,
    val operatingEndTime: String,
    val status: String
)