package com.juanpablo0612.tucargo.core.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun rememberLocationPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit {
    // GPS is not implemented on iOS yet (IosLocationProvider is a stub), so
    // report denied: callers show their location-unavailable state instead of
    // waiting on updates that never come.
    val currentOnResult by rememberUpdatedState(onResult)
    return { currentOnResult(false) }
}

@Composable
actual fun rememberBackgroundLocationPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)
    return { currentOnResult(true) }
}
