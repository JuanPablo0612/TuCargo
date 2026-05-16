package com.juanpablo0612.tucargo.features.driver.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import com.juanpablo0612.tucargo.features.driver.home.presentation.components.AvailabilityButton
import com.juanpablo0612.tucargo.features.driver.home.presentation.components.BalanceCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.driver_home_active_desc
import tucargo.composeapp.generated.resources.driver_home_active_trips_title
import tucargo.composeapp.generated.resources.driver_home_empty_trips_message
import tucargo.composeapp.generated.resources.driver_home_offline_desc
import tucargo.composeapp.generated.resources.driver_home_sign_out_button
import tucargo.composeapp.generated.resources.driver_home_title
import tucargo.composeapp.generated.resources.driver_home_trip_id_label
import tucargo.composeapp.generated.resources.driver_home_trip_status_in_progress
import tucargo.composeapp.generated.resources.driver_home_view_trip_button

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    viewModel: DriverHomeViewModel = koinViewModel(),
    onSignOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Column {
                        Text(
                            text = stringResource(Res.string.driver_home_title),
                            style = MaterialTheme.typography.titleMedium
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
                                MaterialTheme.colorScheme.error
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Text(
                            text = stringResource(Res.string.driver_home_sign_out_button),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item(key = "availability_button", contentType = "availability") {
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))
                    AvailabilityButton(
                        isAvailable = state.isAvailable,
                        onToggle = { viewModel.toggleAvailability(it) }
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
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = stringResource(Res.string.driver_home_empty_trips_message),
                                modifier = Modifier.padding(24.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(
                        items = state.activeTrips,
                        key = { it.id },
                        contentType = { "trip_item" }
                    ) { trip ->
                        ActiveTripItem(trip.id)
                    }
                }
            }
        }
    }
}

@Composable
internal fun ActiveTripItem(tripId: String) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = {
                Text(stringResource(Res.string.driver_home_trip_id_label, tripId.take(8)))
            },
            supportingContent = {
                Text(stringResource(Res.string.driver_home_trip_status_in_progress))
            },
            trailingContent = {
                Button(onClick = { /* Navigate to details */ }) {
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
        ActiveTripItem("trip-abc-123-def")
    }
}
