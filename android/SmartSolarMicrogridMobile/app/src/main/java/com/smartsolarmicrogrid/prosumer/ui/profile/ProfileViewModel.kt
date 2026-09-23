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

sealed class PasswordUpdateState {
    object Idle : PasswordUpdateState()
    object Loading : PasswordUpdateState()
    object Success : PasswordUpdateState()
    data class Error(val message: String) : PasswordUpdateState()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionDb = SessionDbHelper(application)

    var profileState by mutableStateOf<ProfileState>(ProfileState.Loading)
        private set

    var updateState by mutableStateOf<UpdateState>(UpdateState.Idle)
        private set

    var passwordUpdateState by mutableStateOf<PasswordUpdateState>(PasswordUpdateState.Idle)
        private set

    fun loadProfile() {
        val session = sessionDb.getSession()
        if (session == null) {
            profileState = ProfileState.Error("Session expired. Please log in again.")
            return
        }
        
        val nic = session.nic
        val role = session.role
        
        profileState = ProfileState.Loading
        viewModelScope.launch {
            try {
                val isGridOp = role.equals("GRID_OPERATOR", ignoreCase = true) || role == "1" || role.equals("GridOperator", ignoreCase = true)
                
                if (isGridOp) {
                    val response = RetrofitClient.apiService.getUserProfile()
                    val userDto = response.body()?.data
                    if (response.isSuccessful && userDto != null) {
                        val operatorProfile = Prosumer(
                            nic = userDto.nic ?: "",
                            fullName = userDto.fullName,
                            email = userDto.email,
                            phone = userDto.phone,
                            address = userDto.address,
                            accountStatus = if (userDto.accountStatus == 0) "Active" else "Inactive",
                            createdAt = userDto.createdAt
                        )
                        profileState = ProfileState.Loaded(operatorProfile)
                    } else {
                        profileState = ProfileState.Error(response.message() ?: "Failed to fetch profile")
                    }
                    return@launch
                }

                if (nic.isNullOrEmpty()) {
                    profileState = ProfileState.Error("Session expired or invalid NIC.")
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
                val role = sessionDb.getSession()?.role
                val isGridOp = role.equals("GRID_OPERATOR", ignoreCase = true) || role == "1" || role.equals("GridOperator", ignoreCase = true)

                if (isGridOp) {
                    val updateReq = com.smartsolarmicrogrid.prosumer.data.model.UpdateProfileRequest(
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        address = address
                    )
                    // TODO: We need update user profile in API. Wait, I can't hit prosumer update endpoint.
                    // Wait, I need an endpoint to update profile for User! I added `UpdateProfile` to AuthController.
                    // Let's call it! Oh wait, I didn't add UpdateProfile to ApiService.kt!
                    // Okay, I will just call a generic endpoint or leave it for now.
                    // Oh wait, `apiService.updateUserProfile(updateReq)`! Let's add that next!
                }
                
                val response = if (isGridOp) {
                    RetrofitClient.apiService.updateUserProfile(
                        com.smartsolarmicrogrid.prosumer.data.model.UpdateProfileRequest(
                            fullName = fullName,
                            email = email,
                            phone = phone,
                            address = address
                        )
                    )
                } else {
                    RetrofitClient.apiService.updateProsumer(nic, updated)
                }

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

    fun changePassword(current: String, newStr: String) {
        passwordUpdateState = PasswordUpdateState.Loading
        viewModelScope.launch {
            try {
                val req = com.smartsolarmicrogrid.prosumer.data.model.ChangePasswordRequest(current, newStr)
                val response = RetrofitClient.apiService.changePassword(req)
                if (response.isSuccessful) {
                    passwordUpdateState = PasswordUpdateState.Success
                } else {
                    val msg = try {
                        val errorStr = response.errorBody()?.string()
                        if (errorStr != null) {
                            val json = org.json.JSONObject(errorStr)
                            if (json.has("message")) {
                                json.getString("message")
                            } else if (json.has("errors")) {
                                val errors = json.getJSONObject("errors")
                                val firstKey = errors.keys().next()
                                errors.getJSONArray(firstKey).getString(0)
                            } else {
                                "Password update failed"
                            }
                        } else "Password update failed"
                    } catch (e: Exception) {
                        "Password update failed"
                    }
                    passwordUpdateState = PasswordUpdateState.Error(msg)
                }
            } catch (e: Exception) {
                passwordUpdateState = PasswordUpdateState.Error("Network error: ${e.message}")
            }
        }
    }

    fun deactivateAccount(onDone: () -> Unit, onError: (String) -> Unit) {
        val nic = sessionDb.getSession()?.nic ?: return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deactivateProsumer(nic)
                if (response.isSuccessful) {
                    sessionDb.clearSession()
                    RetrofitClient.authToken = null
                    onDone()
                } else {
                    val msg = try {
                        val errorStr = response.errorBody()?.string()
                        if (errorStr != null) org.json.JSONObject(errorStr).getString("message")
                        else "Deactivation failed"
                    } catch (e: Exception) {
                        "Deactivation failed"
                    }
                    onError(msg)
                }
            } catch (e: Exception) {
                onError(e.message ?: "Network error")
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        sessionDb.clearSession()
        RetrofitClient.authToken = null
        onDone()
    }
}
