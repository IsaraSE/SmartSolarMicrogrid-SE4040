package com.smartsolarmicrogrid.prosumer.data.model

data class LoginRequest(
    val username: String,   // email or NIC, per your team's login API
    val password: String
)

data class LoginResponse(
    val userId: String,
    val fullName: String,
    val role: String,
    val accountStatus: String
)

data class RegisterRequest(
    val nic: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String,
    val password: String
)

data class Prosumer(
    val nic: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String,
    val accountStatus: String,
    val createdAt: String? = null
)