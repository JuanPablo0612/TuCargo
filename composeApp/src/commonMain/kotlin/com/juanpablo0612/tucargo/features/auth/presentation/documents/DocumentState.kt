package com.juanpablo0612.tucargo.features.auth.presentation.documents

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.core.ui.event.UiEvent

@Immutable
data class DocumentState(
    val isLoading: Boolean = false,
    val idFrontPath: String? = null,
    val idBackPath: String? = null,
    val error: DocumentError? = null,
    val navigationEvent: UiEvent<Unit>? = null
)

sealed interface DocumentAction {
    data class OnFrontPhotoSelected(val path: String) : DocumentAction
    data class OnBackPhotoSelected(val path: String) : DocumentAction
    data object OnSubmit : DocumentAction // Esto arregla el error "Unresolved reference OnSubmit"
    data object OnBackClick : DocumentAction
}