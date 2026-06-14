package com.juanpablo0612.tucargo.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.juanpablo0612.tucargo.core.location.DriverLocation
import org.jetbrains.compose.resources.stringResource
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.map_your_location

private const val ANIMATION_DURATION_MS = 1500
private const val BOUNDS_PADDING_PX = 120

@Composable
actual fun MapComponent(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float,
    onMapClick: ((latitude: Double, longitude: Double) -> Unit)?,
    driverLocation: DriverLocation?,
    originLatLng: Pair<Double, Double>?,
    destinationLatLng: Pair<Double, Double>?,
) {
    val staticLocation = remember(latitude, longitude) { LatLng(latitude, longitude) }

    // Animatable lat/lng for smooth driver marker movement
    val animLat = remember { Animatable(driverLocation?.lat?.toFloat() ?: latitude.toFloat()) }
    val animLng = remember { Animatable(driverLocation?.lng?.toFloat() ?: longitude.toFloat()) }

    LaunchedEffect(driverLocation) {
        val loc = driverLocation ?: return@LaunchedEffect
        launch { animLat.animateTo(loc.lat.toFloat(), tween(ANIMATION_DURATION_MS)) }
        animLng.animateTo(loc.lng.toFloat(), tween(ANIMATION_DURATION_MS))
    }

    val driverMarkerState = rememberMarkerState(
        position = LatLng(animLat.value.toDouble(), animLng.value.toDouble())
    )
    LaunchedEffect(animLat.value, animLng.value) {
        driverMarkerState.position = LatLng(animLat.value.toDouble(), animLng.value.toDouble())
    }

    // Static marker (used when no driverLocation is active)
    val staticMarkerState = rememberMarkerState(position = staticLocation)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(staticLocation, zoom)
    }

    var hasSetInitialBounds by remember { mutableStateOf(false) }

    LaunchedEffect(driverLocation, originLatLng, destinationLatLng) {
        if (!hasSetInitialBounds && driverLocation != null && originLatLng != null && destinationLatLng != null) {
            val bounds = LatLngBounds.builder()
                .include(LatLng(driverLocation.lat, driverLocation.lng))
                .include(LatLng(originLatLng.first, originLatLng.second))
                .include(LatLng(destinationLatLng.first, destinationLatLng.second))
                .build()
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, BOUNDS_PADDING_PX)
                )
                hasSetInitialBounds = true
            } catch (_: Exception) {
                // bounds too small or map not yet sized; skip silently
            }
        } else if (driverLocation == null && !hasSetInitialBounds) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(staticLocation, zoom),
                durationMs = 1000
            )
        }
    }

    val markerTitle = stringResource(Res.string.map_your_location)

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapClick = onMapClick?.let { callback ->
            { latLng -> callback(latLng.latitude, latLng.longitude) }
        },
        uiSettings = remember {
            MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        }
    ) {
        if (driverLocation != null) {
            Marker(state = driverMarkerState, title = "Conductor")
            originLatLng?.let { (lat, lng) ->
                Marker(state = rememberMarkerState(position = LatLng(lat, lng)), title = "Origen")
            }
            destinationLatLng?.let { (lat, lng) ->
                Marker(state = rememberMarkerState(position = LatLng(lat, lng)), title = "Destino")
            }
        } else {
            Marker(state = staticMarkerState, title = markerTitle)
        }
    }
}
