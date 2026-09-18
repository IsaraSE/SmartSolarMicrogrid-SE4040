package com.smartsolarmicrogrid.prosumer.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBackToLogin: () -> Unit,
    registerViewModel: RegisterViewModel = viewModel()
) {
    var nic by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val state = registerViewModel.registerState

    LaunchedEffect(state) {
        if (state is RegisterState.Success) {
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFFF5F5F5)),
                    startY = 0f,
                    endY = 700f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Create Prosumer Account",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Join the Smart Solar Microgrid network",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    RegisterField(
                        value = nic,
                        onValueChange = { nic = it },
                        label = "NIC",
                        icon = Icons.Filled.Badge
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    RegisterField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name",
                        icon = Icons.Filled.Person
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    RegisterField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        icon = Icons.Filled.Email,
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    RegisterField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone",
                        icon = Icons.Filled.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    RegisterField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Address",
                        icon = Icons.Filled.Home
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF2E7D32))
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E7D32),
                            focusedLabelColor = Color(0xFF2E7D32)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state is RegisterState.Error) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            registerViewModel.register(nic, fullName, email, phone, address, password)
                        },
                        enabled = state !is RegisterState.Loading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (state is RegisterState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Register", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = onNavigateBackToLogin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Already have an account? ", color = Color.Gray, fontSize = 13.sp)
                        Text("Login", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RegisterField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = Color(0xFF2E7D32))
        },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2E7D32),
            focusedLabelColor = Color(0xFF2E7D32)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(onRegisterSuccess = {}, onNavigateBackToLogin = {})
}