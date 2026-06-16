package com.juanpablo0612.tucargo.features.auth.driverdocs

sealed interface DriverDocsError {
    data object UserNotAuthenticated : DriverDocsError
    data object UploadError : DriverDocsError
    data object SomeDocsFailed : DriverDocsError
    data object FileTooLarge : DriverDocsError
}
