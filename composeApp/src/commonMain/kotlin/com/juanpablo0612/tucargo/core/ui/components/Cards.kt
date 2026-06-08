package com.juanpablo0612.tucargo.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.juanpablo0612.tucargo.core.ui.theme.LocalExtendedColors
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import com.juanpablo0612.tucargo.data.trip.TripStatus
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.trip_status_arrived_pickup
import tucargo.composeapp.generated.resources.trip_status_assigned
import tucargo.composeapp.generated.resources.trip_status_cancelled
import tucargo.composeapp.generated.resources.trip_status_completed
import tucargo.composeapp.generated.resources.trip_status_in_progress
import tucargo.composeapp.generated.resources.trip_status_on_way
import tucargo.composeapp.generated.resources.trip_status_searching

@Composable
fun ErrorCard(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                },
            )
        }
    }
}

@Preview
@Composable
fun ErrorCardPreview() {
    TuCargoTheme {
        Surface {
            ErrorCard(message = "An unknown error occurred", title = "Error")
        }
    }
}

@Composable
internal fun TripStatusBadge(status: TripStatus, modifier: Modifier = Modifier) {
    val extendedColors = LocalExtendedColors.current
    val (bg, fg) = when (status) {
        TripStatus.SEARCHING -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
        )
        TripStatus.ASSIGNED, TripStatus.ON_WAY, TripStatus.ARRIVED_PICKUP -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )
        TripStatus.IN_PROGRESS -> Pair(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
        )
        TripStatus.COMPLETED -> Pair(
            extendedColors.successContainer,
            extendedColors.onSuccessContainer,
        )
        TripStatus.CANCELLED -> Pair(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
        )
    }
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = stringResource(status.toDisplayNameRes()),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
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
