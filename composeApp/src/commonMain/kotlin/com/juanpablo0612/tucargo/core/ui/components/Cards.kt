package com.juanpablo0612.tucargo.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme

@Composable
fun ErrorCard(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            title?.let {
                Text(text = it)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(text = message)
        }
    }
}

@Preview
@Composable
fun ErrorCardPreview() {
    TuCargoTheme {
        ErrorCard(message = "An unknown error occurred")
    }
}