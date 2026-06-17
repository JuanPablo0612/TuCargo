package com.juanpablo0612.tucargo.features.trip.completed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ResponsiveContainer
import com.juanpablo0612.tucargo.core.ui.theme.LocalDimensions
import com.juanpablo0612.tucargo.core.ui.toCurrencyString
import com.juanpablo0612.tucargo.core.ui.toDistanceString
import com.juanpablo0612.tucargo.domain.model.Trip
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.trip_completed_balance_placeholder
import tucargo.composeapp.generated.resources.trip_completed_commission
import tucargo.composeapp.generated.resources.trip_completed_delivered_at
import tucargo.composeapp.generated.resources.trip_completed_earnings
import tucargo.composeapp.generated.resources.trip_completed_home_button
import tucargo.composeapp.generated.resources.trip_completed_rate_placeholder
import tucargo.composeapp.generated.resources.trip_completed_rating_placeholder
import tucargo.composeapp.generated.resources.trip_completed_title_client
import tucargo.composeapp.generated.resources.trip_completed_title_driver
import tucargo.composeapp.generated.resources.trip_detail_cargo_label
import tucargo.composeapp.generated.resources.trip_detail_destination_label
import tucargo.composeapp.generated.resources.trip_detail_origin_label
import tucargo.composeapp.generated.resources.trip_detail_price_label

@Composable
fun TripCompletedScreen(
    tripId: String,
    onDriverHomeClick: () -> Unit,
    onClientHomeClick: () -> Unit,
) {
    val viewModel: TripCompletedViewModel = koinViewModel { parametersOf(tripId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TripCompletedContent(
        uiState = uiState,
        onHomeClick = if (uiState.isDriver) onDriverHomeClick else onClientHomeClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripCompletedContent(
    uiState: TripCompletedState,
    onHomeClick: () -> Unit,
) {
    val titleRes = if (uiState.isDriver) Res.string.trip_completed_title_driver
    else Res.string.trip_completed_title_client

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(titleRes),
                        modifier = Modifier.semantics { heading() },
                    )
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
                ) { CircularProgressIndicator() }

                uiState.trip == null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(onClick = onHomeClick) {
                        Text(stringResource(Res.string.trip_completed_home_button))
                    }
                }

                uiState.isDriver -> DriverCompletedBody(
                    trip = uiState.trip,
                    onHomeClick = onHomeClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimensions.formHorizontalPadding)
                        .verticalScroll(rememberScrollState()),
                )

                else -> ClientCompletedBody(
                    trip = uiState.trip,
                    onHomeClick = onHomeClick,
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
private fun DriverCompletedBody(
    trip: Trip,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val net = trip.priceTotal - trip.commissionFee
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.trip_completed_earnings, net.toDouble().toCurrencyString()),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(
                Res.string.trip_completed_commission,
                trip.commissionFee.toDouble().toCurrencyString()
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider()
        Text(
            text = stringResource(Res.string.trip_completed_balance_placeholder),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.trip_completed_rating_placeholder),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onHomeClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.trip_completed_home_button))
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ClientCompletedBody(
    trip: Trip,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        CompletedSection(label = stringResource(Res.string.trip_detail_origin_label)) {
            Text(text = trip.origin.address, style = MaterialTheme.typography.bodyMedium)
        }
        CompletedSection(label = stringResource(Res.string.trip_detail_destination_label)) {
            Text(text = trip.destination.address, style = MaterialTheme.typography.bodyMedium)
        }
        CompletedSection(label = stringResource(Res.string.trip_detail_cargo_label)) {
            Text(
                text = "${trip.distanceKm.toDistanceString()} km",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        CompletedSection(label = stringResource(Res.string.trip_detail_price_label)) {
            Text(
                text = trip.priceTotal.toDouble().toCurrencyString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        trip.completedAt?.let { ts ->
            CompletedSection(
                label = stringResource(Res.string.trip_completed_delivered_at, formatTimestamp(ts))
            ) {}
        }
        HorizontalDivider()
        Text(
            text = stringResource(Res.string.trip_completed_rate_placeholder),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onHomeClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.trip_completed_home_button))
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CompletedSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        content()
    }
}

private fun formatTimestamp(epochMs: Long): String {
    val totalSeconds = epochMs / 1000
    val daySeconds = 86400
    val totalDays = totalSeconds / daySeconds

    val year = 1970
    val months = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    var remainingDays = totalDays.toInt()
    var y = year
    while (true) {
        val daysInYear = if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) 366 else 365
        if (remainingDays < daysInYear) break
        remainingDays -= daysInYear
        y++
    }
    val leapYear = y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)
    if (leapYear) months[1] = 29
    var m = 0
    while (remainingDays >= months[m]) {
        remainingDays -= months[m]
        m++
    }
    val d = remainingDays + 1

    val secondsInDay = totalSeconds % daySeconds
    val hours = secondsInDay / 3600
    val minutes = (secondsInDay % 3600) / 60

    return "${d.toString().padStart(2, '0')}/${(m + 1).toString().padStart(2, '0')}/$y ${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
}
