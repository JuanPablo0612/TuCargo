package com.juanpablo0612.tucargo.features.driver.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.components.TripStatusBadge
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import com.juanpablo0612.tucargo.data.trip.Trip
import com.juanpablo0612.tucargo.features.driver.home.presentation.components.AvailabilityButton
import com.juanpablo0612.tucargo.features.driver.home.presentation.components.BalanceCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.client_home_sign_out_desc
import tucargo.composeapp.generated.resources.driver_home_active_desc
import tucargo.composeapp.generated.resources.driver_home_active_trips_title
import tucargo.composeapp.generated.resources.driver_home_empty_trips_message
import tucargo.composeapp.generated.resources.driver_home_offline_desc
import tucargo.composeapp.generated.resources.driver_home_availability_error
import tucargo.composeapp.generated.resources.driver_home_load_error
import tucargo.composeapp.generated.resources.driver_home_title
import tucargo.composeapp.generated.resources.driver_home_trip_id_label
import tucargo.composeapp.generated.resources.driver_home_view_trip_button

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    viewModel: DriverHomeViewModel = koinViewModel(),
    onSignOut: () -> Unit,
    onTripClick: (tripId: String) -> Unit = {},
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
                        onToggle = { viewModel.toggleAvailability(it) },
                    )
                }

                item(key = "balance_card", contentType = "balance") {
                    BalanceCard(
                        balance = state.balance,
                        totalTrips = state.totalTrips
                    )
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
