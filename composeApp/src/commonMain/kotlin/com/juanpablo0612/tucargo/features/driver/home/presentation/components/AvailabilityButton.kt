package com.juanpablo0612.tucargo.features.driver.home.presentation.components

import org.jetbrains.compose.resources.stringResource
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.driver_home_availability_go_online
import tucargo.composeapp.generated.resources.driver_home_availability_online
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AvailabilityButton(
    isAvailable: Boolean, // Estado de disponibilidad[cite: 1]
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animación de color: Verde si está disponible, Gris si no[cite: 1]
    val backgroundColor by animateColorAsState(
        if (isAvailable) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant
    )
    val contentColor by animateColorAsState(
        if (isAvailable) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    )

    Button(
        onClick = { onToggle(!isAvailable) }, // Cambiar disponibilidad[cite: 1]
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = null
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (isAvailable) stringResource(Res.string.driver_home_availability_online) else stringResource(Res.string.driver_home_availability_go_online),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}