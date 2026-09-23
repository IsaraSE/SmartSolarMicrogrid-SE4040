package com.smartsolarmicrogrid.prosumer.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val SolarGreen = Color(0xFF2E7D32)

@Composable
fun EditProfileScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val state = profileViewModel.profileState
    val updateState = profileViewModel.updateState

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }
    
    var showPasswordForm by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val passwordUpdateState = profileViewModel.passwordUpdateState

    // Load profile when the screen starts
    LaunchedEffect(Unit) {
        if (state !is ProfileState.Loaded) {
            profileViewModel.loadProfile()
        }
    }

    // Pre-fill fields once profile data is loaded
    if (!initialized && state is ProfileState.Loaded) {
        fullName = state.prosumer.fullName
        email = state.prosumer.email
        phone = state.prosumer.phone
        address = state.prosumer.address
        initialized = true
    }

    LaunchedEffect(updateState) {
        if (updateState is UpdateState.Success) {
            onSaved()
        }
    }

    LaunchedEffect(passwordUpdateState) {
        if (passwordUpdateState is PasswordUpdateState.Success) {
            currentPassword = ""
            newPassword = ""
            confirmNewPassword = ""
            passwordVisible = false
        }
    }

    // Add scroll capability so the new form is accessible
    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Edit Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = (state as? ProfileState.Loaded)?.prosumer?.nic ?: "",
            onValueChange = { },
            label = { Text("NIC Number") },
            leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null, tint = Color.Gray) },
            singleLine = true,
            readOnly = true,
            enabled = false, // Unclickable and uneditable as it acts as unique ID
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = SolarGreen) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = SolarGreen) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = SolarGreen) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            leadingIcon = { Icon(Icons.Filled.Home, contentDescription = null, tint = SolarGreen) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (updateState is UpdateState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(updateState.message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { profileViewModel.updateProfile(fullName, email, phone, address) },
            enabled = updateState !is UpdateState.Loading,
            colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (updateState is UpdateState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color.LightGray)
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).clickable { showPasswordForm = !showPasswordForm },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Change Password", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Icon(
                imageVector = if (showPasswordForm) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null
            )
        }
        
        if (showPasswordForm) {
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = SolarGreen) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = SolarGreen) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmNewPassword,
                onValueChange = { confirmNewPassword = it },
                label = { Text("Confirm New Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = SolarGreen) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            val passwordMismatch = newPassword.isNotEmpty() && confirmNewPassword.isNotEmpty() && newPassword != confirmNewPassword
            if (passwordMismatch) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("New passwords do not match", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            
            if (passwordUpdateState is PasswordUpdateState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(passwordUpdateState.message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            if (passwordUpdateState is PasswordUpdateState.Success) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Password changed successfully", color = SolarGreen, fontSize = 13.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { 
                    if (newPassword == confirmNewPassword) {
                        profileViewModel.changePassword(currentPassword, newPassword)
                    }
                },
                enabled = passwordUpdateState !is PasswordUpdateState.Loading && 
                          currentPassword.isNotEmpty() && 
                          newPassword.isNotEmpty() && 
                          newPassword == confirmNewPassword,
                colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (passwordUpdateState is PasswordUpdateState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Update Password", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}