package com.juanpablo0612.tucargo.features.driver.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.bg_location_dialog_bullet_tracking
import tucargo.composeapp.generated.resources.bg_location_dialog_bullet_updates
import tucargo.composeapp.generated.resources.bg_location_dialog_continue
import tucargo.composeapp.generated.resources.bg_location_dialog_instruction
import tucargo.composeapp.generated.resources.bg_location_dialog_intro
import tucargo.composeapp.generated.resources.bg_location_dialog_not_now
import tucargo.composeapp.generated.resources.bg_location_dialog_title

@Composable
fun BackgroundLocationDisclosureDialog(
    onContinue: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.bg_location_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.bg_location_dialog_intro),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(Res.string.bg_location_dialog_bullet_updates),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(Res.string.bg_location_dialog_bullet_tracking),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.bg_location_dialog_instruction),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.bg_location_dialog_continue))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.bg_location_dialog_not_now))
            }
        }
    )
}
