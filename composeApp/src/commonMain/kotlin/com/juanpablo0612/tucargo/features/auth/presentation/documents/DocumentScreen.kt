package com.juanpablo0612.tucargo.features.auth.presentation.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DocumentScreen(
    viewModel: DocumentViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onSuccessNavigate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                text = "Identity Verification",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please upload a clear photo of your identity card (both sides).",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Módulo de Error consistente con tu Login
            uiState.errorMessage?.let {
                ErrorCard(
                    message = it,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Selector Frontal
            DocumentPickerItem(
                label = "Front Side ID",
                isLoaded = uiState.idFrontPath != null,
                onClick = { onAction(DocumentAction.OnFrontPhotoSelected("simulated_path_front")) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector Trasero
            DocumentPickerItem(
                label = "Back Side ID",
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
                        text = if (uiState.isUploadSuccess) "Documents Uploaded" else "Continue",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            TextButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Go Back")
            }
        }
    }
}

@Composable
fun DocumentPickerItem(
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
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Círculo indicador personalizado (SIN ICONOS)
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
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}