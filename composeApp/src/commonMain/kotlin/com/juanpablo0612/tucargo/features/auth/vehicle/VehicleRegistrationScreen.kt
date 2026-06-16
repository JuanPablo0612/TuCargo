package com.juanpablo0612.tucargo.features.auth.vehicle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.asString
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.components.LoadingButton
import com.juanpablo0612.tucargo.core.ui.components.RoundedTextField
import com.juanpablo0612.tucargo.core.ui.components.StepIndicator
import com.juanpablo0612.tucargo.core.ui.components.buildOnboardingSteps
import com.juanpablo0612.tucargo.domain.model.VehicleType
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.onboarding_step_documents
import tucargo.composeapp.generated.resources.onboarding_step_personal
import tucargo.composeapp.generated.resources.onboarding_step_review
import tucargo.composeapp.generated.resources.onboarding_step_vehicle
import tucargo.composeapp.generated.resources.vehicle_reg_back_button
import tucargo.composeapp.generated.resources.vehicle_reg_color_label
import tucargo.composeapp.generated.resources.vehicle_reg_color_placeholder
import tucargo.composeapp.generated.resources.vehicle_reg_model_label
import tucargo.composeapp.generated.resources.vehicle_reg_model_placeholder
import tucargo.composeapp.generated.resources.vehicle_reg_plate_label
import tucargo.composeapp.generated.resources.vehicle_reg_plate_placeholder
import tucargo.composeapp.generated.resources.vehicle_reg_save_error
import tucargo.composeapp.generated.resources.vehicle_reg_submit_button
import tucargo.composeapp.generated.resources.vehicle_reg_subtitle
import tucargo.composeapp.generated.resources.vehicle_reg_title
import tucargo.composeapp.generated.resources.vehicle_reg_type_label
import tucargo.composeapp.generated.resources.vehicle_reg_year_label
import tucargo.composeapp.generated.resources.vehicle_reg_year_placeholder
import tucargo.composeapp.generated.resources.vehicle_type_car
import tucargo.composeapp.generated.resources.vehicle_type_motorcycle
import tucargo.composeapp.generated.resources.vehicle_type_truck
import tucargo.composeapp.generated.resources.vehicle_type_van

@Composable
fun VehicleRegistrationScreen(
    viewModel: VehicleRegistrationViewModel = koinViewModel(),
    onSuccessNavigate: () -> Unit,
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaveComplete) {
        if (uiState.isSaveComplete) {
            onSuccessNavigate()
            viewModel.onNavigated()
        }
    }

    VehicleRegistrationScreenContent(
        uiState = uiState,
        plateState = viewModel.plateState,
        modelState = viewModel.modelState,
        colorState = viewModel.colorState,
        yearState = viewModel.yearState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun VehicleRegistrationScreenContent(
    uiState: VehicleRegistrationState,
    plateState: TextFieldState,
    modelState: TextFieldState,
    colorState: TextFieldState,
    yearState: TextFieldState,
    onAction: (VehicleRegistrationAction) -> Unit,
    onBackClick: () -> Unit,
) {
    val stepLabels = listOf(
        stringResource(Res.string.onboarding_step_personal),
        stringResource(Res.string.onboarding_step_vehicle),
        stringResource(Res.string.onboarding_step_documents),
        stringResource(Res.string.onboarding_step_review),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.vehicle_reg_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.vehicle_reg_back_button),
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
                steps = buildOnboardingSteps(currentStep = 2, labels = stepLabels),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = stringResource(Res.string.vehicle_reg_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AnimatedVisibility(
                visible = uiState.saveError != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                uiState.saveError?.let {
                    ErrorCard(
                        message = stringResource(Res.string.vehicle_reg_save_error),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            RoundedTextField(
                state = plateState,
                label = { Text(stringResource(Res.string.vehicle_reg_plate_label)) },
                placeholder = { Text(stringResource(Res.string.vehicle_reg_plate_placeholder)) },
                isError = uiState.plateError != null,
                supportingText = uiState.plateError?.let { err -> { Text(err.asString()) } },
                modifier = Modifier.fillMaxWidth(),
            )

            RoundedTextField(
                state = modelState,
                label = { Text(stringResource(Res.string.vehicle_reg_model_label)) },
                placeholder = { Text(stringResource(Res.string.vehicle_reg_model_placeholder)) },
                isError = uiState.modelError != null,
                supportingText = uiState.modelError?.let { err -> { Text(err.asString()) } },
                modifier = Modifier.fillMaxWidth(),
            )

            RoundedTextField(
                state = colorState,
                label = { Text(stringResource(Res.string.vehicle_reg_color_label)) },
                placeholder = { Text(stringResource(Res.string.vehicle_reg_color_placeholder)) },
                isError = uiState.colorError != null,
                supportingText = uiState.colorError?.let { err -> { Text(err.asString()) } },
                modifier = Modifier.fillMaxWidth(),
            )

            RoundedTextField(
                state = yearState,
                label = { Text(stringResource(Res.string.vehicle_reg_year_label)) },
                placeholder = { Text(stringResource(Res.string.vehicle_reg_year_placeholder)) },
                isError = uiState.yearError != null,
                supportingText = uiState.yearError?.let { err -> { Text(err.asString()) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.vehicle_reg_type_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    VehicleType.entries.forEach { type ->
                        FilterChip(
                            selected = uiState.selectedVehicleType == type,
                            onClick = { onAction(VehicleRegistrationAction.SelectVehicleType(type)) },
                            label = {
                                Text(
                                    text = when (type) {
                                        VehicleType.CAR -> stringResource(Res.string.vehicle_type_car)
                                        VehicleType.MOTORCYCLE -> stringResource(Res.string.vehicle_type_motorcycle)
                                        VehicleType.VAN -> stringResource(Res.string.vehicle_type_van)
                                        VehicleType.TRUCK -> stringResource(Res.string.vehicle_type_truck)
                                    }
                                )
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            LoadingButton(
                onClick = { onAction(VehicleRegistrationAction.Submit) },
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(Res.string.vehicle_reg_submit_button),
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
