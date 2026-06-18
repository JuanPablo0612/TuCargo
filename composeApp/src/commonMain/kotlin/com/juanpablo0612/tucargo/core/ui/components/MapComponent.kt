package com.juanpablo0612.tucargo.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.juanpablo0612.tucargo.core.location.DriverLocation

@Composable
expect fun MapComponent(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float = 15f,
    onMapClick: ((latitude: Double, longitude: Double) -> Unit)? = null,
    driverLocation: DriverLocation? = null,
    originLatLng: Pair<Double, Double>? = null,
    destinationLatLng: Pair<Double, Double>? = null,
    myLocationEnabled: Boolean = false,
)
