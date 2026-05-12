package com.juanpablo0612.tucargo.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun MapComponent(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Float
) {
    // Placeholder para iOS para satisfacer la declaración 'expect'
    // En una fase posterior, se integrará con Google Maps SDK para iOS o MapKit vía interop
    Box(
        modifier = modifier.fillMaxSize().background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text("Mapa no disponible en iOS (Simulador/Actual)")
    }
}
