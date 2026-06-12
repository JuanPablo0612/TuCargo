package com.juanpablo0612.tucargo.features.auth.presentation.documents

sealed interface DocumentError {
    data object UserNotAuthenticated : DocumentError
    data object UploadError : DocumentError
}
