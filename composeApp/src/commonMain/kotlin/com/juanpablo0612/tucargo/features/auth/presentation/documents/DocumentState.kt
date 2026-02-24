package com.juanpablo0612.tucargo.features.auth.presentation.documents

import androidx.compose.runtime.Immutable

@Immutable
data class DocumentState(
    val isLoading: Boolean = false,
    val idFrontPath: String? = null,
    val idBackPath: String? = null,
    val errorMessage: String? = null,
    val isUploadSuccess: Boolean = false
)

sealed interface DocumentAction {
    data class OnFrontPhotoSelected(val path: String) : DocumentAction
    data class OnBackPhotoSelected(val path: String) : DocumentAction
    data object OnSubmit : DocumentAction // Esto arregla el error "Unresolved reference OnSubmit"
    data object OnBackClick : DocumentAction
}