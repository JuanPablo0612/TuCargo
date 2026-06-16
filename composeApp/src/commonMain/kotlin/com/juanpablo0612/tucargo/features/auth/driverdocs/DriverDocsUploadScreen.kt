package com.juanpablo0612.tucargo.features.auth.driverdocs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.components.DocumentPickerItem
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.components.LoadingButton
import com.juanpablo0612.tucargo.core.ui.components.StepIndicator
import com.juanpablo0612.tucargo.core.ui.components.buildOnboardingSteps
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.driver_docs_back_button
import tucargo.composeapp.generated.resources.driver_docs_file_too_large
import tucargo.composeapp.generated.resources.driver_docs_id_back
import tucargo.composeapp.generated.resources.driver_docs_id_front
import tucargo.composeapp.generated.resources.driver_docs_license
import tucargo.composeapp.generated.resources.driver_docs_personal_section
import tucargo.composeapp.generated.resources.driver_docs_registration_card
import tucargo.composeapp.generated.resources.driver_docs_soat
import tucargo.composeapp.generated.resources.driver_docs_submit_button
import tucargo.composeapp.generated.resources.driver_docs_subtitle
import tucargo.composeapp.generated.resources.driver_docs_tech_review
import tucargo.composeapp.generated.resources.driver_docs_title
import tucargo.composeapp.generated.resources.driver_docs_upload_error
import tucargo.composeapp.generated.resources.driver_docs_user_not_authenticated
import tucargo.composeapp.generated.resources.driver_docs_vehicle_section
import tucargo.composeapp.generated.resources.onboarding_step_documents
import tucargo.composeapp.generated.resources.onboarding_step_personal
import tucargo.composeapp.generated.resources.onboarding_step_review
import tucargo.composeapp.generated.resources.onboarding_step_vehicle

@Composable
fun DriverDocsUploadScreen(
    viewModel: DriverDocsUploadViewModel = koinViewModel(),
    onSuccessNavigate: () -> Unit,
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isUploadComplete) {
        if (uiState.isUploadComplete) {
            onSuccessNavigate()
            viewModel.onNavigated()
        }
    }

    DriverDocsUploadScreenContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DriverDocsUploadScreenContent(
    uiState: DriverDocsUploadState,
    onAction: (DriverDocsAction) -> Unit,
    onBackClick: () -> Unit,
) {
    val stepLabels = listOf(
        stringResource(Res.string.onboarding_step_personal),
        stringResource(Res.string.onboarding_step_vehicle),
        stringResource(Res.string.onboarding_step_documents),
        stringResource(Res.string.onboarding_step_review),
    )

    val idFrontLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        file?.let { onAction(DriverDocsAction.OnDocumentSelected(KycDocumentType.ID_FRONT, it)) }
    }
    val idBackLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        file?.let { onAction(DriverDocsAction.OnDocumentSelected(KycDocumentType.ID_BACK, it)) }
    }
    val licenseLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        file?.let { onAction(DriverDocsAction.OnDocumentSelected(KycDocumentType.DRIVER_LICENSE, it)) }
    }
    val soatLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        file?.let { onAction(DriverDocsAction.OnDocumentSelected(KycDocumentType.SOAT, it)) }
    }
    val techReviewLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        file?.let { onAction(DriverDocsAction.OnDocumentSelected(KycDocumentType.VEHICLE_TECH_REVIEW, it)) }
    }
    val regCardLauncher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        file?.let { onAction(DriverDocsAction.OnDocumentSelected(KycDocumentType.VEHICLE_REGISTRATION_CARD, it)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.driver_docs_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.driver_docs_back_button),
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StepIndicator(
                steps = buildOnboardingSteps(currentStep = 3, labels = stepLabels),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = stringResource(Res.string.driver_docs_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AnimatedVisibility(
                visible = uiState.uploadError != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                uiState.uploadError?.let {
                    val msgRes = when (it) {
                        DriverDocsError.UserNotAuthenticated -> Res.string.driver_docs_user_not_authenticated
                        DriverDocsError.UploadError, DriverDocsError.SomeDocsFailed -> Res.string.driver_docs_upload_error
                        DriverDocsError.FileTooLarge -> Res.string.driver_docs_file_too_large
                    }
                    ErrorCard(
                        message = stringResource(msgRes),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Text(
                text = stringResource(Res.string.driver_docs_personal_section),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            DocumentPickerItem(
                label = stringResource(Res.string.driver_docs_id_front),
                isLoaded = uiState.documents[KycDocumentType.ID_FRONT] != null,
                isError = uiState.documentErrors[KycDocumentType.ID_FRONT] == true,
                onClick = { idFrontLauncher.launch() },
                fileName = uiState.documents[KycDocumentType.ID_FRONT]?.name,
            )

            DocumentPickerItem(
                label = stringResource(Res.string.driver_docs_id_back),
                isLoaded = uiState.documents[KycDocumentType.ID_BACK] != null,
                isError = uiState.documentErrors[KycDocumentType.ID_BACK] == true,
                onClick = { idBackLauncher.launch() },
                fileName = uiState.documents[KycDocumentType.ID_BACK]?.name,
            )

            DocumentPickerItem(
                label = stringResource(Res.string.driver_docs_license),
                isLoaded = uiState.documents[KycDocumentType.DRIVER_LICENSE] != null,
                isError = uiState.documentErrors[KycDocumentType.DRIVER_LICENSE] == true,
                onClick = { licenseLauncher.launch() },
                fileName = uiState.documents[KycDocumentType.DRIVER_LICENSE]?.name,
            )

            HorizontalDivider()

            Text(
                text = stringResource(Res.string.driver_docs_vehicle_section),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            DocumentPickerItem(
                label = stringResource(Res.string.driver_docs_soat),
                isLoaded = uiState.documents[KycDocumentType.SOAT] != null,
                isError = uiState.documentErrors[KycDocumentType.SOAT] == true,
                onClick = { soatLauncher.launch() },
                fileName = uiState.documents[KycDocumentType.SOAT]?.name,
            )

            DocumentPickerItem(
                label = stringResource(Res.string.driver_docs_tech_review),
                isLoaded = uiState.documents[KycDocumentType.VEHICLE_TECH_REVIEW] != null,
                isError = uiState.documentErrors[KycDocumentType.VEHICLE_TECH_REVIEW] == true,
                onClick = { techReviewLauncher.launch() },
                fileName = uiState.documents[KycDocumentType.VEHICLE_TECH_REVIEW]?.name,
            )

            DocumentPickerItem(
                label = stringResource(Res.string.driver_docs_registration_card),
                isLoaded = uiState.documents[KycDocumentType.VEHICLE_REGISTRATION_CARD] != null,
                isError = uiState.documentErrors[KycDocumentType.VEHICLE_REGISTRATION_CARD] == true,
                onClick = { regCardLauncher.launch() },
                fileName = uiState.documents[KycDocumentType.VEHICLE_REGISTRATION_CARD]?.name,
            )

            Spacer(Modifier.height(8.dp))

            LoadingButton(
                onClick = { onAction(DriverDocsAction.OnSubmit) },
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(Res.string.driver_docs_submit_button),
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
