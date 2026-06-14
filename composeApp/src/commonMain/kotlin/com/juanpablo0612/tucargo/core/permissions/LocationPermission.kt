package com.juanpablo0612.tucargo.core.permissions

import androidx.compose.runtime.Composable

/**
 * Returns a request() lambda. Invoking it checks the platform location
 * permission, prompting the user if needed, and reports the outcome through
 * [onResult]. If permission is already granted, [onResult] fires immediately.
 */
@Composable
expect fun rememberLocationPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit

/**
 * Returns a request() lambda for background location access.
 * Must only be called after foreground location permission is already granted.
 * On Android, this triggers the system settings flow (Android 11+).
 * On iOS, it's a no-op that immediately invokes [onResult] with true.
 */
@Composable
expect fun rememberBackgroundLocationPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit
