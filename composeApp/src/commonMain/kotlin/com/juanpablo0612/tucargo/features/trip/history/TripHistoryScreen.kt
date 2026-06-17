package com.juanpablo0612.tucargo.features.trip.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import org.jetbrains.compose.resources.painterResource
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ResponsiveContainer
import com.juanpablo0612.tucargo.core.ui.theme.LocalDimensions
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.components.TripCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.arrow_back
import tucargo.composeapp.generated.resources.trip_history_back_button
import tucargo.composeapp.generated.resources.trip_history_empty
import tucargo.composeapp.generated.resources.trip_history_load_error
import tucargo.composeapp.generated.resources.trip_history_title

@Composable
fun TripHistoryScreen(
    onTripClick: (tripId: String) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: TripHistoryViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TripHistoryScreenContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onTripClick = onTripClick,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripHistoryScreenContent(
    uiState: TripHistoryState,
    onAction: (TripHistoryAction) -> Unit,
    onTripClick: (tripId: String) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.trip_history_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back),
                            contentDescription = stringResource(Res.string.trip_history_back_button),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { onAction(TripHistoryAction.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val dimensions = LocalDimensions.current
            ResponsiveContainer(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading && uiState.trips.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                uiState.error != null -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorCard(
                        message = stringResource(Res.string.trip_history_load_error),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                uiState.trips.isEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                    ) {
                        Text(
                            text = stringResource(Res.string.trip_history_empty),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(dimensions.screenHorizontalPadding),
                ) {
                    items(
                        items = uiState.trips,
                        key = { it.id },
                        contentType = { "trip_card" }
                    ) { trip ->
                        TripCard(
                            trip = trip,
                            onClick = { onTripClick(trip.id) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            }
        }
    }
}
