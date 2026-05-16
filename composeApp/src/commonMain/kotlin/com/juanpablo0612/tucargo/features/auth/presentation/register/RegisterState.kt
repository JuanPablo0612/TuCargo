package com.juanpablo0612.tucargo.features.auth.presentation.register

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.core.ui.event.UiEvent

@Immutable
data class RegisterState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigationEvent: UiEvent<Unit>? = null
)
