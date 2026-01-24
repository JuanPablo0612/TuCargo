package com.juanpablo0612.tucargo.features.auth.presentation.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    val emailState = TextFieldState()
    val passwordState = TextFieldState()

    fun onLogin() {

    }
}