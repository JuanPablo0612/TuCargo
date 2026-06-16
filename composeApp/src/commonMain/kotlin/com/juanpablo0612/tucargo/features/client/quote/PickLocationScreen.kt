package com.juanpablo0612.tucargo.features.client.quote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.MapComponent
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.pick_dest_title
import tucargo.composeapp.generated.resources.pick_location_confirm_button
import tucargo.composeapp.generated.resources.pick_origin_title

private const val DEFAULT_LAT = 4.6097
private const val DEFAULT_LNG = -74.0817

@Composable
fun PickOriginScreen(
    viewModel: TripRequestViewModel = koinViewModel(),
    onConfirmed: () -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PickLocationScreenContent(
        title = stringResource(Res.string.pick_origin_title),
        initialLat = state.originLat,
        initialLng = state.originLng,
        initialAddr = state.originAddr,
        onConfirm = { lat, lng, addr ->
            viewModel.confirmOrigin(lat, lng, addr)
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

    PickLocationScreenContent(
        title = stringResource(Res.string.pick_dest_title),
        initialLat = state.destLat,
        initialLng = state.destLng,
        initialAddr = state.destAddr,
        onConfirm = { lat, lng, addr ->
            viewModel.confirmDest(lat, lng, addr)
            onConfirmed()
        },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickLocationScreenContent(
    title: String,
    initialLat: Double?,
    initialLng: Double?,
    initialAddr: String,
    onConfirm: (Double, Double, String) -> Unit,
    onBackClick: () -> Unit
) {
    var pickedLat by remember { mutableStateOf(initialLat ?: DEFAULT_LAT) }
    var pickedLng by remember { mutableStateOf(initialLng ?: DEFAULT_LNG) }
    var addressText by remember { mutableStateOf(initialAddr) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
                latitude = pickedLat,
                longitude = pickedLng,
                onMapClick = { lat, lng ->
                    pickedLat = lat
                    pickedLng = lng
                }
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = addressText,
                    onValueChange = { addressText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(title) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }

            Button(
                onClick = { onConfirm(pickedLat, pickedLng, addressText) },
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
