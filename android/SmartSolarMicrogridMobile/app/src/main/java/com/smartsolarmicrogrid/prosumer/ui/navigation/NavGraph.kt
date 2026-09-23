/*
 * NavGraph.kt
 * Smart Solar Microgrid Trading System - Prosumer Mobile Application
 *
 * Single navigation graph for the Prosumer app. Shared ViewModels are scoped to
 * a parent back stack entry so the selected station, slot and reservation stay
 * available across the booking screens.
 */
package com.smartsolarmicrogrid.prosumer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext

import com.smartsolarmicrogrid.prosumer.ui.auth.LoginScreen
import com.smartsolarmicrogrid.prosumer.ui.auth.RoleSelectionScreen
import com.smartsolarmicrogrid.prosumer.ui.auth.RegisterScreen
import com.smartsolarmicrogrid.prosumer.ui.auth.PendingActivationScreen
import com.smartsolarmicrogrid.prosumer.ui.booking.BookingSummaryScreen
import com.smartsolarmicrogrid.prosumer.ui.booking.BookingViewModel
import com.smartsolarmicrogrid.prosumer.ui.booking.CancelBookingScreen
import com.smartsolarmicrogrid.prosumer.ui.booking.CreateBookingScreen
import com.smartsolarmicrogrid.prosumer.ui.booking.CreateBookingState
import com.smartsolarmicrogrid.prosumer.ui.booking.ModifyBookingScreen
import com.smartsolarmicrogrid.prosumer.ui.bookinglist.BookingDetailsScreen
import com.smartsolarmicrogrid.prosumer.ui.bookinglist.BookingListScreen
import com.smartsolarmicrogrid.prosumer.ui.bookinglist.BookingListViewModel
import com.smartsolarmicrogrid.prosumer.ui.dashboard.DashboardScreen
import com.smartsolarmicrogrid.prosumer.ui.operator.OperatorDashboardScreen
import com.smartsolarmicrogrid.prosumer.ui.operator.OperatorReservationsScreen
import com.smartsolarmicrogrid.prosumer.ui.operator.OperatorViewModel
import com.smartsolarmicrogrid.prosumer.ui.operator.QrScannerScreen
import com.smartsolarmicrogrid.prosumer.ui.operator.StationMapScreen
import com.smartsolarmicrogrid.prosumer.ui.qr.BookingQrScreen
import com.smartsolarmicrogrid.prosumer.ui.profile.EditProfileScreen
import com.smartsolarmicrogrid.prosumer.ui.profile.ProfileScreen
import com.smartsolarmicrogrid.prosumer.ui.station.SlotListScreen
import com.smartsolarmicrogrid.prosumer.ui.station.StationDetailsScreen
import com.smartsolarmicrogrid.prosumer.ui.station.StationListScreen
import com.smartsolarmicrogrid.prosumer.ui.station.StationViewModel

@Composable
inline fun <reified T : androidx.lifecycle.ViewModel> sharedActivityViewModel(): T {
    val activity = LocalContext.current as ComponentActivity
    return viewModel(activity)
}

/** Where the user reached Modify / Cancel from: the new-booking flow or the booking list. */
const val SOURCE_CREATE = "create"
const val SOURCE_LIST = "list"

sealed class Screen(val route: String) {
    object RoleSelection : Screen("role_selection")
    object Login : Screen("login/{role}") {
        fun createRoute(role: String) = "login/$role"
    }
    object Register : Screen("register")
    object PendingActivation : Screen("pending_activation/{nic}") {
        fun createRoute(nic: String) = "pending_activation/$nic"
    }
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Stations : Screen("stations")
    object StationDetails : Screen("station_details")
    object Slots : Screen("slots")
    object CreateBooking : Screen("create_booking")
    object BookingSummary : Screen("booking_summary")
    object BookingList : Screen("booking_list")
    object BookingDetails : Screen("booking_details")
    object Dashboard : Screen("dashboard")
    object BookingQr : Screen("booking_qr")

    // Grid operator mode
    object OperatorHome : Screen("operator_home")
    object OperatorReservations : Screen("operator_reservations")
    object OperatorScan : Screen("operator_scan")
    object OperatorStations : Screen("operator_stations") // Replacing OperatorMap
    object OperatorStationDetails : Screen("operator_station_details")
    object OperatorSlots : Screen("operator_slots")

