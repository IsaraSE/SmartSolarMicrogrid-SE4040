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
                    profileState = ProfileState.Loaded(getMockProfile()) // TODO: remove before submission
                    return@launch
                }
                val response = RetrofitClient.apiService.getProsumer(nic)
                val prosumer = response.body()?.data
                if (response.isSuccessful && prosumer != null) {
                    profileState = ProfileState.Loaded(prosumer)
                } else {
                    profileState = ProfileState.Loaded(getMockProfile()) // TODO: remove before submission
                }
            } catch (e: Exception) {
                profileState = ProfileState.Loaded(getMockProfile()) // TODO: remove before submission
            }
        }
    }

    // TODO: remove this function before final submission — for UI preview only, no real backend yet
    private fun getMockProfile(): Prosumer {
        return Prosumer(
            nic = "200012345678",
            fullName = "Juthmini Perera",
            email = "juthmini@example.com",
            phone = "0771234567",
            address = "45 Galle Road, Colombo 03",
            accountStatus = "ACTIVE",
            createdAt = "2026-01-15"
        )
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
                    updateState = UpdateState.Error("Update failed")
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
}