package com.smartsolarmicrogrid.prosumer.data.model

data class LoginRequest(
    val email: String,   // the backend authenticates by email (LoginRequestDto.Email)
    val password: String
)

data class LoginResponse(
    val userId: String,
    val nic: String,
    val fullName: String,
    val role: String,
    val accountStatus: String,
    val token: String? = null
)

data class RegisterRequest(
    val nic: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val address: String,
    val password: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
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