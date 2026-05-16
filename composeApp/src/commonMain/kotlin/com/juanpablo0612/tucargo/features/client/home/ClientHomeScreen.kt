package com.juanpablo0612.tucargo.features.client.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.components.MapComponent
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import com.juanpablo0612.tucargo.data.trip.Trip
import com.juanpablo0612.tucargo.data.trip.TripStatus
import com.juanpablo0612.tucargo.data.user.User
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.arrow_forward
import tucargo.composeapp.generated.resources.client_home_default_user_name
import tucargo.composeapp.generated.resources.client_home_empty_trips_subtitle
import tucargo.composeapp.generated.resources.client_home_empty_trips_title
import tucargo.composeapp.generated.resources.client_home_greeting_afternoon
import tucargo.composeapp.generated.resources.client_home_greeting_evening
import tucargo.composeapp.generated.resources.client_home_greeting_morning
import tucargo.composeapp.generated.resources.client_home_load_error
import tucargo.composeapp.generated.resources.client_home_new_trip_button
import tucargo.composeapp.generated.resources.client_home_trips_error
import tucargo.composeapp.generated.resources.client_home_recent_trips_title
import tucargo.composeapp.generated.resources.client_home_sign_out_desc
import tucargo.composeapp.generated.resources.client_home_stats_status_active
import tucargo.composeapp.generated.resources.client_home_stats_status_label
import tucargo.composeapp.generated.resources.client_home_stats_trips_label
import tucargo.composeapp.generated.resources.client_home_trip_destination_empty
import tucargo.composeapp.generated.resources.client_home_trip_origin_empty
import tucargo.composeapp.generated.resources.client_home_view_all_button
import tucargo.composeapp.generated.resources.client_home_your_location_title
import tucargo.composeapp.generated.resources.local_shipping
import tucargo.composeapp.generated.resources.package_2
import tucargo.composeapp.generated.resources.trip_status_arrived_pickup
import tucargo.composeapp.generated.resources.trip_status_assigned
import tucargo.composeapp.generated.resources.trip_status_cancelled
import tucargo.composeapp.generated.resources.trip_status_completed
import tucargo.composeapp.generated.resources.trip_status_in_progress
import tucargo.composeapp.generated.resources.trip_status_on_way
import tucargo.composeapp.generated.resources.trip_status_searching
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ClientHomeScreen(
    viewModel: ClientHomeViewModel = koinViewModel(),
    onNewTrip: () -> Unit = {},
    onSignOut: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ClientHomeScreenContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNewTrip = onNewTrip
    ) {
        onSignOut()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ClientHomeScreenContent(
    uiState: ClientHomeState,
    onAction: (ClientHomeAction) -> Unit,
    onNewTrip: () -> Unit,
    onSignOut: () -> Unit
) {
    Scaffold(
        topBar = {
            ClientTopAppBar(
                user = uiState.user,
                onSignOut = {
                    onAction(ClientHomeAction.SignOut)
                    onSignOut()
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoadingTrips,
            onRefresh = { onAction(ClientHomeAction.RefreshTrips) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    uiState.error?.let { clientError ->
                        item(key = "error_banner", contentType = "error") {
                            val errorRes = when (clientError) {
                                ClientHomeError.LoadUserError -> Res.string.client_home_load_error
                                ClientHomeError.LoadTripsError -> Res.string.client_home_trips_error
                            }
                            ErrorCard(
                                message = stringResource(errorRes),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    item(key = "greeting", contentType = "greeting") {
                        GreetingSection(
                            userName = uiState.user.fullName,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    item(key = "stats", contentType = "stats") {
                        StatsCard(
                            tripsCount = uiState.recentTrips.size,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }

                    item(key = "new_trip_button", contentType = "action") {
                        Button(
                            onClick = onNewTrip,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(painterResource(Res.drawable.package_2), contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(Res.string.client_home_new_trip_button),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 6.dp)
                            )
                            Icon(painterResource(Res.drawable.arrow_forward), contentDescription = null)
                        }
                    }

                    item(key = "map", contentType = "map") {
                        MapSection(
                            latitude = uiState.userLatitude,
                            longitude = uiState.userLongitude,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }

                    item(key = "trips_header", contentType = "header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.client_home_recent_trips_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.semantics { heading() }
                            )
                            TextButton(onClick = { }) {
                                Text(stringResource(Res.string.client_home_view_all_button))
                            }
                        }
                    }

                    if (uiState.isLoadingTrips) {
                        item(key = "loading", contentType = "loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            }
                        }
                    } else if (uiState.recentTrips.isEmpty()) {
                        item(key = "empty_trips", contentType = "empty") {
                            EmptyTripsSection(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    } else {
                        items(
                            items = uiState.recentTrips,
                            key = { it.id },
                            contentType = { "trip_card" }
                        ) { trip ->
                            TripCard(
                                trip = trip,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                    item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientTopAppBar(user: User, onSignOut: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.fullName
                            .split(" ")
                            .asSequence()
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                            .joinToString("")
                            .ifEmpty { "U" },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "TuCargo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user.fullName.ifEmpty { user.email },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onSignOut) {
                Icon(
                    painter = painterResource(Res.drawable.arrow_forward),
                    contentDescription = stringResource(Res.string.client_home_sign_out_desc)
                )
            }
        }
    )
}

@Composable
private fun GreetingSection(userName: String, modifier: Modifier = Modifier) {
    val now = Clock.System.now()
    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(now.toEpochMilliseconds())
    val greetingRes = when (instant.toLocalDateTime(TimeZone.currentSystemDefault()).hour) {
        in 0..11 -> Res.string.client_home_greeting_morning
        in 12..17 -> Res.string.client_home_greeting_afternoon
        else -> Res.string.client_home_greeting_evening
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(greetingRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (userName.isNotEmpty())
                userName.split(" ").first()
            else
                stringResource(Res.string.client_home_default_user_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatsCard(tripsCount: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(
                value = tripsCount.toString(),
                label = stringResource(Res.string.client_home_stats_trips_label)
            )
            StatItem(
                value = stringResource(Res.string.client_home_stats_status_active),
                label = stringResource(Res.string.client_home_stats_status_label)
            )
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun MapSection(latitude: Double, longitude: Double, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.client_home_your_location_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(shape = MaterialTheme.shapes.large) {
            MapComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                latitude = latitude,
                longitude = longitude
            )
        }
    }
}

@Composable
private fun TripCard(trip: Trip, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.local_shipping),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.origin.address.ifEmpty {
                        stringResource(Res.string.client_home_trip_origin_empty)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "→ ${trip.destination.address.ifEmpty { stringResource(Res.string.client_home_trip_destination_empty) }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TripStatusBadge(status = trip.status)
        }
    }
}

@Composable
private fun TripStatusBadge(status: TripStatus) {
    val (bg, fg) = when (status) {
        TripStatus.SEARCHING -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        TripStatus.ASSIGNED, TripStatus.ON_WAY, TripStatus.ARRIVED_PICKUP -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        TripStatus.IN_PROGRESS -> Pair(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        TripStatus.COMPLETED -> Pair(Color(0xFFD4EDDA), Color(0xFF155724))
        TripStatus.CANCELLED -> Pair(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
    }
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(status.toDisplayNameRes()),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

private fun TripStatus.toDisplayNameRes() = when (this) {
    TripStatus.SEARCHING -> Res.string.trip_status_searching
    TripStatus.ASSIGNED -> Res.string.trip_status_assigned
    TripStatus.ON_WAY -> Res.string.trip_status_on_way
    TripStatus.ARRIVED_PICKUP -> Res.string.trip_status_arrived_pickup
    TripStatus.IN_PROGRESS -> Res.string.trip_status_in_progress
    TripStatus.COMPLETED -> Res.string.trip_status_completed
    TripStatus.CANCELLED -> Res.string.trip_status_cancelled
}

@Composable
private fun EmptyTripsSection(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(Res.drawable.package_2),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.client_home_empty_trips_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(Res.string.client_home_empty_trips_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview
@Composable
internal fun ClientHomeScreenContentPreview() {
    TuCargoTheme {
        ClientHomeScreenContent(
            uiState = ClientHomeState(),
            onAction = {},
            onNewTrip = {},
            onSignOut = {}
        )
    }
}
