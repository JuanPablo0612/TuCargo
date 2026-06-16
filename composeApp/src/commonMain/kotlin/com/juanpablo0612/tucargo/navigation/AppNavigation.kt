package com.juanpablo0612.tucargo.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.juanpablo0612.tucargo.domain.model.DriverOnboardingStatus
import com.juanpablo0612.tucargo.domain.model.UserRole
import com.juanpablo0612.tucargo.features.admin.home.AdminHomeScreen
import com.juanpablo0612.tucargo.features.admin.review.AdminDriverReviewScreen
import com.juanpablo0612.tucargo.features.auth.AuthViewModel
import com.juanpablo0612.tucargo.features.auth.documents.KycPendingScreen
import com.juanpablo0612.tucargo.features.auth.driverdocs.DriverDocsUploadScreen
import com.juanpablo0612.tucargo.features.auth.login.LoginScreen
import com.juanpablo0612.tucargo.features.auth.register.RegisterScreen
import com.juanpablo0612.tucargo.features.auth.resetpassword.ResetPasswordScreen
import com.juanpablo0612.tucargo.features.auth.vehicle.VehicleRegistrationScreen
import com.juanpablo0612.tucargo.features.client.createtrip.CreateTripScreen
import com.juanpablo0612.tucargo.features.client.home.ClientHomeScreen
import com.juanpablo0612.tucargo.features.client.quote.CargoScreen
import com.juanpablo0612.tucargo.features.client.quote.PickDestScreen
import com.juanpablo0612.tucargo.features.client.quote.PickOriginScreen
import com.juanpablo0612.tucargo.features.client.quote.QuoteScreen
import com.juanpablo0612.tucargo.features.client.searching.SearchingScreen
import com.juanpablo0612.tucargo.features.driver.home.DriverHomeScreen
import com.juanpablo0612.tucargo.features.trip.presentation.active.TripActiveScreen
import com.juanpablo0612.tucargo.features.trip.presentation.completed.TripCompletedScreen
import com.juanpablo0612.tucargo.features.trip.presentation.detail.TripDetailScreen
import com.juanpablo0612.tucargo.features.trip.presentation.history.TripHistoryScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable sealed class Route {
    @Serializable data object Login : Route()
    @Serializable data object Register : Route()
    @Serializable data object ResetPassword : Route()
    @Serializable data object KycPending : Route()
    @Serializable data object DriverOnboardingVehicle : Route()
    @Serializable data object DriverOnboardingDocuments : Route()
    @Serializable data object ClientHome : Route()
    @Serializable data object DriverHome : Route()
    @Serializable data object CreateTrip : Route()
    @Serializable data object PickOrigin : Route()
    @Serializable data object PickDest : Route()
    @Serializable data object Cargo : Route()
    @Serializable data object Quote : Route()
    @Serializable data class Searching(val tripId: String) : Route()
    @Serializable data object TripHistory : Route()
    @Serializable data class TripActive(val tripId: String) : Route()
    @Serializable data class TripDetail(val tripId: String) : Route()
    @Serializable data class TripCompleted(val tripId: String) : Route()
    @Serializable data object AdminHome : Route()
    @Serializable data class AdminDriverReview(val driverId: String, val driverName: String) : Route()
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    if (authState is AuthViewModel.AuthState.Loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var wasAuthenticated by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Authenticated -> wasAuthenticated = true
            is AuthViewModel.AuthState.Unauthenticated -> {
                if (wasAuthenticated) {
                    navController.navigate(Route.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> Unit
        }
    }

    val startDestination: Route = when (val s = authState) {
        is AuthViewModel.AuthState.Authenticated -> when {
            s.user.role == UserRole.ADMIN -> Route.AdminHome
            s.user.role == UserRole.CLIENT -> Route.ClientHome
            s.user.isVerified -> Route.DriverHome
            else -> driverOnboardingRoute(s.driverOnboardingStatus)
        }
        else -> Route.Login
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authNavGraph(navController)

        composable<Route.ClientHome> {
            ClientHomeScreen(
                onNewTrip = { navController.navigate(Route.PickOrigin) },
                onSignOut = { authViewModel.logout() },
                onTripClick = { tripId -> navController.navigate(Route.TripDetail(tripId)) },
                onViewAllClick = { navController.navigate(Route.TripHistory) },
            )
        }

        composable<Route.CreateTrip> {
            CreateTripScreen(
                onTripCreated = { tripId ->
                    navController.navigate(Route.TripDetail(tripId)) {
                        popUpTo<Route.CreateTrip> { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable<Route.PickOrigin> {
            PickOriginScreen(
                onConfirmed = { navController.navigate(Route.PickDest) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Route.PickDest> {
            PickDestScreen(
                onConfirmed = { navController.navigate(Route.Cargo) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Route.Cargo> {
            CargoScreen(
                onNext = { navController.navigate(Route.Quote) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Route.Quote> {
            QuoteScreen(
                onTripCreated = { tripId ->
                    navController.navigate(Route.Searching(tripId)) {
                        popUpTo<Route.PickOrigin> { inclusive = true }
                    }
                },
                onNewQuote = {
                    navController.navigate(Route.PickOrigin) {
                        popUpTo<Route.PickOrigin> { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Route.Searching> { backStackEntry ->
            val route: Route.Searching = backStackEntry.toRoute()
            SearchingScreen(
                tripId = route.tripId,
                onTripAccepted = { tripId ->
                    navController.navigate(Route.TripDetail(tripId)) {
                        popUpTo<Route.ClientHome> { inclusive = false }
                    }
                },
                onRetry = {
                    navController.navigate(Route.PickOrigin) {
                        popUpTo<Route.ClientHome> { inclusive = false }
                    }
                },
                onCancel = {
                    navController.navigate(Route.ClientHome) {
                        popUpTo<Route.ClientHome> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.DriverHome> {
            DriverHomeScreen(
                onSignOut = { authViewModel.logout() },
                onTripClick = { tripId -> navController.navigate(Route.TripActive(tripId)) },
                onHistoryClick = { navController.navigate(Route.TripHistory) },
            )
        }

        composable<Route.TripHistory> {
            TripHistoryScreen(
                onTripClick = { tripId -> navController.navigate(Route.TripDetail(tripId)) },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable<Route.TripActive> { backStackEntry ->
            val route: Route.TripActive = backStackEntry.toRoute()
            TripActiveScreen(
                tripId = route.tripId,
                onBackClick = { navController.popBackStack() },
                onTripCompleted = {
                    navController.navigate(Route.TripCompleted(route.tripId)) {
                        popUpTo<Route.DriverHome> { inclusive = false }
                    }
                },
            )
        }

        composable<Route.TripDetail> { backStackEntry ->
            val route: Route.TripDetail = backStackEntry.toRoute()
            TripDetailScreen(
                tripId = route.tripId,
                onBackClick = { navController.popBackStack() },
                onTripCompleted = {
                    navController.navigate(Route.TripCompleted(route.tripId)) {
                        popUpTo<Route.ClientHome> { inclusive = false }
                    }
                },
            )
        }

        composable<Route.TripCompleted> { backStackEntry ->
            val route: Route.TripCompleted = backStackEntry.toRoute()
            TripCompletedScreen(
                tripId = route.tripId,
                onDriverHomeClick = {
                    navController.navigate(Route.DriverHome) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onClientHomeClick = {
                    navController.navigate(Route.ClientHome) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable<Route.DriverOnboardingVehicle> {
            VehicleRegistrationScreen(
                onSuccessNavigate = {
                    navController.navigate(Route.DriverOnboardingDocuments) {
                        popUpTo<Route.DriverOnboardingVehicle> { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Route.DriverOnboardingDocuments> {
            DriverDocsUploadScreen(
                onSuccessNavigate = {
                    navController.navigate(Route.KycPending) {
                        popUpTo<Route.DriverOnboardingDocuments> { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Route.KycPending> {
            KycPendingScreen(
                onVerified = {
                    navController.navigate(Route.DriverHome) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Route.AdminHome> {
            AdminHomeScreen(
                onDriverClick = { driverId, driverName ->
                    navController.navigate(Route.AdminDriverReview(driverId, driverName))
                },
                onSignOut = { authViewModel.logout() },
            )
        }

        composable<Route.AdminDriverReview> { backStackEntry ->
            val route: Route.AdminDriverReview = backStackEntry.toRoute()
            AdminDriverReviewScreen(
                driverId = route.driverId,
                driverName = route.driverName,
                onVerified = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}

private fun driverOnboardingRoute(status: DriverOnboardingStatus?): Route = when (status) {
    is DriverOnboardingStatus.Verified -> Route.DriverHome
    is DriverOnboardingStatus.PendingReview -> Route.KycPending
    is DriverOnboardingStatus.IncompleteDocs -> Route.DriverOnboardingDocuments
    is DriverOnboardingStatus.IncompleteVehicle, null -> Route.DriverOnboardingVehicle
}

private fun NavGraphBuilder.authNavGraph(navController: NavController) {
    composable<Route.Login> {
        LoginScreen(
            onForgotPasswordClick = { navController.navigate(Route.ResetPassword) },
            onLoginSuccess = { role ->
                val home = when (role) {
                    UserRole.ADMIN -> Route.AdminHome
                    UserRole.DRIVER -> Route.DriverOnboardingVehicle
                    UserRole.CLIENT -> Route.ClientHome
                }
                navController.navigate(home) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onRegisterClick = { navController.navigate(Route.Register) }
        )
    }

    composable<Route.Register> {
        RegisterScreen(
            onRegisterSuccess = { role ->
                when (role) {
                    UserRole.DRIVER -> navController.navigate(Route.DriverOnboardingVehicle) {
                        popUpTo<Route.Register> { inclusive = true }
                    }
                    // Registration only offers CLIENT and DRIVER.
                    else -> navController.navigate(Route.ClientHome) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            onBackClick = { navController.popBackStack() }
        )
    }

    composable<Route.ResetPassword> {
        ResetPasswordScreen(
            onBackClick = { navController.popBackStack() }
        )
    }
}
