package com.juanpablo0612.tucargo.core.ui.components

import org.jetbrains.compose.resources.stringResource
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.map_your_location
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
actual fun MapComponent(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float
) {
    val userLocation = remember(latitude, longitude) {
        LatLng(latitude, longitude)
    }

    // Persistencia del estado del marcador (Corrige el error de 'state object during composition')
    val markerState = rememberMarkerState(position = userLocation)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, zoom)
    }

    // Efecto para centrar el mapa cuando la ubicación cambia (Ideal para rastreo en logística)
    LaunchedEffect(userLocation) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(userLocation, zoom),
            durationMs = 1000
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = remember {
            MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        }
    ) {
        Marker(
            state = markerState,
            title = stringResource(Res.string.map_your_location)
        )
    }
}
