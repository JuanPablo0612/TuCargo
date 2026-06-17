package com.juanpablo0612.tucargo.features.client.quote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.juanpablo0612.tucargo.core.ui.components.ResponsiveContainer
import com.juanpablo0612.tucargo.core.ui.theme.LocalDimensions
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.cargo_description_label
import tucargo.composeapp.generated.resources.cargo_description_placeholder
import tucargo.composeapp.generated.resources.cargo_next_button
import tucargo.composeapp.generated.resources.cargo_title
import tucargo.composeapp.generated.resources.cargo_weight_confirm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargoScreen(
    viewModel: TripRequestViewModel = koinViewModel(),
    onNext: () -> Unit,
    onBackClick: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var weightConfirmed by remember { mutableStateOf(false) }

    val isValid = description.length in 1..200 && weightConfirmed

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.cargo_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        val dimensions = LocalDimensions.current
        ResponsiveContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensions.formHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(dimensions.sectionSpacing)
        ) {
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 200) description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.cargo_description_label)) },
                placeholder = { Text(stringResource(Res.string.cargo_description_placeholder)) },
                maxLines = 4,
                supportingText = { Text("${description.length}/200") }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = weightConfirmed,
                    onCheckedChange = { weightConfirmed = it }
                )
                Text(
                    text = stringResource(Res.string.cargo_weight_confirm),
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    viewModel.confirmCargo(description, weightConfirmed)
                    viewModel.requestQuote()
                    onNext()
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.cargo_next_button))
            }
        }
        }
    }
}
