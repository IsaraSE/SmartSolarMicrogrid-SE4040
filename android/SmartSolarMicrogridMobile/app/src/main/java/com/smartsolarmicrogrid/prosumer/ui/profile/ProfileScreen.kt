package com.smartsolarmicrogrid.prosumer.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val SolarGreenDark = Color(0xFF1B5E20)
private val SolarGreen = Color(0xFF2E7D32)
private val SurfaceGray = Color(0xFFF9F9F9)
private val LightGreenBg = Color(0xFFE8F5E9)
private val DarkTextColor = Color(0xFF1A1A1A)

@Composable
fun ProfileScreen(
    onNavigateToEdit: () -> Unit,
    onDeactivated: () -> Unit,
    onBack: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    val state = profileViewModel.profileState
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out of your account?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        profileViewModel.logout(onDone = onDeactivated)
                    }
                ) {
                    Text("Log Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = { Text("Deactivate Account") },
            text = { Text("Are you sure you want to deactivate your account? Once deactivated, you will not be able to log in and your account can only be reactivated by a Backoffice officer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeactivateDialog = false
                        profileViewModel.deactivateAccount(
                            onDone = onDeactivated,
                            onError = { err -> errorMessage = err }
                        )
                    }
                ) {
                    Text("Deactivate", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Cannot Deactivate") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SolarGreen)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Top Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Filled.WbSunny,
                        contentDescription = "Sun",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("HelioGrid", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Prosumer Portal", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }

            // Main White Content Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
            ) {
                when (state) {
                    is ProfileState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SolarGreen)
                        }
                    }
                    is ProfileState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is ProfileState.Loaded -> {
                        val prosumer = state.prosumer
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp)
                        ) {
                            Text("Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("Manage your account information", fontSize = 14.sp, color = Color.Gray)
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // User Summary Card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(LightGreenBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = SolarGreen,
                                        modifier = Modifier.size(50.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column {
                                    Text(prosumer.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SolarGreenDark)
                                    Text("Prosumer Member", fontSize = 14.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    // Status Chip
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(LightGreenBg)
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SolarGreen))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Active", fontSize = 12.sp, color = SolarGreenDark, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Contact Details Card
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                                    .background(Color.White)
                            ) {
                                DetailRow(icon = Icons.Filled.Badge, label = "NIC Number", value = prosumer.nic)
                                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(start = 64.dp))
                                DetailRow(icon = Icons.Filled.Email, label = "Email", value = prosumer.email)
                                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(start = 64.dp))
                                DetailRow(icon = Icons.Filled.Phone, label = "Phone", value = prosumer.phone)
                                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(start = 64.dp))
                                DetailRow(icon = Icons.Filled.LocationOn, label = "Address", value = prosumer.address)
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text("Account Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Account Details Card
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                                    .background(Color.White)
                            ) {
                                val createdAt = prosumer.createdAt ?: "N/A"
                                val formattedDate = try {
                                    if (createdAt != "N/A") {
                                        LocalDate.parse(createdAt.substringBefore("T")).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                                    } else "N/A"
                                } catch (e: Exception) {
                                    createdAt.substringBefore("T")
                                }
                                
                                DetailRow(icon = Icons.Filled.CalendarToday, label = "Member Since", value = formattedDate, hasChevron = false)
                                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(start = 64.dp))
                                
                                // Status Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(LightGreenBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = SolarGreen, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text("Account Status", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(LightGreenBg)
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Active", fontSize = 14.sp, color = SolarGreenDark, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Button(
                                onClick = onNavigateToEdit,
                                colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(54.dp)
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit Profile", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedButton(
                                onClick = { showDeactivateDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(54.dp)
                            ) {
                                Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Request Account Deactivation", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedButton(
                                onClick = { showLogoutDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SolarGreen),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SolarGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(54.dp)
                            ) {
                                Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    hasChevron: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(LightGreenBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = SolarGreen, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 15.sp, color = DarkTextColor, fontWeight = FontWeight.Medium)
        }
        if (hasChevron) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        }
    }
}