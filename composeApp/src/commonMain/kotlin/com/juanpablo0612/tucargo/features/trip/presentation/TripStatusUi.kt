package com.juanpablo0612.tucargo.features.trip.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.juanpablo0612.tucargo.core.ui.theme.LocalExtendedColors
import com.juanpablo0612.tucargo.domain.model.TripStatus
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.MaterialTheme
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.trip_status_arrived_pickup
import tucargo.composeapp.generated.resources.trip_status_assigned
import tucargo.composeapp.generated.resources.trip_status_cancelled
import tucargo.composeapp.generated.resources.trip_status_completed
import tucargo.composeapp.generated.resources.trip_status_in_progress
import tucargo.composeapp.generated.resources.trip_status_on_way
import tucargo.composeapp.generated.resources.trip_status_searching

@Composable
fun TripStatus.displayName(): String = stringResource(this.toDisplayNameRes())

@Composable
fun TripStatus.displayContainerColor(): Color {
    val extendedColors = LocalExtendedColors.current
    return when (this) {
        TripStatus.SEARCHING -> MaterialTheme.colorScheme.secondaryContainer
        TripStatus.ASSIGNED, TripStatus.ON_WAY, TripStatus.ARRIVED_PICKUP -> MaterialTheme.colorScheme.primaryContainer
        TripStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer
        TripStatus.COMPLETED -> extendedColors.successContainer
        TripStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
    }
}

@Composable
fun TripStatus.onDisplayContainerColor(): Color {
    val extendedColors = LocalExtendedColors.current
    return when (this) {
        TripStatus.SEARCHING -> MaterialTheme.colorScheme.onSecondaryContainer
        TripStatus.ASSIGNED, TripStatus.ON_WAY, TripStatus.ARRIVED_PICKUP -> MaterialTheme.colorScheme.onPrimaryContainer
        TripStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onTertiaryContainer
        TripStatus.COMPLETED -> extendedColors.onSuccessContainer
        TripStatus.CANCELLED -> MaterialTheme.colorScheme.onErrorContainer
    }
}

internal fun TripStatus.toDisplayNameRes(): StringResource = when (this) {
    TripStatus.SEARCHING -> Res.string.trip_status_searching
    TripStatus.ASSIGNED -> Res.string.trip_status_assigned
    TripStatus.ON_WAY -> Res.string.trip_status_on_way
    TripStatus.ARRIVED_PICKUP -> Res.string.trip_status_arrived_pickup
    TripStatus.IN_PROGRESS -> Res.string.trip_status_in_progress
    TripStatus.COMPLETED -> Res.string.trip_status_completed
    TripStatus.CANCELLED -> Res.string.trip_status_cancelled
}
