package com.juanpablo0612.tucargo.features.trip.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.juanpablo0612.tucargo.core.ui.components.MapComponent
import com.juanpablo0612.tucargo.core.ui.components.TripStatusBadge
import com.juanpablo0612.tucargo.core.ui.toCurrencyString
import com.juanpablo0612.tucargo.core.ui.toDistanceString
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.trip.isCancelled
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.driver_home_trip_distance
import tucargo.composeapp.generated.resources.trip_detail_back_button
import tucargo.composeapp.generated.resources.trip_detail_cancel_button
import tucargo.composeapp.generated.resources.trip_detail_cancel_confirm_message
import tucargo.composeapp.generated.resources.trip_detail_cancel_confirm_no
import tucargo.composeapp.generated.resources.trip_detail_cancel_confirm_title
import tucargo.composeapp.generated.resources.trip_detail_cancel_confirm_yes
import tucargo.composeapp.generated.resources.trip_detail_cancel_error
import tucargo.composeapp.generated.resources.trip_detail_cargo_label
import tucargo.composeapp.generated.resources.trip_detail_delivery_code_hint
import tucargo.composeapp.generated.resources.trip_detail_delivery_code_title
import tucargo.composeapp.generated.resources.trip_detail_destination_label
import tucargo.composeapp.generated.resources.trip_detail_driver_pending
import tucargo.composeapp.generated.resources.trip_detail_driver_section
import tucargo.composeapp.generated.resources.trip_detail_load_error
import tucargo.composeapp.generated.resources.trip_detail_origin_label
import tucargo.composeapp.generated.resources.trip_detail_plate_label
import tucargo.composeapp.generated.resources.trip_detail_price_label
import tucargo.composeapp.generated.resources.trip_detail_title

@Composable
fun TripDetailScreen(
    tripId: String,
    onBackClick: () -> Unit,
) {
    val viewModel: TripDetailViewModel = koinViewModel { parametersOf(tripId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TripDetailScreenContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripDetailScreenContent(
    uiState: TripDetailState,
    onAction: (TripDetailAction) -> Unit,
    onBackClick: () -> Unit,
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(Res.string.trip_detail_cancel_confirm_title)) },
            text = { Text(stringResource(Res.string.trip_detail_cancel_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        onAction(TripDetailAction.CancelTrip)
                    }
                ) {
                    Text(stringResource(Res.string.trip_detail_cancel_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(Res.string.trip_detail_cancel_confirm_no))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.trip_detail_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.trip_detail_back_button),
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
                    message = stringResource(Res.string.trip_detail_load_error),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            else -> TripDetailBody(
                uiState = uiState,
                trip = uiState.trip,
                onCancelClick = { showCancelDialog = true },
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
private fun TripDetailBody(
    uiState: TripDetailState,
    trip: Trip,
    onCancelClick: () -> Unit,
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

        if (uiState.error == TripDetailError.CancelError) {
            ErrorCard(
                message = stringResource(Res.string.trip_detail_cancel_error),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Card(shape = MaterialTheme.shapes.large) {
            MapComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                latitude = trip.driverLastLat ?: trip.origin.lat,
                longitude = trip.driverLastLng ?: trip.origin.lng,
            )
        }

        DetailSection(title = stringResource(Res.string.trip_detail_driver_section)) {
            if (trip.driverId == null) {
                Text(
                    text = stringResource(Res.string.trip_detail_driver_pending),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                DetailRow(label = trip.driverName, value = "")
                DetailRow(
                    label = stringResource(Res.string.trip_detail_plate_label),
                    value = trip.driverPlate,
                )
            }
        }

        DetailSection(title = stringResource(Res.string.trip_detail_origin_label)) {
            Text(text = trip.origin.address, style = MaterialTheme.typography.bodyMedium)
        }
        DetailSection(title = stringResource(Res.string.trip_detail_destination_label)) {
            Text(text = trip.destination.address, style = MaterialTheme.typography.bodyMedium)
        }
        DetailSection(title = stringResource(Res.string.trip_detail_cargo_label)) {
            Text(text = trip.cargoDescription, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(
                    Res.string.driver_home_trip_distance,
                    trip.distanceKm.toDistanceString()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DetailSection(title = stringResource(Res.string.trip_detail_price_label)) {
            Text(
                text = trip.priceTotal.toCurrencyString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        if (uiState.isClient && trip.status != TripStatus.COMPLETED && !trip.status.isCancelled()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.trip_detail_delivery_code_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Text(
                        text = trip.deliveryCode,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Text(
                        text = stringResource(Res.string.trip_detail_delivery_code_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }

        if (uiState.isClient &&
            (trip.status == TripStatus.REQUESTED || trip.status == TripStatus.OFFERED || trip.status == TripStatus.ACCEPTED)
        ) {
            OutlinedButton(
                onClick = onCancelClick,
                enabled = !uiState.isCancelling,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(Res.string.trip_detail_cancel_button),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), thickness = 0.dp)
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        content()
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
