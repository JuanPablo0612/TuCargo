package com.juanpablo0612.tucargo.features.driver.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import org.jetbrains.compose.resources.painterResource
import tucargo.composeapp.generated.resources.power_settings_new
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.juanpablo0612.tucargo.core.ui.theme.LocalExtendedColors
import org.jetbrains.compose.resources.stringResource
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.driver_home_availability_go_online
import tucargo.composeapp.generated.resources.driver_home_availability_online

@Composable
fun AvailabilityButton(
    isAvailable: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current

    val backgroundColor by animateColorAsState(
        targetValue = if (isAvailable) extendedColors.available else MaterialTheme.colorScheme.surfaceVariant,
        label = "AvailabilityButtonBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isAvailable) extendedColors.onAvailable else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "AvailabilityButtonContent"
    )
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = backgroundColor,
        contentColor = contentColor
    )

    val stateLabel = if (isAvailable)
        stringResource(Res.string.driver_home_availability_online)
    else
        stringResource(Res.string.driver_home_availability_go_online)

    Button(
        onClick = { onToggle(!isAvailable) },
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .semantics { stateDescription = stateLabel },
        colors = buttonColors,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.power_settings_new),
                contentDescription = null
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stateLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
fun AvailabilityButtonPreview() {
    TuCargoTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AvailabilityButton(isAvailable = true, onToggle = {})
            AvailabilityButton(isAvailable = false, onToggle = {})
        }
    }
}
