package com.juanpablo0612.tucargo.features.driver.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.components.LoadingButton
import com.juanpablo0612.tucargo.core.ui.components.TripStatusBadge
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import com.juanpablo0612.tucargo.core.ui.toCurrencyString
import com.juanpablo0612.tucargo.core.ui.toDistanceString
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.features.driver.home.presentation.components.AvailabilityButton
import com.juanpablo0612.tucargo.features.driver.home.presentation.components.BalanceCard
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.arrow_forward
import tucargo.composeapp.generated.resources.client_home_sign_out_desc
import tucargo.composeapp.generated.resources.driver_home_accept_button
import tucargo.composeapp.generated.resources.driver_home_accept_error
import tucargo.composeapp.generated.resources.driver_home_active_desc
import tucargo.composeapp.generated.resources.driver_home_active_trips_title
import tucargo.composeapp.generated.resources.driver_home_availability_error
import tucargo.composeapp.generated.resources.driver_home_available_trips_error
import tucargo.composeapp.generated.resources.driver_home_available_trips_title
import tucargo.composeapp.generated.resources.driver_home_empty_trips_message
import tucargo.composeapp.generated.resources.driver_home_history_desc
import tucargo.composeapp.generated.resources.driver_home_load_error
import tucargo.composeapp.generated.resources.driver_home_location_permission_denied
import tucargo.composeapp.generated.resources.driver_home_no_available_trips
import tucargo.composeapp.generated.resources.driver_home_offline_desc
import tucargo.composeapp.generated.resources.driver_home_title
import tucargo.composeapp.generated.resources.driver_home_tracking_error
import tucargo.composeapp.generated.resources.driver_home_trip_distance
import tucargo.composeapp.generated.resources.driver_home_trip_id_label
import tucargo.composeapp.generated.resources.driver_home_trip_taken_error
import tucargo.composeapp.generated.resources.driver_home_view_trip_button

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    viewModel: DriverHomeViewModel = koinViewModel(),
    onSignOut: () -> Unit,
    onTripClick: (tripId: String) -> Unit = {},
    onHistoryClick: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(Res.string.driver_home_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = if (state.isAvailable)
                                stringResource(Res.string.driver_home_active_desc)
                            else
                                stringResource(Res.string.driver_home_offline_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.isAvailable)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = stringResource(Res.string.driver_home_history_desc),
                        )
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(Res.string.client_home_sign_out_desc),
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                state.error?.let { driverError ->
                    item(key = "error_banner", contentType = "error") {
                        val errorRes = when (driverError) {
                            DriverHomeError.LoadDriverError -> Res.string.driver_home_load_error
                            DriverHomeError.ToggleAvailabilityError -> Res.string.driver_home_availability_error
                            DriverHomeError.TrackingError -> Res.string.driver_home_tracking_error
                            DriverHomeError.LocationPermissionDenied -> Res.string.driver_home_location_permission_denied
                            DriverHomeError.AvailableTripsError -> Res.string.driver_home_available_trips_error
                            DriverHomeError.AcceptTripError -> Res.string.driver_home_accept_error
                            DriverHomeError.TripAlreadyTaken -> Res.string.driver_home_trip_taken_error
                        }
                        ErrorCard(
                            message = stringResource(errorRes),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }

                item(key = "availability_button", contentType = "availability") {
                    AvailabilityButton(
                        isAvailable = state.isAvailable,
                        onToggle = { viewModel.onAction(DriverHomeAction.ToggleAvailability(it)) },
                    )
                }

                item(key = "balance_card", contentType = "balance") {
                    BalanceCard(
                        balance = state.balance,
                        totalTrips = state.totalTrips
                    )
                }

                if (state.isAvailable) {
                    item(key = "available_header", contentType = "header") {
                        Text(
                            text = stringResource(Res.string.driver_home_available_trips_title),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() }
                        )
                    }

                    if (state.availableTrips.isEmpty()) {
                        item(key = "available_empty", contentType = "empty") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                ),
                            ) {
                                Text(
                                    text = stringResource(Res.string.driver_home_no_available_trips),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    } else {
                        items(
                            items = state.availableTrips,
                            key = { "available_${it.id}" },
                            contentType = { "available_trip" }
                        ) { trip ->
                            AvailableTripCard(
                                trip = trip,
                                isAccepting = state.isAccepting,
                                onAccept = { viewModel.onAction(DriverHomeAction.AcceptTrip(trip.id)) },
                            )
                        }
                    }
                }

                item(key = "trips_header", contentType = "header") {
                    Text(
                        text = stringResource(Res.string.driver_home_active_trips_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() }
                    )
                }

                if (state.activeTrips.isEmpty()) {
                    item(key = "empty_state", contentType = "empty") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                        ) {
                            Text(
                                text = stringResource(Res.string.driver_home_empty_trips_message),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    items(
                        items = state.activeTrips,
                        key = { it.id },
                        contentType = { "trip_item" }
                    ) { trip ->
                        ActiveTripItem(trip = trip, onViewTrip = { onTripClick(trip.id) })
                    }
                }
            }
        }
    }
}

@Composable
internal fun AvailableTripCard(
    trip: Trip,
    isAccepting: Boolean,
    onAccept: () -> Unit,
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = trip.origin.address,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.arrow_forward),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = trip.destination.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = trip.priceTotal.toCurrencyString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(
                            Res.string.driver_home_trip_distance,
                            trip.distanceKm.toDistanceString()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LoadingButton(
                    onClick = onAccept,
                    isLoading = isAccepting,
                ) {
                    Text(stringResource(Res.string.driver_home_accept_button))
                }
            }
        }
    }
}

@Composable
internal fun ActiveTripItem(trip: Trip, onViewTrip: () -> Unit) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = {
                Text(stringResource(Res.string.driver_home_trip_id_label, trip.id.take(8)))
            },
            supportingContent = {
                TripStatusBadge(status = trip.status)
            },
            trailingContent = {
                Button(onClick = onViewTrip) {
                    Text(stringResource(Res.string.driver_home_view_trip_button))
                }
            }
        )
    }
}

@Preview
@Composable
internal fun ActiveTripItemPreview() {
    TuCargoTheme {
        ActiveTripItem(trip = Trip(id = "trip-abc-123-def"), onViewTrip = {})
    }
}
