package com.juanpablo0612.tucargo.features.trip.active

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import org.jetbrains.compose.resources.painterResource
import tucargo.composeapp.generated.resources.arrow_back
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ResponsiveContainer
import com.juanpablo0612.tucargo.core.ui.theme.LocalDimensions
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.components.LoadingButton
import com.juanpablo0612.tucargo.core.ui.components.TripStatusBadge
import com.juanpablo0612.tucargo.core.ui.toCurrencyString
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.trip.nextDriverStatus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.trip_active_action_arrived_at_destination
import tucargo.composeapp.generated.resources.trip_active_action_arrived_at_origin
import tucargo.composeapp.generated.resources.trip_active_action_complete
import tucargo.composeapp.generated.resources.trip_active_action_start
import tucargo.composeapp.generated.resources.trip_active_back_button
import tucargo.composeapp.generated.resources.trip_active_cargo_section
import tucargo.composeapp.generated.resources.trip_active_client_section
import tucargo.composeapp.generated.resources.trip_active_code_locked
import tucargo.composeapp.generated.resources.trip_active_code_invalid_attempts
import tucargo.composeapp.generated.resources.trip_active_complete_dialog_cancel
import tucargo.composeapp.generated.resources.trip_active_complete_dialog_confirm
import tucargo.composeapp.generated.resources.trip_active_complete_dialog_message
import tucargo.composeapp.generated.resources.trip_active_complete_dialog_title
import tucargo.composeapp.generated.resources.trip_active_destination_label
import tucargo.composeapp.generated.resources.trip_active_geofence_cancel
import tucargo.composeapp.generated.resources.trip_active_geofence_confirm
import tucargo.composeapp.generated.resources.trip_active_geofence_dialog_message
import tucargo.composeapp.generated.resources.trip_active_geofence_dialog_title
import tucargo.composeapp.generated.resources.trip_active_load_error
import tucargo.composeapp.generated.resources.trip_active_origin_label
import tucargo.composeapp.generated.resources.trip_active_phone_label
import tucargo.composeapp.generated.resources.trip_active_title
import tucargo.composeapp.generated.resources.trip_active_update_error

