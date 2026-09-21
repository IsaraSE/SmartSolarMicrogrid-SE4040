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
    data class Success(val nic: String, val role: String, val accountStatus: String) : LoginState()
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
                val response = RetrofitClient.apiService.login(LoginRequest(email = username, password = password))
                val user = response.body()?.data
                if (response.isSuccessful && user != null) {
                    // Make the JWT available to the interceptor for authenticated requests,
                    // and persist it (with the rest of the session) in SQLite.
                    RetrofitClient.authToken = user.token
                    sessionDb.saveSession(
                        nic = user.userId,
                        fullName = user.fullName,
                        role = user.role,
                        accountStatus = user.accountStatus,
                        token = user.token
                    )
                    loginState = LoginState.Success(user.userId, user.role, user.accountStatus)
                } else {
                    val msg = response.errorBody()?.string()?.let { 
                        org.json.JSONObject(it).optString("message", "Invalid username or password") 
                    } ?: "Invalid username or password"
                    loginState = LoginState.Error(msg)
                }
            } catch (e: Exception) {
                loginState = LoginState.Error("Network error: ${e.message}")
            }
        }
    }
}