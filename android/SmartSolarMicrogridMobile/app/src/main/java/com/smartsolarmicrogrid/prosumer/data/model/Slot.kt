package com.smartsolarmicrogrid.prosumer.data.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class Slot(
    val slotId: String,
    val stationId: String,
    @SerializedName("startDateTime") val startDateTime: String,
    @SerializedName("endDateTime") val endDateTime: String,
    val capacity: Double = 0.0,
    val status: String,   // AVAILABLE, RESERVED, UNAVAILABLE
    @SerializedName("slotName") val slotName: String? = null
) {
    fun getFormattedDate(): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(startDateTime) ?: return "Unknown Date"
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            "Unknown Date"
        }
    }

    fun getFormattedStartTime(): String {
        return formatTime(startDateTime)
    }

    fun getFormattedEndTime(): String {
        return formatTime(endDateTime)
    }

    private fun formatTime(dateTimeStr: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(dateTimeStr) ?: return "Unknown Time"
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            "Unknown Time"
        }
    }
}

data class UpdateSlotRequest(
    val startDateTime: String,
    val endDateTime: String,
    val status: Int,
    val capacity: Double,
    val notes: String? = null
)