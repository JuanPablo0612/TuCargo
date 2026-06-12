package com.juanpablo0612.tucargo.features.trip.presentation.active

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.components.LoadingButton
import com.juanpablo0612.tucargo.core.ui.components.RoundedTextField
import com.juanpablo0612.tucargo.core.ui.components.TripStatusBadge
import com.juanpablo0612.tucargo.core.ui.toCurrencyString
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.trip.nextDriverStatus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.trip_active_action_arrived
import tucargo.composeapp.generated.resources.trip_active_action_complete
import tucargo.composeapp.generated.resources.trip_active_action_pickup
import tucargo.composeapp.generated.resources.trip_active_action_start
import tucargo.composeapp.generated.resources.trip_active_back_button
import tucargo.composeapp.generated.resources.trip_active_cargo_section
import tucargo.composeapp.generated.resources.trip_active_client_section
import tucargo.composeapp.generated.resources.trip_active_complete_dialog_cancel
import tucargo.composeapp.generated.resources.trip_active_complete_dialog_confirm
import tucargo.composeapp.generated.resources.trip_active_complete_dialog_hint
import tucargo.composeapp.generated.resources.trip_active_complete_dialog_message
import tucargo.composeapp.generated.resources.trip_active_complete_dialog_title
import tucargo.composeapp.generated.resources.trip_active_destination_label
import tucargo.composeapp.generated.resources.trip_active_invalid_code_error
import tucargo.composeapp.generated.resources.trip_active_load_error
import tucargo.composeapp.generated.resources.trip_active_origin_label
import tucargo.composeapp.generated.resources.trip_active_phone_label
import tucargo.composeapp.generated.resources.trip_active_title
import tucargo.composeapp.generated.resources.trip_active_update_error

@Composable
fun TripActiveScreen(
    tripId: String,
    onBackClick: () -> Unit,
) {
    val viewModel: TripActiveViewModel = koinViewModel { parametersOf(tripId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
    val codeState = rememberTextFieldState()

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text(stringResource(Res.string.trip_active_complete_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(Res.string.trip_active_complete_dialog_message))
                    RoundedTextField(
                        state = codeState,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.trip_active_complete_dialog_hint)) },
                        isError = uiState.error == TripActiveError.InvalidDeliveryCode,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCompleteDialog = false
                        onAction(TripActiveAction.CompleteWithCode(codeState.text.toString()))
                    }
                ) {
                    Text(stringResource(Res.string.trip_active_complete_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text(stringResource(Res.string.trip_active_complete_dialog_cancel))
                }
            },
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.trip_active_back_button),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            uiState.trip == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
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
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
            )
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
                TripActiveError.InvalidDeliveryCode -> Res.string.trip_active_invalid_code_error
            }
            ErrorCard(message = stringResource(msgRes), modifier = Modifier.fillMaxWidth())
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
                TripStatus.ON_WAY -> stringResource(Res.string.trip_active_action_start)
                TripStatus.ARRIVED_PICKUP -> stringResource(Res.string.trip_active_action_arrived)
                TripStatus.IN_PROGRESS -> stringResource(Res.string.trip_active_action_pickup)
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
