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
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import android.os.Handler
import android.os.Looper

import com.smartsolarmicrogrid.prosumer.ui.navigation.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore a previously stored session so authenticated requests work after an app restart.
        val sessionDb = SessionDbHelper(this)
        val session = sessionDb.getSession()
        RetrofitClient.authToken = session?.token

        val initialRoute = if (session != null && session.token != null) {
            when {
                session.accountStatus == "PENDING" -> Screen.PendingActivation.createRoute(session.nic)
                session.role == "GRID_OPERATOR" -> Screen.OperatorHome.route
                else -> Screen.Dashboard.route
            }
        } else {
            Screen.Login.route
        }

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            
            LaunchedEffect(Unit) {
                RetrofitClient.onSessionExpired = {
                    sessionDb.clearSession()
                    RetrofitClient.authToken = null
                    Handler(Looper.getMainLooper()).post {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }

            SmartSolarMicrogridMobileTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavGraph(navController = navController, startDestination = initialRoute)
                    }
                }
            }
        }
    }
}