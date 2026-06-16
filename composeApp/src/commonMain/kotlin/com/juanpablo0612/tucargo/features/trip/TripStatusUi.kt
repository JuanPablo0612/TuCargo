package com.juanpablo0612.tucargo.features.trip

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.juanpablo0612.tucargo.core.ui.theme.LocalExtendedColors
import com.juanpablo0612.tucargo.domain.model.TripStatus
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.MaterialTheme
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.trip_status_accepted
import tucargo.composeapp.generated.resources.trip_status_at_dropoff
import tucargo.composeapp.generated.resources.trip_status_at_pickup
import tucargo.composeapp.generated.resources.trip_status_cancelled
import tucargo.composeapp.generated.resources.trip_status_cancelled_admin
import tucargo.composeapp.generated.resources.trip_status_cancelled_client
import tucargo.composeapp.generated.resources.trip_status_cancelled_driver
import tucargo.composeapp.generated.resources.trip_status_cancelled_no_driver
import tucargo.composeapp.generated.resources.trip_status_completed
import tucargo.composeapp.generated.resources.trip_status_in_transit
import tucargo.composeapp.generated.resources.trip_status_offered
import tucargo.composeapp.generated.resources.trip_status_requested

@Composable
fun TripStatus.displayName(): String = stringResource(this.toDisplayNameRes())

@Composable
fun TripStatus.displayContainerColor(): Color {
    val extendedColors = LocalExtendedColors.current
    return when (this) {
        TripStatus.REQUESTED, TripStatus.OFFERED -> MaterialTheme.colorScheme.secondaryContainer
        TripStatus.ACCEPTED, TripStatus.AT_PICKUP, TripStatus.IN_TRANSIT -> MaterialTheme.colorScheme.primaryContainer
        TripStatus.AT_DROPOFF -> MaterialTheme.colorScheme.tertiaryContainer
        TripStatus.COMPLETED -> extendedColors.successContainer
        TripStatus.CANCELLED_NO_DRIVER,
        TripStatus.CANCELLED_CLIENT,
        TripStatus.CANCELLED_DRIVER,
        TripStatus.CANCELLED_ADMIN -> MaterialTheme.colorScheme.errorContainer
    }
}

@Composable
fun TripStatus.onDisplayContainerColor(): Color {
    val extendedColors = LocalExtendedColors.current
    return when (this) {
        TripStatus.REQUESTED, TripStatus.OFFERED -> MaterialTheme.colorScheme.onSecondaryContainer
        TripStatus.ACCEPTED, TripStatus.AT_PICKUP, TripStatus.IN_TRANSIT -> MaterialTheme.colorScheme.onPrimaryContainer
        TripStatus.AT_DROPOFF -> MaterialTheme.colorScheme.onTertiaryContainer
        TripStatus.COMPLETED -> extendedColors.onSuccessContainer
        TripStatus.CANCELLED_NO_DRIVER,
        TripStatus.CANCELLED_CLIENT,
        TripStatus.CANCELLED_DRIVER,
        TripStatus.CANCELLED_ADMIN -> MaterialTheme.colorScheme.onErrorContainer
    }
}

internal fun TripStatus.toDisplayNameRes(): StringResource = when (this) {
    TripStatus.REQUESTED -> Res.string.trip_status_requested
    TripStatus.OFFERED -> Res.string.trip_status_offered
    TripStatus.ACCEPTED -> Res.string.trip_status_accepted
    TripStatus.AT_PICKUP -> Res.string.trip_status_at_pickup
    TripStatus.IN_TRANSIT -> Res.string.trip_status_in_transit
    TripStatus.AT_DROPOFF -> Res.string.trip_status_at_dropoff
    TripStatus.COMPLETED -> Res.string.trip_status_completed
    TripStatus.CANCELLED_NO_DRIVER -> Res.string.trip_status_cancelled_no_driver
    TripStatus.CANCELLED_CLIENT -> Res.string.trip_status_cancelled_client
    TripStatus.CANCELLED_DRIVER -> Res.string.trip_status_cancelled_driver
    TripStatus.CANCELLED_ADMIN -> Res.string.trip_status_cancelled_admin
}
