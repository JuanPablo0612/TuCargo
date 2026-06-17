package com.juanpablo0612.tucargo.features.client.quote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ResponsiveContainer
import com.juanpablo0612.tucargo.core.ui.theme.LocalDimensions
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.components.MapComponent
import com.juanpablo0612.tucargo.core.util.roundToDecimals
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.quote_commission_note
import tucargo.composeapp.generated.resources.quote_error_no_route
import tucargo.composeapp.generated.resources.quote_error_out_of_range
import tucargo.composeapp.generated.resources.quote_error_same_origin
import tucargo.composeapp.generated.resources.quote_error_service_unavailable
import tucargo.composeapp.generated.resources.quote_expired
import tucargo.composeapp.generated.resources.quote_new_button
import tucargo.composeapp.generated.resources.quote_request_button
import tucargo.composeapp.generated.resources.quote_title
import tucargo.composeapp.generated.resources.quote_valid_for
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteScreen(
    viewModel: TripRequestViewModel = koinViewModel(),
    onTripCreated: (tripId: String) -> Unit,
    onNewQuote: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createdTripId) {
        uiState.createdTripId?.let { tripId ->
            onTripCreated(tripId)
            viewModel.onNavigated()
        }
    }

    val quote = uiState.quote
    if (quote == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.quote_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.error != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        val msg = when (uiState.error) {
                            QuoteError.SAME_ORIGIN_DEST -> stringResource(Res.string.quote_error_same_origin)
                            QuoteError.QUOTE_OUT_OF_RANGE -> stringResource(Res.string.quote_error_out_of_range)
                            QuoteError.NO_ROUTE -> stringResource(Res.string.quote_error_no_route)
                            QuoteError.SERVICE_UNAVAILABLE -> stringResource(Res.string.quote_error_service_unavailable)
                            else -> stringResource(Res.string.quote_error_service_unavailable)
                        }
                        ErrorCard(message = msg, modifier = Modifier.fillMaxWidth())
                        Button(onClick = onNewQuote, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(Res.string.quote_new_button))
                        }
                    }
                } else {
                    CircularProgressIndicator()
                }
            }
        }
        return
    }

    var remainingMs by remember(quote.id) {
        mutableLongStateOf(
            quote.validUntil - kotlin.time.Clock.System.now().toEpochMilliseconds()
        )
    }
    val isExpired = remainingMs <= 0

    LaunchedEffect(quote.id) {
        while (remainingMs > 0) {
            delay(1000L.milliseconds)
            remainingMs = quote.validUntil - kotlin.time.Clock.System.now().toEpochMilliseconds()
        }
    }

    val minutes = (remainingMs / 1000 / 60).coerceAtLeast(0)
    val seconds = (remainingMs / 1000 % 60).coerceAtLeast(0)
    val countdownText = "$minutes:${seconds.toString().padStart(2, '0')}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.quote_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        val dimensions = LocalDimensions.current
        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensions.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(shape = MaterialTheme.shapes.large) {
                MapComponent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensions.mapHeight),
                    latitude = quote.originLat,
                    longitude = quote.originLng,
                    onMapClick = null
                )
            }

            Text(
                text = "${quote.originAddr} → ${quote.destAddr}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${quote.distanceKm.roundToDecimals(1)} km",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = quote.totalPrice.format(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isExpired) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(
                            Res.string.quote_commission_note,
                            quote.commissionFee.format()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    if (!isExpired) {
                        Text(
                            text = stringResource(Res.string.quote_valid_for, countdownText),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.quote_expired),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            uiState.error?.let { error ->
                val msg = when (error) {
                    QuoteError.SAME_ORIGIN_DEST -> stringResource(Res.string.quote_error_same_origin)
                    QuoteError.QUOTE_OUT_OF_RANGE -> stringResource(Res.string.quote_error_out_of_range)
                    QuoteError.NO_ROUTE -> stringResource(Res.string.quote_error_no_route)
                    QuoteError.SERVICE_UNAVAILABLE -> stringResource(Res.string.quote_error_service_unavailable)
                    else -> null
                }
                msg?.let { ErrorCard(message = it, modifier = Modifier.fillMaxWidth()) }
            }

            if (isExpired) {
                Button(
                    onClick = onNewQuote,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.quote_new_button))
                }
            } else {
                Button(
                    onClick = {
                        viewModel.requestTrip(
                            quoteId = quote.id,
                            cargoDescription = uiState.cargoDescription,
                            weightConfirmed = uiState.weightConfirmed
                        )
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    }
                    Text(stringResource(Res.string.quote_request_button))
                }
            }
        }
        }
    }
}
