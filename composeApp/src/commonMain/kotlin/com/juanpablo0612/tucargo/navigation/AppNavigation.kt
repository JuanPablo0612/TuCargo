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
import com.juanpablo0612.tucargo.features.client.home.ClientHomeScreen
import com.juanpablo0612.tucargo.features.driver.home.presentation.DriverHomeScreen
import kotlinx.serialization.Serializable

@Serializable object Welcome
@Serializable object Login
@Serializable object Register
@Serializable object Documents
@Serializable object ClientHome
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
                // quitamos 'onNewShipment' si tu Composable no lo tiene
                onSignOut = {
                    navController.navigate(Welcome) {
                        popUpTo(0) { inclusive = true }
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
