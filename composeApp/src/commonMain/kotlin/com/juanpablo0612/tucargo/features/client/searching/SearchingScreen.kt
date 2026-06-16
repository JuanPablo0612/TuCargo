package com.juanpablo0612.tucargo.features.client.searching

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.usecase.trip.CancelTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.ObserveTripUseCase
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.searching_cancel_button
import tucargo.composeapp.generated.resources.searching_no_driver_message
import tucargo.composeapp.generated.resources.searching_no_driver_title
import tucargo.composeapp.generated.resources.searching_retry_button
import tucargo.composeapp.generated.resources.searching_title

@Composable
fun SearchingScreen(
    tripId: String,
    onTripAccepted: (tripId: String) -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    val observeTripUseCase: ObserveTripUseCase = koinInject()
    val cancelTripUseCase: CancelTripUseCase = koinInject()

    var showNoDriverDialog by remember { mutableStateOf(false) }

    LaunchedEffect(tripId) {
        observeTripUseCase(tripId).collectLatest { trip ->
            when (trip.status) {
                TripStatus.ACCEPTED -> onTripAccepted(trip.id)
                TripStatus.CANCELLED_NO_DRIVER -> showNoDriverDialog = true
                else -> Unit
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(Res.string.searching_title),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    if (showNoDriverDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(Res.string.searching_no_driver_title)) },
            text = { Text(stringResource(Res.string.searching_no_driver_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showNoDriverDialog = false
                    onRetry()
                }) {
                    Text(stringResource(Res.string.searching_retry_button))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNoDriverDialog = false
                    onCancel()
                }) {
                    Text(stringResource(Res.string.searching_cancel_button))
                }
            }
        )
    }
}
