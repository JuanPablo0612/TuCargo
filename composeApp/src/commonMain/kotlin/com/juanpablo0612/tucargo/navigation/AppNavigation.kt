// RUTA: composeApp/src/commonMain/kotlin/com/juanpablo0612/tucargo/navigation/AppNavigation.kt

package com.juanpablo0612.tucargo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.juanpablo0612.tucargo.features.auth.presentation.documents.DocumentScreen
import com.juanpablo0612.tucargo.features.auth.presentation.login.LoginScreen
import com.juanpablo0612.tucargo.features.auth.presentation.register.RegisterScreen
import com.juanpablo0612.tucargo.features.auth.presentation.welcome.WelcomeScreen
import com.juanpablo0612.tucargo.features.client.cargo.CargoDetailsScreen
import com.juanpablo0612.tucargo.features.client.home.ClientHomeScreen
import com.juanpablo0612.tucargo.features.client.order.ReviewOrderScreen
import com.juanpablo0612.tucargo.features.client.route.SetRouteScreen
import com.juanpablo0612.tucargo.features.driver.home.presentation.DriverHomeScreen
import kotlinx.serialization.Serializable

@Serializable object Welcome
@Serializable object Login
@Serializable object Register
@Serializable object Documents
@Serializable object ClientHome
@Serializable object SetRoute
@Serializable object CargoDetails
@Serializable object ReviewOrder
@Serializable object DriverHome // Nueva ruta para el conductor

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Welcome
    ) {
        composable<Welcome> {
            WelcomeScreen(
                onSendCargoClick = { navController.navigate(Login) },
                onDriverClick = { navController.navigate(Login) }
            )
        }

        composable<Login> {
            LoginScreen(
                onForgotPasswordClick = { },
                onLoginSuccess = { role ->
                    val destination = if (role == "DRIVER") DriverHome else ClientHome
                    navController.navigate(destination) {
                        popUpTo<Welcome> { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Register) }
            )
        }

        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Documents) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Documents> {
            DocumentScreen(
                onSuccessNavigate = {
                    navController.navigate(Login) {
                        popUpTo<Register> { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<ClientHome> {
            ClientHomeScreen(
                onNewTrip = { navController.navigate(SetRoute) },
                onSignOut = {
                    navController.navigate(Welcome) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<SetRoute> {
            SetRouteScreen(
                onBackClick = { navController.popBackStack() },
                onChooseOnMapClick = { navController.navigate(CargoDetails) },
                onSuggestionClick = { _ -> navController.navigate(CargoDetails) }
            )
        }

        composable<CargoDetails> {
            CargoDetailsScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { _ -> navController.navigate(ReviewOrder) }
            )
        }

        composable<ReviewOrder> {
            ReviewOrderScreen(
                onBackClick = { navController.popBackStack() },
                onRequestDriverClick = { 
                    // Regresamos a Home o vamos a Tracking
                    navController.navigate(ClientHome) {
                        popUpTo<ClientHome> { inclusive = true }
                    }
                }
            )
        }

        // MÓDULO CONDUCTOR (11 días)
        composable<DriverHome> {
            DriverHomeScreen(
                onSignOut = {
                    navController.navigate(Welcome) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
