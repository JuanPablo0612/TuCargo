package com.juanpablo0612.tucargo.features.auth.presentation.documents

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.asString
import com.juanpablo0612.tucargo.core.ui.components.DocumentPickerItem
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.theme.TuCargoTheme
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.docs_back_button
import tucargo.composeapp.generated.resources.docs_back_label
import tucargo.composeapp.generated.resources.docs_front_label
import tucargo.composeapp.generated.resources.docs_submit_button
import tucargo.composeapp.generated.resources.docs_subtitle
import tucargo.composeapp.generated.resources.docs_title
import tucargo.composeapp.generated.resources.docs_upload_error
import tucargo.composeapp.generated.resources.docs_user_not_authenticated

@Composable
fun DocumentScreen(
    viewModel: DocumentViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onSuccessNavigate: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isUploadComplete) {
        if (uiState.isUploadComplete) {
            onSuccessNavigate()
            viewModel.onNavigated()
        }
    }

    DocumentScreenContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DocumentScreenContent(
    uiState: DocumentState,
    onAction: (DocumentAction) -> Unit,
    onBackClick: () -> Unit,
) {
    val frontLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        file?.let { onAction(DocumentAction.OnFrontPhotoSelected(it)) }
    }
    val backLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        file?.let { onAction(DocumentAction.OnBackPhotoSelected(it)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.docs_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.docs_back_button),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.docs_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AnimatedVisibility(
                visible = uiState.authError != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                uiState.authError?.let {
                    val errorRes = when (it) {
                        DocumentError.UserNotAuthenticated -> Res.string.docs_user_not_authenticated
                        DocumentError.UploadError -> Res.string.docs_upload_error
                    }
                    ErrorCard(
                        message = stringResource(errorRes),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DocumentPickerItem(
                    label = stringResource(Res.string.docs_front_label),
                    isLoaded = uiState.idFront != null,
                    isError = uiState.frontFileError != null,
                    onClick = { frontLauncher.launch() },
                    fileName = uiState.idFront?.name,
                )
                uiState.frontFileError?.let { err ->
                    Text(
                        text = err.asString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DocumentPickerItem(
                    label = stringResource(Res.string.docs_back_label),
                    isLoaded = uiState.idBack != null,
                    isError = uiState.backFileError != null,
                    onClick = { backLauncher.launch() },
                    fileName = uiState.idBack?.name,
                )
                uiState.backFileError?.let { err ->
                    Text(
                        text = err.asString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onAction(DocumentAction.OnSubmit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                enabled = !uiState.isLoading,
            ) {
                AnimatedContent(
                    targetState = uiState.isLoading,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "DocumentSubmitButton",
                ) { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.docs_submit_button),
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
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
            onBackClick = {},
        )
    }
}
