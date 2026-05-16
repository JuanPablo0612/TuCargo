package com.juanpablo0612.tucargo.features.auth.presentation.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.docs_back_button
import tucargo.composeapp.generated.resources.docs_back_label
import tucargo.composeapp.generated.resources.docs_front_label
import tucargo.composeapp.generated.resources.docs_submit_button
import tucargo.composeapp.generated.resources.docs_subtitle
import tucargo.composeapp.generated.resources.docs_title

@Composable
fun DocumentScreen(
    viewModel: DocumentViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onSuccessNavigate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { onSuccessNavigate() }
    }

    DocumentScreenContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick
    )
}

@Composable
internal fun DocumentScreenContent(
    uiState: DocumentState,
    onAction: (DocumentAction) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(Res.string.docs_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.docs_subtitle),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            uiState.errorMessage?.let {
                ErrorCard(
                    message = it,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            DocumentPickerItem(
                label = stringResource(Res.string.docs_front_label),
                isLoaded = uiState.idFrontPath != null,
                onClick = { onAction(DocumentAction.OnFrontPhotoSelected("simulated_path_front")) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DocumentPickerItem(
                label = stringResource(Res.string.docs_back_label),
                isLoaded = uiState.idBackPath != null,
                onClick = { onAction(DocumentAction.OnBackPhotoSelected("simulated_path_back")) }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onAction(DocumentAction.OnSubmit) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.docs_submit_button),
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            TextButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(Res.string.docs_back_button))
            }
        }
    }
}

@Composable
internal fun DocumentPickerItem(
    label: String,
    isLoaded: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isLoaded) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (isLoaded) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(containerColor)
            .border(2.dp, borderColor, MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isLoaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isLoaded) "✓" else "+",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
internal fun DocumentScreenContentPreview() {
    TuCargoTheme {
        DocumentScreenContent(
            uiState = DocumentState(),
            onAction = {},
            onBackClick = {}
        )
    }
}
