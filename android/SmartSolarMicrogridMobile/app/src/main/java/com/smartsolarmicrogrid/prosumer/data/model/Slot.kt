package com.smartsolarmicrogrid.prosumer.data.model

data class Slot(
    val slotId: String,
    val stationId: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val status: String   // AVAILABLE, RESERVED, UNAVAILABLE
)