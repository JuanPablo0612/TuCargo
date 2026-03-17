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
import kotlinx.serialization.Serializable

@Serializable object Welcome
@Serializable object Login
@Serializable object Register
@Serializable object Documents
@Serializable object ClientHome

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
                onLoginSuccess = {
                    navController.navigate(ClientHome) {
                        popUpTo<Welcome> { inclusive = true }
                    }
                }
            )
        }

        composable<Register> {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Documents) {
                        popUpTo<Welcome> { inclusive = true }
                    }
                }
            )
        }

        composable<Documents> {
            DocumentScreen(
                onBackClick = { navController.popBackStack() },
                onSuccessNavigate = {
                    navController.navigate(ClientHome) {
                        popUpTo<Welcome> { inclusive = true }
                    }
                }
            )
        }

        composable<ClientHome> {
            ClientHomeScreen(
                onNewShipment = { },
                onSignOut = {
                    navController.navigate(Welcome) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
