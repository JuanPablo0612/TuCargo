package com.juanpablo0612.tucargo.features.auth.presentation.register

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {
    // Definimos los estados que la Screen está intentando usar
    val nameState = TextFieldState()
    val emailState = TextFieldState()
    val passwordState = TextFieldState()
    val confirmPasswordState = TextFieldState()

    fun onRegister() {
        // Lógica para registrar al usuario
        println("Registrando usuario...")
    }
}