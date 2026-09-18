package com.smartsolarmicrogrid.prosumer.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartsolarmicrogrid.prosumer.data.api.RetrofitClient
import com.smartsolarmicrogrid.prosumer.data.model.LoginRequest
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.smartsolarmicrogrid.prosumer.data.local.SessionDbHelper

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val role: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    var loginState by androidx.compose.runtime.mutableStateOf<LoginState>(LoginState.Idle)
        private set

    private val sessionDb = SessionDbHelper(application)

    fun login(username: String, password: String) {
        loginState = LoginState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionDb.saveSession(
                        nic = body.userId,
                        fullName = body.fullName,
                        role = body.role,
                        accountStatus = body.accountStatus
                    )
                    loginState = LoginState.Success(body.role)
                } else {
                    loginState = LoginState.Error("Invalid username or password")
                }
            } catch (e: Exception) {
                loginState = LoginState.Error("Network error: ${e.message}")
            }
        }
    }
}