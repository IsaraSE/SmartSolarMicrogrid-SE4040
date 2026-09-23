package com.smartsolarmicrogrid.prosumer.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartsolarmicrogrid.prosumer.R
import com.smartsolarmicrogrid.prosumer.ui.operator.SolarGreen

@Composable
fun RoleSelectionScreen(
    onSelectRole: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEF6F0)), // Soft green background to match image
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Reusing launcher icon as placeholder if custom icon not found
                contentDescription = "App Logo",
                modifier = Modifier.size(140.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Smart Solar Microgrid",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SolarGreen
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Clean Energy\nStronger Communities",
                fontSize = 16.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Button(
                onClick = { onSelectRole("PROSUMER") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continue as Prosumer", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { onSelectRole("GRID_OPERATOR") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = SolarGreen
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, SolarGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continue as Grid Operator", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
