package com.juanpablo0612.tucargo.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import org.jetbrains.compose.resources.painterResource
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.add
import tucargo.composeapp.generated.resources.check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.juanpablo0612.tucargo.core.ui.theme.LocalDimensions

@Composable
fun DocumentPickerItem(
    label: String,
    isLoaded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fileName: String? = null,
    isError: Boolean = false,
) {
    val containerColor = when {
        isError -> MaterialTheme.colorScheme.errorContainer
        isLoaded -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isLoaded -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val iconContainerColor = when {
        isError -> MaterialTheme.colorScheme.error
        isLoaded -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    val iconTint = if (isLoaded || isError) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.surface

    val dimensions = LocalDimensions.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensions.documentPickerHeight)
            .clip(MaterialTheme.shapes.medium)
            .background(containerColor)
            .border(2.dp, borderColor, MaterialTheme.shapes.medium)
            .semantics(mergeDescendants = true) { role = Role.Button }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(dimensions.iconSizeMedium)
                    .clip(CircleShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(if (isLoaded) Res.drawable.check else Res.drawable.add),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isLoaded) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (fileName != null) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
