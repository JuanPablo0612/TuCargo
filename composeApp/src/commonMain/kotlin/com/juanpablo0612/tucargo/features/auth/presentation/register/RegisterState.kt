package com.juanpablo0612.tucargo.features.auth.presentation.register

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.core.ui.event.UiEvent

@Immutable
data class RegisterState(
    val isLoading: Boolean = false,
    val error: RegisterError? = null,
    val navigationEvent: UiEvent<String>? = null
)
