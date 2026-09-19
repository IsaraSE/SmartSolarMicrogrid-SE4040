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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun QrScannerScreen(
    onBack: () -> Unit,
    operatorViewModel: OperatorViewModel = viewModel()
) {
    val context = LocalContext.current

    // Launches the ZXing scanner and hands the scanned text to the API for verification.
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { operatorViewModel.verifyQrReference(it) }
    }

    fun launchScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Point the camera at the prosumer's QR code")
            setBeepEnabled(false)
            setOrientationLocked(false)
        }
        scanLauncher.launch(options)
    }

    // Camera permission must be granted before the scanner can open.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchScanner()
    }

    fun startScan() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) launchScanner() else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

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
        ) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("QR Verification", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Scan and verify against the server",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }

            when (val state = operatorViewModel.verifyState) {

                is VerifyState.Idle -> {
                    ScanPrompt(onScan = { startScan() })
                }

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
                        onScanAgain = {
                            operatorViewModel.resetScan()
                            startScan()
                        }
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
                            onScanAgain = {
                                operatorViewModel.resetScan()
                                startScan()
                            }
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
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.VerifiedUser,
                                        contentDescription = null,
                                        tint = SolarGreen
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Reservation verified",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SolarGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = SurfaceGray)
                                Spacer(modifier = Modifier.height(12.dp))

                                result.prosumerName?.let { InfoRow("Prosumer", it) }
                                result.prosumerNic?.let { InfoRow("NIC", it) }
                                result.stationId?.let { InfoRow("Station", it) }
                                result.slotId?.let { InfoRow("Slot", it) }
                                result.bookingDate?.let { InfoRow("Date", it) }
                                result.startTime?.let { InfoRow("Start time", it) }
                                result.status?.let { InfoRow("Status", it) }
                                result.reservationId?.let { InfoRow("Reservation", it) }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        CompleteSection(
                            reservationId = result.reservationId,
                            completeState = operatorViewModel.completeState,
                            onComplete = { id -> operatorViewModel.completeTransfer(id) },
                            onScanNext = {
                                operatorViewModel.resetScan()
                                startScan()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/** First state of the screen: invite the operator to scan. */
@Composable
private fun ScanPrompt(onScan: () -> Unit) {
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
            Icon(
                Icons.Filled.QrCodeScanner,
                contentDescription = null,
                tint = SolarGreen,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Ready to scan", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Ask the prosumer to show their transaction QR code, then scan it to verify the reservation against the server.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onScan,
                colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Scanner")
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

/** Complete-the-transfer action shown after a successful verification. */
@Composable
private fun CompleteSection(
    reservationId: String?,
    completeState: CompleteState,
    onComplete: (String) -> Unit,
    onScanNext: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        when (completeState) {
            is CompleteState.Success -> {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SolarGreen.copy(alpha = 0.10f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SolarGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Energy transfer completed",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SolarGreen
                            )
                            Text(
                                "The booking now shows as COMPLETED in the prosumer's history.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onScanNext,
                    colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Scan next prosumer")
                }
            }

            is CompleteState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SolarGreen)
                }
            }

            else -> {
                if (completeState is CompleteState.Error) {
                    Text(
                        completeState.message,
                        fontSize = 12.sp,
                        color = DangerRed
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Button(
                    onClick = { reservationId?.let { onComplete(it) } },
                    enabled = reservationId != null,
                    colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Filled.BatteryChargingFull, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Energy Transfer")
                }
            }
        }
    }
}

/** One labelled line in the verified reservation card. */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}