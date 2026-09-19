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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartsolarmicrogrid.prosumer.ui.auth.LoginScreen
import com.smartsolarmicrogrid.prosumer.ui.auth.RegisterScreen
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
import com.smartsolarmicrogrid.prosumer.ui.qr.BookingQrScreen
import com.smartsolarmicrogrid.prosumer.ui.profile.EditProfileScreen
import com.smartsolarmicrogrid.prosumer.ui.profile.ProfileScreen
import com.smartsolarmicrogrid.prosumer.ui.station.SlotListScreen
import com.smartsolarmicrogrid.prosumer.ui.station.StationListScreen
import com.smartsolarmicrogrid.prosumer.ui.station.StationViewModel

/** Where the user reached Modify / Cancel from: the new-booking flow or the booking list. */
const val SOURCE_CREATE = "create"
const val SOURCE_LIST = "list"

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Stations : Screen("stations")
    object Slots : Screen("slots")
    object CreateBooking : Screen("create_booking")
    object BookingSummary : Screen("booking_summary")
    object BookingList : Screen("booking_list")
    object BookingDetails : Screen("booking_details")
    object Dashboard : Screen("dashboard")
    object BookingQr : Screen("booking_qr")

    object ModifyBooking : Screen("modify_booking/{source}") {
        fun createRoute(source: String) = "modify_booking/$source"
    }

    object CancelBooking : Screen("cancel_booking/{source}") {
        fun createRoute(source: String) = "cancel_booking/$source"
    }
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    // TODO: set back to Screen.Login.route before final submission
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Prosumers land straight on the dashboard after signing in.
                    navController.navigate(Screen.Dashboard.route) {
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

        composable(Screen.Profile.route) {
            WithBottomBar(navController, Screen.Profile.route) {
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
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Stations.route) {
            val stationViewModel: StationViewModel = viewModel()
            WithBottomBar(navController, Screen.Stations.route) {
                StationListScreen(
                    onStationSelected = { station ->
                        stationViewModel.selectStationAndLoadSlots(station)
                        navController.navigate(Screen.Slots.route)
                    },
                    onBack = { navController.popBackStack() },
                    stationViewModel = stationViewModel
                )
            }
        }

        composable(Screen.Slots.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Stations.route)
            }
            val stationViewModel: StationViewModel = viewModel(parentEntry)
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
            val stationViewModel: StationViewModel = viewModel(parentEntry)
            val bookingViewModel: BookingViewModel = viewModel(backStackEntry)

            val station = stationViewModel.selectedStation
            val slot = stationViewModel.selectedSlot

            if (station != null && slot != null) {
                CreateBookingScreen(
                    station = station,
                    slot = slot,
                    onBookingConfirmed = { navController.navigate(Screen.BookingSummary.route) },
                    onBack = { navController.popBackStack() },
                    bookingViewModel = bookingViewModel
                )
            }
        }

        composable(Screen.BookingSummary.route) { backStackEntry ->
            val createBookingEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.CreateBooking.route)
            }
            val bookingViewModel: BookingViewModel = viewModel(createBookingEntry)
            val state = bookingViewModel.createBookingState

            if (state is CreateBookingState.Success) {
                BookingSummaryScreen(
                    reservation = state.reservation,
                    onDone = {
                        navController.navigate(Screen.BookingList.route) {
                            popUpTo(Screen.Dashboard.route)
                        }
                    },
                    onModify = {
                        navController.navigate(Screen.ModifyBooking.createRoute(SOURCE_CREATE))
                    },
                    onCancel = {
                        navController.navigate(Screen.CancelBooking.createRoute(SOURCE_CREATE))
                    }
                )
            }
        }

        composable(Screen.Dashboard.route) {
            WithBottomBar(navController, Screen.Dashboard.route) {
                DashboardScreen(
                    onViewAllBookings = { navController.navigate(Screen.BookingList.route) },
                    onBack = null
                )
            }
        }

        // ---------- Booking views: Current / Pending / History + search ----------

        composable(Screen.BookingList.route) { backStackEntry ->
            val bookingListViewModel: BookingListViewModel = viewModel(backStackEntry)
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
            val listEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.BookingList.route)
            }
            val bookingListViewModel: BookingListViewModel = viewModel(listEntry)
            val bookingViewModel: BookingViewModel = viewModel(listEntry)
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
            val listEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.BookingList.route)
            }
            val bookingListViewModel: BookingListViewModel = viewModel(listEntry)
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
                val listEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.BookingList.route)
                }
                val bookingListViewModel: BookingListViewModel = viewModel(listEntry)
                val bookingViewModel: BookingViewModel = viewModel(listEntry)
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
                val listEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.BookingList.route)
                }
                val bookingListViewModel: BookingListViewModel = viewModel(listEntry)
                val bookingViewModel: BookingViewModel = viewModel(listEntry)
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