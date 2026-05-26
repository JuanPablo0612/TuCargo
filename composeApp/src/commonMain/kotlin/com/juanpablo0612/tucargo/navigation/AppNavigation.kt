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
import com.juanpablo0612.tucargo.features.auth.presentation.AuthViewModel
import com.juanpablo0612.tucargo.features.auth.presentation.documents.DocumentScreen
import com.juanpablo0612.tucargo.features.auth.presentation.login.LoginScreen
import com.juanpablo0612.tucargo.features.auth.presentation.register.RegisterScreen
import com.juanpablo0612.tucargo.features.auth.presentation.resetpassword.ResetPasswordScreen
import com.juanpablo0612.tucargo.features.client.home.ClientHomeScreen
import com.juanpablo0612.tucargo.features.driver.home.presentation.DriverHomeScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable sealed class Route {
    @Serializable data object Login : Route()
    @Serializable data object Register : Route()
    @Serializable data object ResetPassword : Route()
    @Serializable data object KycUpload : Route()
    @Serializable data object KycPending : Route()
    @Serializable data object ClientHome : Route()
    @Serializable data object DriverHome : Route()
    @Serializable data object VehicleSetup : Route()
    @Serializable data object Wallet : Route()
    @Serializable data object Topup : Route()
    @Serializable data object Profile : Route()
    @Serializable data object Searching : Route()
    @Serializable data object TripHistory : Route()
    @Serializable data class TripActive(val tripId: String) : Route()
    @Serializable data class TripCompleted(val tripId: String) : Route()
    @Serializable data class TripDetail(val tripId: String) : Route()
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // Show loading until we know the initial auth state
    if (authState is AuthViewModel.AuthState.Loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Track whether the user was ever authenticated this session so we only redirect
    // to Login on logout, not on the initial cold-start when the user is simply not signed in.
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

    // Initial start destination is determined once from auth state (NavHost ignores
    // startDestination changes after first composition, so this is safe).
    val startDestination: Route = when (val s = authState) {
        is AuthViewModel.AuthState.Authenticated ->
            if (s.user.role == "DRIVER") Route.DriverHome else Route.ClientHome
        else -> Route.Login
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authNavGraph(navController)

        composable<Route.ClientHome> {
            ClientHomeScreen(
                onSignOut = { authViewModel.logout() }
            )
        }

        composable<Route.DriverHome> {
            DriverHomeScreen(
                onSignOut = { authViewModel.logout() }
            )
        }

        composable<Route.KycUpload> {
            DocumentScreen(
                onSuccessNavigate = {
                    navController.navigate(Route.KycPending) {
                        popUpTo<Route.KycUpload> { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

private fun NavGraphBuilder.authNavGraph(navController: NavController) {
    composable<Route.Login> {
        LoginScreen(
            onForgotPasswordClick = { navController.navigate(Route.ResetPassword) },
            onLoginSuccess = { role ->
                val home = if (role == "DRIVER") Route.DriverHome else Route.ClientHome
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
                    "DRIVER" -> navController.navigate(Route.KycUpload) {
                        popUpTo<Route.Register> { inclusive = true }
                    }
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
