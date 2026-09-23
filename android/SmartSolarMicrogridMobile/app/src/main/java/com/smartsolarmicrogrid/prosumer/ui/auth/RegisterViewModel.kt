package com.smartsolarmicrogrid.prosumer.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartsolarmicrogrid.prosumer.data.api.RetrofitClient
import com.smartsolarmicrogrid.prosumer.data.model.RegisterRequest
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val nic: String) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    var registerState by mutableStateOf<RegisterState>(RegisterState.Idle)
        private set

    fun register(
        nic: String,
        fullName: String,
        email: String,
        phone: String,
        address: String,
        password: String
    ) {
        registerState = RegisterState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.registerProsumer(
                    RegisterRequest(
                        nic = nic,
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        address = address,
                        password = password
                    )
                )
                if (response.isSuccessful) {
                    registerState = RegisterState.Success(nic)
                } else {
                    val msg = try {
                        val errorStr = response.errorBody()?.string()
                        if (errorStr != null) org.json.JSONObject(errorStr).getString("message")
                        else "Registration failed."
                    } catch (e: Exception) {
                        "Registration failed. Please check your details."
                    }
                    registerState = RegisterState.Error(msg)
                }
            } catch (e: Exception) {
                registerState = RegisterState.Error("Network error: ${e.message}")
            }
        }
    }
}