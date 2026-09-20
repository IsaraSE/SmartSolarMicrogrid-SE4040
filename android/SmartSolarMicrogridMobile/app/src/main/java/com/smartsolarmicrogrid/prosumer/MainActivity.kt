package com.smartsolarmicrogrid.prosumer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.smartsolarmicrogrid.prosumer.data.api.RetrofitClient
import com.smartsolarmicrogrid.prosumer.data.local.SessionDbHelper
import com.smartsolarmicrogrid.prosumer.ui.navigation.NavGraph
import com.smartsolarmicrogrid.prosumer.ui.theme.SmartSolarMicrogridMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore a previously stored JWT so authenticated requests work after an app restart.
        RetrofitClient.authToken = SessionDbHelper(this).getToken()

        enableEdgeToEdge()
        setContent {
            SmartSolarMicrogridMobileTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavGraph()
                    }
                }
            }
        }
    }
}