package com.juanpablo0612.tucargo.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.juanpablo0612.tucargo.core.ui.theme.LocalDimensions

@Composable
fun ResponsiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = LocalDimensions.current.contentMaxWidth,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = if (maxWidth != Dp.Unspecified) {
                Modifier.widthIn(max = maxWidth).fillMaxWidth()
            } else {
                Modifier.fillMaxWidth()
            }
        ) {
            content()
        }
    }
}

fun Modifier.responsiveMaxWidth(maxWidth: Dp): Modifier =
    if (maxWidth != Dp.Unspecified) {
        this.widthIn(max = maxWidth)
    } else {
        this
    }
