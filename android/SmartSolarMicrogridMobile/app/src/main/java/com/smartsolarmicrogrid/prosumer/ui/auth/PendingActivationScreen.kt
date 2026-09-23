package com.smartsolarmicrogrid.prosumer.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsolarmicrogrid.prosumer.data.api.RetrofitClient
import kotlinx.coroutines.delay

private val SolarGreen = Color(0xFF2E7D32)
private val PendingAmber = Color(0xFFFFA000)

@Composable
fun PendingActivationScreen(
    nic: String,
    onBackToLogin: () -> Unit
) {
    var isApproved by remember { mutableStateOf(false) }

    LaunchedEffect(nic) {
        if (nic.isNotEmpty()) {
            while (!isApproved) {
                try {
                    val response = RetrofitClient.apiService.getProsumer(nic)
                    val user = response.body()?.data
                    if (response.isSuccessful && user != null) {
                        if (user.accountStatus == "ACTIVE") {
                            isApproved = true
                            break
                        }
                    }
                } catch (e: Exception) {
                    // Ignore errors during polling
                }
                delay(5000) // Poll every 5 seconds
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(60.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isApproved) Icons.Filled.CheckCircle else Icons.Filled.Email,
                    contentDescription = null,
                    tint = SolarGreen,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (isApproved) "Account Activated!" else "Registration Submitted",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1B)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isApproved) "Your account has been successfully approved by the Backoffice. You can now login." else "Your account has been created successfully and is awaiting activation by the Backoffice team.",
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Status Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isApproved) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isApproved) Icons.Filled.CheckCircle else Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = if (isApproved) SolarGreen else PendingAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Account Status", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = if (isApproved) "ACTIVE" else "PENDING", 
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = if (isApproved) SolarGreen else PendingAmber
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isApproved) {
                Text(
                    text = "You will be notified once your account is activated. Please try logging in later.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onBackToLogin,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (isApproved) "Login Now" else "Back to Login", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
