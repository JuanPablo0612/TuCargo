package com.juanpablo0612.tucargo.core.permissions

import androidx.compose.runtime.Composable

/**
 * Returns a request() lambda. Invoking it checks the platform location
 * permission, prompting the user if needed, and reports the outcome through
 * [onResult]. If permission is already granted, [onResult] fires immediately.
 */
@Composable
expect fun rememberLocationPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit
