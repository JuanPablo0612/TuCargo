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
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.features.trip.presentation.displayContainerColor
import com.juanpablo0612.tucargo.features.trip.presentation.displayName
import com.juanpablo0612.tucargo.features.trip.presentation.onDisplayContainerColor

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
                    liveRegion = LiveRegionMode.Assertive
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
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(status.displayContainerColor())
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = status.displayName(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = status.onDisplayContainerColor(),
        )
    }
}
