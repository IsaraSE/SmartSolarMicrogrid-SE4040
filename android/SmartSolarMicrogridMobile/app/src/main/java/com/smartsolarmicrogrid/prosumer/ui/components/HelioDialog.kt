package com.smartsolarmicrogrid.prosumer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

private val GreenPrimary = Color(0xFF1B8A4A)
private val GreenDark = Color(0xFF145A32)
private val DangerRed = Color(0xFFEF4444)
private val AmberWarn = Color(0xFFFFB300)

enum class HelioDialogType { INFO, SUCCESS, WARNING, DANGER }

/**
 * Premium HelioGrid styled dialog used across the entire app.
 * Replaces the default Material AlertDialog with a polished, branded design.
 */
@Composable
fun HelioDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    type: HelioDialogType = HelioDialogType.INFO,
    icon: ImageVector? = null,
    confirmText: String = "Confirm",
    dismissText: String? = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    val accentColor = when (type) {
        HelioDialogType.INFO -> GreenPrimary
        HelioDialogType.SUCCESS -> Color(0xFF43A047)
        HelioDialogType.WARNING -> AmberWarn
        HelioDialogType.DANGER -> DangerRed
    }

    val iconBgColor = when (type) {
        HelioDialogType.INFO -> Color(0xFFE8F5E9)
        HelioDialogType.SUCCESS -> Color(0xFFE8F5E9)
        HelioDialogType.WARNING -> Color(0xFFFFF8E1)
        HelioDialogType.DANGER -> Color(0xFFFFEBEE)
    }

    val resolvedIcon = icon ?: when (type) {
        HelioDialogType.INFO -> Icons.Filled.Info
        HelioDialogType.SUCCESS -> Icons.Filled.CheckCircle
        HelioDialogType.WARNING -> Icons.Filled.Warning
        HelioDialogType.DANGER -> Icons.Filled.Warning
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        tonalElevation = 0.dp,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .padding(vertical = 24.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Icon badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        resolvedIcon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Message
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                // Optional custom content (e.g. text fields)
                if (content != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    content()
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Confirm button - gradient
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (type == HelioDialogType.DANGER)
                                        listOf(Color(0xFFE53935), Color(0xFFEF5350))
                                    else
                                        listOf(GreenDark, GreenPrimary)
                                ),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            confirmText,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Dismiss button - text style
                if (dismissText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { onDismiss?.invoke() ?: onDismissRequest() },
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text(
                            dismissText,
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {},  // handled inside text content
        dismissButton = null
    )
}
