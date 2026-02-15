package com.juanpablo0612.tucargo.features.auth.presentation.login

sealed interface LoginAction {
    data object ValidateEmail : LoginAction
    data object ValidatePassword : LoginAction
    data object Login : LoginAction
}