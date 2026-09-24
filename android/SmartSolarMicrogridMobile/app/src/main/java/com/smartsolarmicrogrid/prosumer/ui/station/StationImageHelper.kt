package com.smartsolarmicrogrid.prosumer.ui.station

import com.smartsolarmicrogrid.prosumer.R
import com.smartsolarmicrogrid.prosumer.data.model.Station
import kotlin.math.abs

fun getStationImageRes(station: Station): Int {
    val name = station.stationName.lowercase()
    if (name.contains("jaffna")) return R.drawable.station_jaffna
    if (name.contains("colombo")) return R.drawable.station_colombo
    if (name.contains("kandy")) return R.drawable.station_kandy
    
    // Fallback to existing unique images (1, 2, 3) based on ID hash
    return when (abs(station.stationId.hashCode()) % 3) {
        0 -> R.drawable.station_1
        1 -> R.drawable.station_2
        else -> R.drawable.station_3
    }
}
