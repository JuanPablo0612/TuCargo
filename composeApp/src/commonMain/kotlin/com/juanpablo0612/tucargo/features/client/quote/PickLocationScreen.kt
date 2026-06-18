package com.juanpablo0612.tucargo.features.client.quote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import org.jetbrains.compose.resources.painterResource
import tucargo.composeapp.generated.resources.arrow_back
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.MapComponent
import com.juanpablo0612.tucargo.domain.model.PlacePrediction
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.pick_dest_title
import tucargo.composeapp.generated.resources.pick_location_confirm_button
import tucargo.composeapp.generated.resources.pick_origin_title
import tucargo.composeapp.generated.resources.place_search_hint
import tucargo.composeapp.generated.resources.close
import tucargo.composeapp.generated.resources.place_clear_search

private const val DEFAULT_LAT = 4.6097
private const val DEFAULT_LNG = -74.0817

@Composable
fun PickOriginScreen(
    viewModel: TripRequestViewModel = koinViewModel(),
    onConfirmed: () -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        viewModel.resetSessionToken()
        onDispose { viewModel.clearAutocomplete() }
    }

    PickLocationScreenContent(
        title = stringResource(Res.string.pick_origin_title),
        lat = state.originLat ?: DEFAULT_LAT,
        lng = state.originLng ?: DEFAULT_LNG,
        addressText = state.originAddr,
        predictions = state.predictions,
        isAutocompleteLoading = state.isAutocompleteLoading,
        isReverseGeocoding = state.isReverseGeocoding,
        onAddressChanged = { viewModel.onAutocompleteQueryChanged(it, isOrigin = true) },
        onPredictionSelected = { viewModel.onPredictionSelected(it, isOrigin = true) },
        onMapClick = { lat, lng -> viewModel.onMapTapped(lat, lng, isOrigin = true) },
        onClearSearch = { viewModel.clearAutocomplete() },
        onConfirm = {
            viewModel.confirmOrigin(
                state.originLat ?: DEFAULT_LAT,
                state.originLng ?: DEFAULT_LNG,
                state.originAddr
            )
            onConfirmed()
        },
        onBackClick = onBackClick
    )
}

@Composable
fun PickDestScreen(
    viewModel: TripRequestViewModel = koinViewModel(),
    onConfirmed: () -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        viewModel.resetSessionToken()
        onDispose { viewModel.clearAutocomplete() }
    }

    PickLocationScreenContent(
        title = stringResource(Res.string.pick_dest_title),
        lat = state.destLat ?: DEFAULT_LAT,
        lng = state.destLng ?: DEFAULT_LNG,
        addressText = state.destAddr,
        predictions = state.predictions,
        isAutocompleteLoading = state.isAutocompleteLoading,
        isReverseGeocoding = state.isReverseGeocoding,
        onAddressChanged = { viewModel.onAutocompleteQueryChanged(it, isOrigin = false) },
        onPredictionSelected = { viewModel.onPredictionSelected(it, isOrigin = false) },
        onMapClick = { lat, lng -> viewModel.onMapTapped(lat, lng, isOrigin = false) },
        onClearSearch = { viewModel.clearAutocomplete() },
        onConfirm = {
            viewModel.confirmDest(
                state.destLat ?: DEFAULT_LAT,
                state.destLng ?: DEFAULT_LNG,
                state.destAddr
            )
            onConfirmed()
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickLocationScreenContent(
    title: String,
    lat: Double,
    lng: Double,
    addressText: String,
    predictions: List<PlacePrediction>,
    isAutocompleteLoading: Boolean,
    isReverseGeocoding: Boolean,
    onAddressChanged: (String) -> Unit,
    onPredictionSelected: (PlacePrediction) -> Unit,
    onMapClick: (Double, Double) -> Unit,
    onClearSearch: () -> Unit,
    onConfirm: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(painterResource(Res.drawable.arrow_back), contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MapComponent(
                modifier = Modifier.fillMaxSize(),
                latitude = lat,
                longitude = lng,
                onMapClick = onMapClick
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(8.dp)
                ) {
                    OutlinedTextField(
                        value = addressText,
                        onValueChange = onAddressChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.place_search_hint)) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        trailingIcon = {
                            if (isReverseGeocoding) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else if (addressText.isNotEmpty()) {
                                IconButton(onClick = {
                                    onClearSearch()
                                    onAddressChanged("")
                                }) {
                                    Icon(
                                        painterResource(Res.drawable.close),
                                        contentDescription = stringResource(Res.string.place_clear_search),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    )

                    if (isAutocompleteLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                    }
                }

                if (predictions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(predictions, key = { it.placeId }) { prediction ->
                                PredictionItem(
                                    prediction = prediction,
                                    onClick = { onPredictionSelected(prediction) }
                                )
                                if (prediction != predictions.last()) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onConfirm,
                enabled = addressText.isNotBlank(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(stringResource(Res.string.pick_location_confirm_button))
            }
        }
    }
}

@Composable
private fun PredictionItem(
    prediction: PlacePrediction,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prediction.mainText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (prediction.secondaryText.isNotEmpty()) {
                Text(
                    text = prediction.secondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
