/*
 * QrScannerScreen.kt
 * Smart Solar Microgrid Trading System - Prosumer Mobile Application
 *
 * Grid operator QR flow. The camera scan only reads a reference; the reference
 * is then sent to the C# Web API, and the reservation details displayed come
 * from the server response. The operator completes the transfer from here.
 */
package com.smartsolarmicrogrid.prosumer.ui.operator

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    onBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    operatorViewModel: OperatorViewModel = viewModel()
) {
    val context = LocalContext.current
    var hasPermission by remember { 
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) 
    }
    
    var flashEnabled by remember { mutableStateOf(false) }
    var showManualEntry by remember { mutableStateOf(false) }
    var manualId by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { flashEnabled = !flashEnabled }) {
                        Icon(if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff, contentDescription = "Toggle Flash")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0C8A44),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background
            val isIdle = operatorViewModel.verifyState is VerifyState.Idle
            
            if (isIdle && hasPermission) {
                // Inline Camera View
                val lifecycleOwner = LocalLifecycleOwner.current
                var barcodeView by remember { mutableStateOf<CompoundBarcodeView?>(null) }
                
                AndroidView(
                    factory = { ctx ->
                        CompoundBarcodeView(ctx).apply {
                            this.setStatusText("") // Hide default prompt
                            this.decodeSingle(object : BarcodeCallback {
                                override fun barcodeResult(result: BarcodeResult?) {
                                    result?.text?.let { operatorViewModel.verifyQrReference(it) }
                                }
                                override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {}
                            })
                            barcodeView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        if (flashEnabled) view.setTorchOn() else view.setTorchOff()
                    }
                )
                
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> barcodeView?.resume()
                            Lifecycle.Event.ON_PAUSE -> barcodeView?.pause()
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }
                
                // Overlay Content for Idle state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Align the QR code within\nthe frame",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showManualEntry = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                            .height(54.dp)
                    ) {
                        Text("Enter Manually", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            } else if (isIdle && !hasPermission) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                    Text("Camera permission is required to scan QR codes.", color = Color.White)
                }
            } else {
                // Background for result/loading states
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(SolarGreenDark, SolarGreen, SurfaceGray),
                                startY = 0f,
                                endY = 420f
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 32.dp)
                    ) {
                        when (val state = operatorViewModel.verifyState) {
                            is VerifyState.Loading -> {
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(36.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(color = SolarGreen)
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text("Checking with the server…", fontSize = 13.sp, color = Color.Gray)
                                    }
                                }
                            }
            
                            is VerifyState.Error -> {
                                ResultCard(
                                    icon = Icons.Filled.ErrorOutline,
                                    tint = DangerRed,
                                    title = "Verification failed",
                                    message = state.message,
                                    onScanAgain = { operatorViewModel.resetScan() }
                                )
                            }
            
                            is VerifyState.Result -> {
                                val result = state.result
                                if (!result.valid) {
                                    ResultCard(
                                        icon = Icons.Filled.Cancel,
                                        tint = DangerRed,
                                        title = "Invalid QR code",
                                        message = result.message ?: "This code does not match any reservation.",
                                        onScanAgain = { operatorViewModel.resetScan() }
                                    )
                                } else {
                                    Card(
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE8F5E9)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = SolarGreen,
                                                    modifier = Modifier.size(48.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                "Valid Reservation",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SolarGreen
                                            )

                                            Spacer(modifier = Modifier.height(24.dp))
                                            
                                            val currentStatus = if (operatorViewModel.completeState is CompleteState.Success) "Completed" else (result.status ?: "Approved")

                                            InfoRow("Reservation ID", result.reservationNumber ?: result.reservationId ?: "Unknown")
                                            HorizontalDivider(color = SurfaceGray, modifier = Modifier.padding(vertical = 12.dp))
                                            
                                            InfoRow("Prosumer NIC", result.prosumerNic ?: result.prosumerName ?: "Unknown")
                                            HorizontalDivider(color = SurfaceGray, modifier = Modifier.padding(vertical = 12.dp))
                                            
                                            InfoRow("Station", result.stationName ?: result.stationId ?: "Unknown")
                                            HorizontalDivider(color = SurfaceGray, modifier = Modifier.padding(vertical = 12.dp))
                                            
                                            InfoRow("Date", result.bookingDate ?: "")
                                            HorizontalDivider(color = SurfaceGray, modifier = Modifier.padding(vertical = 12.dp))
                                            
                                            val timeString = buildString {
                                                if (result.startTime != null) append(result.startTime)
                                                if (result.endTime != null) append(" - ${result.endTime}")
                                            }
                                            InfoRow("Time", timeString.trim())
                                            HorizontalDivider(color = SurfaceGray, modifier = Modifier.padding(vertical = 12.dp))
                                            
                                            InfoRow("Status", currentStatus, valueColor = SolarGreen)

                                            Spacer(modifier = Modifier.height(32.dp))

                                            if (operatorViewModel.completeState is CompleteState.Success) {
                                                AlertDialog(
                                                    onDismissRequest = { /* Do nothing, force user to click button */ },
                                                    title = { Text("Session Completed", color = SolarGreen) },
                                                    text = { Text("The reservation session has been successfully completed.") },
                                                    confirmButton = {
                                                        TextButton(onClick = onNavigateToDashboard) {
                                                            Text("Go to Dashboard", color = SolarGreen, fontWeight = FontWeight.Bold)
                                                        }
                                                    },
                                                    containerColor = Color.White
                                                )
                                            } else {
                                                Button(
                                                    onClick = { result.reservationId?.let { operatorViewModel.completeTransfer(it) } },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(54.dp)
                                                ) {
                                                    if (operatorViewModel.completeState is CompleteState.Loading) {
                                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                                    } else {
                                                        Text("Complete Session", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                                    }
                                                }
                                                if (operatorViewModel.completeState is CompleteState.Error) {
                                                    Text(
                                                        text = (operatorViewModel.completeState as CompleteState.Error).message,
                                                        color = DangerRed,
                                                        fontSize = 12.sp,
                                                        modifier = Modifier.padding(top = 8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            // Manual Entry Dialog
            if (showManualEntry) {
                AlertDialog(
                    onDismissRequest = { showManualEntry = false },
                    title = { Text("Enter Reference") },
                    text = {
                        OutlinedTextField(
                            value = manualId,
                            onValueChange = { manualId = it },
                            placeholder = { Text("Enter reservation reference") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showManualEntry = false
                            if (manualId.isNotBlank()) {
                                operatorViewModel.verifyQrReference(manualId)
                                manualId = ""
                            }
                        }) {
                            Text("Submit", color = Color(0xFF0C8A44), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showManualEntry = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}

/** Shown for an invalid code or a failed verification. */
@Composable
private fun ResultCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    title: String,
    message: String,
    onScanAgain: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(52.dp))
            Spacer(modifier = Modifier.height(14.dp))
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(message, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = onScanAgain,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Scan again")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = Color(0xFF0F172A)) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
