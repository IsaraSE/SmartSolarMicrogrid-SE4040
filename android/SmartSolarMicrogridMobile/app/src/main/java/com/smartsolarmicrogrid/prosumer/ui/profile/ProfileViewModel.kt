package com.smartsolarmicrogrid.prosumer.ui.profile

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartsolarmicrogrid.prosumer.data.api.RetrofitClient
import com.smartsolarmicrogrid.prosumer.data.local.SessionDbHelper
import com.smartsolarmicrogrid.prosumer.data.model.Prosumer
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Loading : ProfileState()
    data class Loaded(val prosumer: Prosumer) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionDb = SessionDbHelper(application)

    var profileState by mutableStateOf<ProfileState>(ProfileState.Loading)
        private set

    var updateState by mutableStateOf<UpdateState>(UpdateState.Idle)
        private set

    fun loadProfile() {
        val nic = sessionDb.getSession()?.nic
        profileState = ProfileState.Loading
        viewModelScope.launch {
            try {
                if (nic == null) {
                    profileState = ProfileState.Error("Session expired. Please log in again.")
                    return@launch
                }
                val response = RetrofitClient.apiService.getProsumer(nic)
                val prosumer = response.body()?.data
                if (response.isSuccessful && prosumer != null) {
                    profileState = ProfileState.Loaded(prosumer)
                } else {
                    profileState = ProfileState.Error(response.message() ?: "Failed to fetch profile")
                }
            } catch (e: Exception) {
                profileState = ProfileState.Error(e.message ?: "Network error")
            }
        }
    }

    fun updateProfile(fullName: String, email: String, phone: String, address: String) {
        val nic = sessionDb.getSession()?.nic ?: return
        updateState = UpdateState.Loading
        viewModelScope.launch {
            try {
                val current = (profileState as? ProfileState.Loaded)?.prosumer ?: return@launch
                val updated = current.copy(
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    address = address
                )
                val response = RetrofitClient.apiService.updateProsumer(nic, updated)
                if (response.isSuccessful) {
                    updateState = UpdateState.Success
                    profileState = ProfileState.Loaded(updated)
                } else {
                    val msg = try {
                        val errorStr = response.errorBody()?.string()
                        if (errorStr != null) org.json.JSONObject(errorStr).getString("message")
                        else "Update failed"
                    } catch (e: Exception) {
                        "Update failed"
                    }
                    updateState = UpdateState.Error(msg)
                }
            } catch (e: Exception) {
                updateState = UpdateState.Error("Network error: ${e.message}")
            }
        }
    }

    fun deactivateAccount(onDone: () -> Unit) {
        val nic = sessionDb.getSession()?.nic ?: return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deactivateProsumer(nic)
                if (response.isSuccessful) {
                    sessionDb.clearSession()
                    RetrofitClient.authToken = null
                    onDone()
                }
            } catch (e: Exception) {
                // could add an error state for this too if needed
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        sessionDb.clearSession()
        RetrofitClient.authToken = null
        onDone()
    }
}