    object ModifyBooking : Screen("modify_booking/{source}") {
        fun createRoute(source: String) = "modify_booking/$source"
    }

    object CancelBooking : Screen("cancel_booking/{source}") {
        fun createRoute(source: String) = "cancel_booking/$source"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.RoleSelection.route
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onSelectRole = { role ->
                    navController.navigate(Screen.Login.createRoute(role))
                }
            )
        }

        composable(Screen.Login.route) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "PROSUMER"
            LoginScreen(
                role = role,
                onLoginSuccess = { nic, role, status ->
                    if (status == "PENDING") {
                        navController.navigate(Screen.PendingActivation.createRoute(nic)) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        // The role decides which home screen the user lands on.
                        val home = if (role == "GRID_OPERATOR") {
                            Screen.OperatorHome.route
                        } else {
                            Screen.Dashboard.route
                        }
                        navController.navigate(home) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { nic -> 
                    navController.navigate(Screen.PendingActivation.createRoute(nic)) {
                        popUpTo(Screen.Login.route) { inclusive = false }
                    }
                },
                onNavigateBackToLogin = { navController.popBackStack() }
            )
        }
        
        composable(Screen.PendingActivation.route) { backStackEntry ->
            val nic = backStackEntry.arguments?.getString("nic") ?: ""
            PendingActivationScreen(
                nic = nic,
                onBackToLogin = {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true } // Clear entire backstack
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            WithBottomBar(navController, Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToEdit = { navController.navigate(Screen.EditProfile.route) },
                    onDeactivated = {
                        navController.navigate(Screen.RoleSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Stations.route) {
            val stationViewModel: StationViewModel = sharedActivityViewModel()
            WithBottomBar(navController, Screen.Stations.route) {
                StationListScreen(
                    onStationSelected = { station ->
                        stationViewModel.selectStationAndLoadSlots(station)
                        navController.navigate(Screen.StationDetails.route)
                    },
                    onBack = { navController.popBackStack() },
                    stationViewModel = stationViewModel
                )
            }
        }

        composable(Screen.StationDetails.route) {
            val stationViewModel: StationViewModel = sharedActivityViewModel()
            StationDetailsScreen(
                onNavigateToSlots = { navController.navigate(Screen.Slots.route) },
                onBack = { navController.popBackStack() },
                stationViewModel = stationViewModel
            )
        }

        composable(Screen.Slots.route) {
            val stationViewModel: StationViewModel = sharedActivityViewModel()
            SlotListScreen(
                onSlotSelected = { slot ->
                    stationViewModel.selectSlot(slot)
                    navController.navigate(Screen.CreateBooking.route)
                },
                onBack = { navController.popBackStack() },
                stationViewModel = stationViewModel
            )
        }

        composable(Screen.CreateBooking.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Stations.route)
            }
            val stationViewModel: StationViewModel = sharedActivityViewModel()
            val bookingViewModel: BookingViewModel = viewModel(parentEntry)

            val station = stationViewModel.selectedStation
            val slot = stationViewModel.selectedSlot

            if (station != null && slot != null) {
                CreateBookingScreen(
                    station = station,
                    slot = slot,
                    onBookingConfirmed = { 
                        navController.navigate(Screen.BookingSummary.route)
                    },
                    onBack = { navController.popBackStack() },
                    bookingViewModel = bookingViewModel
                )
            }
        }

        composable(Screen.BookingSummary.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Stations.route)
            }
            val stationViewModel: StationViewModel = sharedActivityViewModel()
            val bookingViewModel: BookingViewModel = viewModel(parentEntry)
            val state = bookingViewModel.createBookingState

            val station = stationViewModel.selectedStation
            val slot = stationViewModel.selectedSlot

            if (state is CreateBookingState.Success && station != null && slot != null) {
                BookingSummaryScreen(
                    reservation = state.reservation,
                    station = station,
                    slot = slot,
                    onDone = {
                        bookingViewModel.resetState()
                        navController.navigate(Screen.BookingList.route) {
                            popUpTo(Screen.Dashboard.route)
                        }
                    },
                    onBackToHome = {
                        bookingViewModel.resetState()
                        navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                    }
                )
            }
        }

        composable(Screen.Dashboard.route) {
            val bookingListViewModel: BookingListViewModel = sharedActivityViewModel()
            val stationViewModel: StationViewModel = sharedActivityViewModel()
            WithBottomBar(navController, Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToStations = {
                        stationViewModel.isMapView = false
                        navController.navigate(Screen.Stations.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToBookings = {
                        navController.navigate(Screen.BookingList.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToMap = {
                        stationViewModel.isMapView = true
                        navController.navigate(Screen.Stations.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onBookingSelected = { reservation ->
                        bookingListViewModel.selectReservation(reservation)
                        navController.navigate(Screen.BookingDetails.route)
                    }
                )
            }
        }

        // ---------- Grid operator mode ----------

        composable(Screen.OperatorHome.route) { backStackEntry ->
            val operatorViewModel: OperatorViewModel = viewModel(backStackEntry)
            WithOperatorBottomBar(navController, Screen.OperatorHome.route) {
                OperatorDashboardScreen(
                    onScanQr = { navController.navigate(Screen.OperatorScan.route) },
                    onViewReservations = { navController.navigate(Screen.OperatorReservations.route) },
                    onViewMap = { navController.navigate(Screen.OperatorStations.route) },
                    onLogout = {
                        navController.navigate(Screen.RoleSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    operatorViewModel = operatorViewModel
                )
            }
        }

        composable(Screen.OperatorReservations.route) { backStackEntry ->
            val operatorViewModel: OperatorViewModel = operatorViewModel(navController, backStackEntry)
            WithOperatorBottomBar(navController, Screen.OperatorReservations.route) {
                OperatorReservationsScreen(
                    onScanQr = { navController.navigate(Screen.OperatorScan.route) },
                    operatorViewModel = operatorViewModel
                )
            }
        }

        composable(Screen.OperatorScan.route) { backStackEntry ->
            val operatorViewModel: OperatorViewModel = operatorViewModel(navController, backStackEntry)
            WithOperatorBottomBar(navController, Screen.OperatorScan.route) {
                QrScannerScreen(
                    onBack = {
                        operatorViewModel.resetScan()
                        navController.popBackStack()
                    },
                    operatorViewModel = operatorViewModel
                )
            }
        }

        composable(Screen.OperatorStations.route) { backStackEntry ->
            val operatorViewModel: OperatorViewModel = operatorViewModel(navController, backStackEntry)
            val stationViewModel: StationViewModel = sharedActivityViewModel() // Gets the Activity-scoped StationViewModel
            WithOperatorBottomBar(navController, Screen.OperatorStations.route) {
                com.smartsolarmicrogrid.prosumer.ui.operator.OperatorStationsScreen(
                    onBack = { navController.popBackStack() },
                    onStationSelected = { navController.navigate(Screen.OperatorStationDetails.route) },
                    stationViewModel = stationViewModel
                )
            }
        }

        composable(Screen.OperatorStationDetails.route) {
            val stationViewModel: StationViewModel = sharedActivityViewModel()
            com.smartsolarmicrogrid.prosumer.ui.operator.OperatorStationDetailsScreen(
                onNavigateToSlots = { navController.navigate(Screen.OperatorSlots.route) },
                onBack = { navController.popBackStack() },
                stationViewModel = stationViewModel
            )
        }

        composable(Screen.OperatorSlots.route) {
            val stationViewModel: StationViewModel = sharedActivityViewModel()
            com.smartsolarmicrogrid.prosumer.ui.operator.OperatorSlotListScreen(
                onBack = { navController.popBackStack() },
                stationViewModel = stationViewModel
            )
        }

        // ---------- Booking views: Current / Pending / History + search ----------

        composable(Screen.BookingList.route) { backStackEntry ->
            val bookingListViewModel: BookingListViewModel = sharedActivityViewModel()
            WithBottomBar(navController, Screen.BookingList.route) {
                BookingListScreen(
                    onBookingSelected = { reservation ->
                        bookingListViewModel.selectReservation(reservation)
                        navController.navigate(Screen.BookingDetails.route)
                    },
                    onBack = { navController.popBackStack() },
                    bookingListViewModel = bookingListViewModel
                )
            }
        }

        composable(Screen.BookingDetails.route) { backStackEntry ->
            val bookingListViewModel: BookingListViewModel = sharedActivityViewModel()
            val bookingViewModel: BookingViewModel = sharedActivityViewModel()
            val reservation = bookingListViewModel.selectedReservation

            if (reservation != null) {
                BookingDetailsScreen(
                    reservation = reservation,
                    onModify = {
                        // Clear any result from a previous action so the screen opens fresh.
                        bookingViewModel.resetState()
                        navController.navigate(Screen.ModifyBooking.createRoute(SOURCE_LIST))
                    },
                    onCancel = {
                        bookingViewModel.resetState()
                        navController.navigate(Screen.CancelBooking.createRoute(SOURCE_LIST))
                    },
                    onShowQr = { navController.navigate(Screen.BookingQr.route) },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.BookingQr.route) { backStackEntry ->
            val bookingListViewModel: BookingListViewModel = sharedActivityViewModel()
            val reservation = bookingListViewModel.selectedReservation

            if (reservation != null) {
                BookingQrScreen(
                    reservation = reservation,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // ---------- Modify / Cancel, reachable from both flows ----------

        composable(Screen.ModifyBooking.route) { backStackEntry ->
            val source = backStackEntry.arguments?.getString("source") ?: SOURCE_CREATE

            if (source == SOURCE_LIST) {
                val bookingListViewModel: BookingListViewModel = sharedActivityViewModel()
                val bookingViewModel: BookingViewModel = sharedActivityViewModel()
                val reservation = bookingListViewModel.selectedReservation

                if (reservation != null) {
                    ModifyBookingScreen(
                        reservation = reservation,
                        onUpdated = {
                            bookingListViewModel.refresh()
                            navController.popBackStack(Screen.BookingList.route, inclusive = false)
                        },
                        onBack = { navController.popBackStack() },
                        bookingViewModel = bookingViewModel
                    )
                }
            } else {
                val createBookingEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.CreateBooking.route)
                }
                val bookingViewModel: BookingViewModel = viewModel(createBookingEntry)
                val state = bookingViewModel.createBookingState

                if (state is CreateBookingState.Success) {
                    ModifyBookingScreen(
                        reservation = state.reservation,
                        onUpdated = { navController.popBackStack() },
                        onBack = { navController.popBackStack() },
                        bookingViewModel = bookingViewModel
                    )
                }
            }
        }

        composable(Screen.CancelBooking.route) { backStackEntry ->
            val source = backStackEntry.arguments?.getString("source") ?: SOURCE_CREATE

            if (source == SOURCE_LIST) {
                val bookingListViewModel: BookingListViewModel = sharedActivityViewModel()
                val bookingViewModel: BookingViewModel = sharedActivityViewModel()
                val reservation = bookingListViewModel.selectedReservation

                if (reservation != null) {
                    CancelBookingScreen(
                        reservation = reservation,
                        onCancelled = {
                            bookingListViewModel.refresh()
                            navController.popBackStack(Screen.BookingList.route, inclusive = false)
                        },
                        onBack = { navController.popBackStack() },
                        bookingViewModel = bookingViewModel
                    )
                }
            } else {
                val createBookingEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.CreateBooking.route)
                }
                val bookingViewModel: BookingViewModel = viewModel(createBookingEntry)
                val state = bookingViewModel.createBookingState

                if (state is CreateBookingState.Success) {
                    CancelBookingScreen(
                        reservation = state.reservation,
                        onCancelled = {
                            navController.navigate(Screen.BookingList.route) {
                                popUpTo(Screen.Dashboard.route)
                            }
                        },
                        onBack = { navController.popBackStack() },
                        bookingViewModel = bookingViewModel
                    )
                }
            }
        }
    }
}

/**
 * All operator tabs share the view model scoped to the operator dashboard entry,
 * so counts, the pending list and the scan result stay consistent between tabs.
 */
@Composable
private fun operatorViewModel(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry
): OperatorViewModel {
    val homeEntry = remember(backStackEntry) {
        navController.getBackStackEntry(Screen.OperatorHome.route)
    }
    return viewModel(homeEntry)
}