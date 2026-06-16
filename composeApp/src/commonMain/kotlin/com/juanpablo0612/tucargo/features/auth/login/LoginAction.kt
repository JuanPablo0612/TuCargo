package com.juanpablo0612.tucargo.features.auth.login

sealed interface LoginAction {
    data object ValidateEmail : LoginAction
    data object ValidatePassword : LoginAction
    data object Login : LoginAction
}