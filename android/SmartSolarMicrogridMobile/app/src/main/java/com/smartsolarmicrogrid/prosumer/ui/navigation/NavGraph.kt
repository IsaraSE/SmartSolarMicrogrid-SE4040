package com.smartsolarmicrogrid.prosumer.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartsolarmicrogrid.prosumer.ui.auth.LoginScreen
import com.smartsolarmicrogrid.prosumer.ui.auth.RegisterScreen
import com.smartsolarmicrogrid.prosumer.ui.home.HomeScreen
import com.smartsolarmicrogrid.prosumer.ui.profile.EditProfileScreen
import com.smartsolarmicrogrid.prosumer.ui.profile.ProfileScreen


sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                onNavigateBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToStations = { /* TODO: Step 7 */ },
                onNavigateToBookings = { /* TODO: Step 9 */ },
                onNavigateToDashboard = { /* TODO: Step 9 */ }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToEdit = { navController.navigate(Screen.EditProfile.route) },
                onDeactivated = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}