@Composable
fun TripActiveScreen(
    tripId: String,
    onBackClick: () -> Unit,
    onTripCompleted: () -> Unit,
) {
    val viewModel: TripActiveViewModel = koinViewModel { parametersOf(tripId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.trip?.status) {
        if (uiState.trip?.status == TripStatus.COMPLETED) {
            onTripCompleted()
        }
    }

    TripActiveScreenContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripActiveScreenContent(
    uiState: TripActiveState,
    onAction: (TripActiveAction) -> Unit,
    onBackClick: () -> Unit,
) {
    var showCompleteDialog by remember { mutableStateOf(false) }

    if (uiState.showGeofenceDialog) {
        AlertDialog(
            onDismissRequest = { onAction(TripActiveAction.DismissGeofenceDialog) },
            title = { Text(stringResource(Res.string.trip_active_geofence_dialog_title)) },
            text = { Text(stringResource(Res.string.trip_active_geofence_dialog_message)) },
            confirmButton = {
                TextButton(onClick = { onAction(TripActiveAction.ConfirmGeofenceOverride) }) {
                    Text(stringResource(Res.string.trip_active_geofence_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(TripActiveAction.DismissGeofenceDialog) }) {
                    Text(stringResource(Res.string.trip_active_geofence_cancel))
                }
            },
        )
    }

    if (showCompleteDialog) {
        DeliveryCodeDialog(
            error = uiState.error,
            isLoading = uiState.isUpdating,
            isLocked = uiState.trip?.deliveryCodeAttempts?.let { it >= 5 } == true
                    || uiState.error == TripActiveError.DeliveryCodeLocked,
            onConfirm = { code ->
                showCompleteDialog = false
                onAction(TripActiveAction.CompleteWithCode(code))
            },
            onDismiss = { showCompleteDialog = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.trip_active_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = stringResource(Res.string.trip_active_back_button),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val dimensions = LocalDimensions.current
        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                uiState.trip == null -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensions.formHorizontalPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorCard(
                        message = stringResource(Res.string.trip_active_load_error),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                else -> TripActiveBody(
                    uiState = uiState,
                    trip = uiState.trip,
                    onAdvanceClick = { isCompletion ->
                        if (isCompletion) showCompleteDialog = true
                        else onAction(TripActiveAction.AdvanceStatus)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimensions.formHorizontalPadding)
                        .verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}

@Composable
private fun TripActiveBody(
    uiState: TripActiveState,
    trip: Trip,
    onAdvanceClick: (isCompletion: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TripStatusBadge(status = trip.status)
            Text(
                text = trip.priceTotal.toCurrencyString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        uiState.error?.let { error ->
            val msgRes = when (error) {
                TripActiveError.LoadError -> Res.string.trip_active_load_error
                TripActiveError.UpdateError -> Res.string.trip_active_update_error
                is TripActiveError.DeliveryCodeInvalid -> null
                TripActiveError.DeliveryCodeLocked -> null
            }
            if (msgRes != null) {
                ErrorCard(message = stringResource(msgRes), modifier = Modifier.fillMaxWidth())
            }
        }

        ActiveSection(title = stringResource(Res.string.trip_active_client_section)) {
            Text(
                text = trip.clientName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (trip.clientPhone.isNotBlank()) {
                Text(
                    text = stringResource(Res.string.trip_active_phone_label, trip.clientPhone),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        ActiveSection(title = stringResource(Res.string.trip_active_origin_label)) {
            Text(text = trip.origin.address, style = MaterialTheme.typography.bodyMedium)
        }
        ActiveSection(title = stringResource(Res.string.trip_active_destination_label)) {
            Text(text = trip.destination.address, style = MaterialTheme.typography.bodyMedium)
        }
        ActiveSection(title = stringResource(Res.string.trip_active_cargo_section)) {
            Text(text = trip.cargoDescription, style = MaterialTheme.typography.bodyMedium)
        }

        HorizontalDivider()

        val next = trip.status.nextDriverStatus()
        if (next != null) {
            val actionLabel = when (next) {
                TripStatus.AT_PICKUP -> stringResource(Res.string.trip_active_action_arrived_at_origin)
                TripStatus.IN_TRANSIT -> stringResource(Res.string.trip_active_action_start)
                TripStatus.AT_DROPOFF -> stringResource(Res.string.trip_active_action_arrived_at_destination)
                else -> stringResource(Res.string.trip_active_action_complete)
            }
            LoadingButton(
                onClick = { onAdvanceClick(next == TripStatus.COMPLETED) },
                isLoading = uiState.isUpdating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = actionLabel, style = MaterialTheme.typography.titleSmall)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DeliveryCodeDialog(
    error: TripActiveError?,
    isLoading: Boolean,
    isLocked: Boolean,
    onConfirm: (code: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val digits = remember { mutableStateListOf("", "", "", "") }
    val focusRequesters = remember { List(4) { FocusRequester() } }
    val allFilled = digits.all { it.isNotEmpty() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.trip_active_complete_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.trip_active_complete_dialog_message))

                if (isLocked) {
                    Text(
                        text = stringResource(Res.string.trip_active_code_locked),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        digits.indices.forEach { idx ->
                            BasicTextField(
                                value = digits[idx],
                                onValueChange = { newVal ->
                                    val filtered = newVal.filter { it.isDigit() }.take(1)
                                    digits[idx] = filtered
                                    if (filtered.isNotEmpty() && idx < 3) {
                                        focusRequesters[idx + 1].requestFocus()
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier
                                    .size(56.dp)
                                    .border(
                                        width = 1.dp,
                                        color = if (error is TripActiveError.DeliveryCodeInvalid)
                                            MaterialTheme.colorScheme.error
                                        else
                                            MaterialTheme.colorScheme.outline,
                                        shape = MaterialTheme.shapes.small,
                                    )
                                    .focusRequester(focusRequesters[idx])
                                    .onKeyEvent { keyEvent ->
                                        if (keyEvent.key == Key.Backspace && digits[idx].isEmpty() && idx > 0) {
                                            focusRequesters[idx - 1].requestFocus()
                                            true
                                        } else false
                                    },
                                textStyle = MaterialTheme.typography.headlineMedium.copy(
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                decorationBox = { inner ->
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        inner()
                                    }
                                },
                            )
                        }
                    }

                    when (error) {
                        is TripActiveError.DeliveryCodeInvalid ->
                            Text(
                                text = stringResource(
                                    Res.string.trip_active_code_invalid_attempts,
                                    error.remaining
                                ),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        TripActiveError.DeliveryCodeLocked ->
                            Text(
                                text = stringResource(Res.string.trip_active_code_locked),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        else -> Unit
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(digits.joinToString("")) },
                enabled = !isLoading && !isLocked && allFilled,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(Res.string.trip_active_complete_dialog_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.trip_active_complete_dialog_cancel))
            }
        },
    )

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }
}

@Composable
private fun ActiveSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        content()
    }
}
