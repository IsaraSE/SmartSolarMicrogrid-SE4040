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
fun ProfileScreen(
    onNavigateToEdit: () -> Unit,
    onDeactivated: () -> Unit,
    onBack: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    var showDeactivateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    val state = profileViewModel.profileState

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("My Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (state) {
            is ProfileState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SolarGreen)
                }
            }
            is ProfileState.Error -> {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            is ProfileState.Loaded -> {
                val prosumer = state.prosumer
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        ProfileRow(icon = Icons.Filled.Badge, label = "NIC", value = prosumer.nic)
                        ProfileRow(icon = Icons.Filled.Person, label = "Full Name", value = prosumer.fullName)
                        ProfileRow(icon = Icons.Filled.Email, label = "Email", value = prosumer.email)
                        ProfileRow(icon = Icons.Filled.Phone, label = "Phone", value = prosumer.phone)
                        ProfileRow(icon = Icons.Filled.Home, label = "Address", value = prosumer.address)
                        ProfileRow(icon = Icons.Filled.Info, label = "Status", value = prosumer.accountStatus)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onNavigateToEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Edit Profile")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showDeactivateDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Request Deactivation")
                }
            }
        }
    }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = { Text("Deactivate Account") },
            text = { Text("Are you sure you want to request account deactivation? You will be logged out.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeactivateDialog = false
                    profileViewModel.deactivateAccount(onDone = onDeactivated)
                }) {
                    Text("Confirm", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = SolarGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}