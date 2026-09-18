package com.smartsolarmicrogrid.prosumer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartsolarmicrogrid.prosumer.ui.auth.LoginScreen
import com.smartsolarmicrogrid.prosumer.ui.auth.RegisterScreen
import com.smartsolarmicrogrid.prosumer.ui.home.HomeScreen
import com.smartsolarmicrogrid.prosumer.ui.profile.EditProfileScreen
import com.smartsolarmicrogrid.prosumer.ui.profile.ProfileScreen
import com.smartsolarmicrogrid.prosumer.ui.station.SlotListScreen
import com.smartsolarmicrogrid.prosumer.ui.station.StationListScreen
import com.smartsolarmicrogrid.prosumer.ui.station.StationViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Stations : Screen("stations")
    object Slots : Screen("slots")
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
                onNavigateToStations = { navController.navigate(Screen.Stations.route) },
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

        composable(Screen.Stations.route) {
            val stationViewModel: StationViewModel = viewModel()
            StationListScreen(
                onStationSelected = { station ->
                    stationViewModel.selectStationAndLoadSlots(station)
                    navController.navigate(Screen.Slots.route)
                },
                onBack = { navController.popBackStack() },
                stationViewModel = stationViewModel
            )
        }

        composable(Screen.Slots.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Stations.route)
            }
            val stationViewModel: StationViewModel = viewModel(parentEntry)
            SlotListScreen(
                onSlotSelected = { slot ->
                    // TODO: Step 8 — navigate to Create Booking with this slot
                },
                onBack = { navController.popBackStack() },
                stationViewModel = stationViewModel
            )
        }
    }
}