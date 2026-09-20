package com.smartsolarmicrogrid.prosumer.data.model

/**
 * Matches the backend's standard response envelope: { success, message, data }.
 * The actual payload is inside [data].
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)
