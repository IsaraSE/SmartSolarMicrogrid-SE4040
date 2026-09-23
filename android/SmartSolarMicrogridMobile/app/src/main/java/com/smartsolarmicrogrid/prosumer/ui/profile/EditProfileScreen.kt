package com.smartsolarmicrogrid.prosumer.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
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
    }
}