package com.juanpablo0612.tucargo.features.client.createtrip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpablo0612.tucargo.core.ui.asString
import com.juanpablo0612.tucargo.core.ui.components.ErrorCard
import com.juanpablo0612.tucargo.core.ui.components.LoadingButton
import com.juanpablo0612.tucargo.core.ui.components.MapComponent
import com.juanpablo0612.tucargo.core.ui.components.RoundedTextField
import com.juanpablo0612.tucargo.core.ui.components.StepIndicator
import com.juanpablo0612.tucargo.core.ui.components.buildOnboardingSteps
import com.juanpablo0612.tucargo.core.ui.toCurrencyString
import com.juanpablo0612.tucargo.core.ui.toDistanceString
import com.juanpablo0612.tucargo.domain.model.PaymentMethod
import com.juanpablo0612.tucargo.domain.model.TripQuote
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import tucargo.composeapp.generated.resources.Res
import tucargo.composeapp.generated.resources.create_trip_back_button
import tucargo.composeapp.generated.resources.create_trip_cargo_label
import tucargo.composeapp.generated.resources.create_trip_cargo_placeholder
import tucargo.composeapp.generated.resources.create_trip_destination_label
import tucargo.composeapp.generated.resources.create_trip_destination_placeholder
import tucargo.composeapp.generated.resources.create_trip_invalid_error
import tucargo.composeapp.generated.resources.create_trip_map_hint
import tucargo.composeapp.generated.resources.create_trip_next_button
import tucargo.composeapp.generated.resources.create_trip_origin_label
import tucargo.composeapp.generated.resources.create_trip_origin_placeholder
import tucargo.composeapp.generated.resources.create_trip_payment_cash
import tucargo.composeapp.generated.resources.create_trip_payment_title
import tucargo.composeapp.generated.resources.create_trip_payment_wallet
import tucargo.composeapp.generated.resources.create_trip_pin_required
import tucargo.composeapp.generated.resources.create_trip_quote_base
import tucargo.composeapp.generated.resources.create_trip_quote_distance
import tucargo.composeapp.generated.resources.create_trip_quote_error
import tucargo.composeapp.generated.resources.create_trip_quote_per_distance
import tucargo.composeapp.generated.resources.create_trip_quote_title
import tucargo.composeapp.generated.resources.create_trip_quote_total
import tucargo.composeapp.generated.resources.create_trip_step_destination
import tucargo.composeapp.generated.resources.create_trip_step_details
import tucargo.composeapp.generated.resources.create_trip_step_origin
import tucargo.composeapp.generated.resources.create_trip_submit_button
import tucargo.composeapp.generated.resources.create_trip_submit_error
import tucargo.composeapp.generated.resources.create_trip_title
import tucargo.composeapp.generated.resources.create_trip_user_not_authenticated

private const val DEFAULT_LAT = 4.6097 // Bogotá
private const val DEFAULT_LNG = -74.0817

@Composable
fun CreateTripScreen(
    viewModel: CreateTripViewModel = koinViewModel(),
    onTripCreated: (tripId: String) -> Unit,
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createdTripId) {
        uiState.createdTripId?.let { tripId ->
            onTripCreated(tripId)
            viewModel.onNavigated()
        }
    }

    CreateTripScreenContent(
        uiState = uiState,
        originAddressState = viewModel.originAddressState,
        destinationAddressState = viewModel.destinationAddressState,
        cargoDescriptionState = viewModel.cargoDescriptionState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateTripScreenContent(
    uiState: CreateTripState,
    originAddressState: androidx.compose.foundation.text.input.TextFieldState,
    destinationAddressState: androidx.compose.foundation.text.input.TextFieldState,
    cargoDescriptionState: androidx.compose.foundation.text.input.TextFieldState,
    onAction: (CreateTripAction) -> Unit,
    onBackClick: () -> Unit,
) {
    val stepLabels = listOf(
        stringResource(Res.string.create_trip_step_origin),
        stringResource(Res.string.create_trip_step_destination),
        stringResource(Res.string.create_trip_step_details),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.create_trip_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.step == CreateTripStep.ORIGIN) onBackClick()
                            else onAction(CreateTripAction.PreviousStep)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.create_trip_back_button),
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
                steps = buildOnboardingSteps(
                    currentStep = uiState.step.ordinal + 1,
                    labels = stepLabels
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            uiState.error?.let { error ->
                val msgRes = when (error) {
                    CreateTripError.QuoteError -> Res.string.create_trip_quote_error
                    CreateTripError.SubmitError -> Res.string.create_trip_submit_error
                    CreateTripError.InvalidTrip -> Res.string.create_trip_invalid_error
                    CreateTripError.UserNotAuthenticated -> Res.string.create_trip_user_not_authenticated
                }
                ErrorCard(message = stringResource(msgRes), modifier = Modifier.fillMaxWidth())
            }

            when (uiState.step) {
                CreateTripStep.ORIGIN -> LocationStep(
                    addressState = originAddressState,
                    addressLabel = stringResource(Res.string.create_trip_origin_label),
                    addressPlaceholder = stringResource(Res.string.create_trip_origin_placeholder),
                    addressError = uiState.originAddressError?.asString(),
                    pinMissing = uiState.originPinMissing,
                    pickedLat = uiState.originLat,
                    pickedLng = uiState.originLng,
                    isLoading = false,
                    buttonText = stringResource(Res.string.create_trip_next_button),
                    onMapClick = { lat, lng -> onAction(CreateTripAction.OnMapClick(lat, lng)) },
                    onContinue = { onAction(CreateTripAction.NextStep) },
                )

                CreateTripStep.DESTINATION -> LocationStep(
                    addressState = destinationAddressState,
                    addressLabel = stringResource(Res.string.create_trip_destination_label),
                    addressPlaceholder = stringResource(Res.string.create_trip_destination_placeholder),
                    addressError = uiState.destinationAddressError?.asString(),
                    pinMissing = uiState.destinationPinMissing,
                    pickedLat = uiState.destinationLat,
                    pickedLng = uiState.destinationLng,
                    isLoading = uiState.isQuoteLoading,
                    buttonText = stringResource(Res.string.create_trip_next_button),
                    onMapClick = { lat, lng -> onAction(CreateTripAction.OnMapClick(lat, lng)) },
                    onContinue = { onAction(CreateTripAction.NextStep) },
                )

                CreateTripStep.DETAILS -> DetailsStep(
                    uiState = uiState,
                    cargoDescriptionState = cargoDescriptionState,
                    onAction = onAction,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LocationStep(
    addressState: androidx.compose.foundation.text.input.TextFieldState,
    addressLabel: String,
    addressPlaceholder: String,
    addressError: String?,
    pinMissing: Boolean,
    pickedLat: Double?,
    pickedLng: Double?,
    isLoading: Boolean,
    buttonText: String,
    onMapClick: (Double, Double) -> Unit,
    onContinue: () -> Unit,
) {
    RoundedTextField(
        state = addressState,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(addressLabel) },
        placeholder = { Text(addressPlaceholder) },
        isError = addressError != null,
        supportingText = addressError?.let { { Text(it) } },
    )

    Text(
        text = stringResource(Res.string.create_trip_map_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Card(shape = MaterialTheme.shapes.large) {
        MapComponent(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            latitude = pickedLat ?: DEFAULT_LAT,
            longitude = pickedLng ?: DEFAULT_LNG,
            onMapClick = onMapClick,
        )
    }

    if (pinMissing) {
        Text(
            text = stringResource(Res.string.create_trip_pin_required),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }

    LoadingButton(
        onClick = onContinue,
        isLoading = isLoading,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = buttonText, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun DetailsStep(
    uiState: CreateTripState,
    cargoDescriptionState: androidx.compose.foundation.text.input.TextFieldState,
    onAction: (CreateTripAction) -> Unit,
) {
    uiState.quote?.let { QuoteCard(quote = it) }

    RoundedTextField(
        state = cargoDescriptionState,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(Res.string.create_trip_cargo_label)) },
        placeholder = { Text(stringResource(Res.string.create_trip_cargo_placeholder)) },
        isError = uiState.cargoDescriptionError != null,
        supportingText = uiState.cargoDescriptionError?.let { error -> { Text(error.asString()) } },
    )

    Text(
        text = stringResource(Res.string.create_trip_payment_title),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = uiState.paymentMethod == PaymentMethod.CASH,
            onClick = { onAction(CreateTripAction.SelectPaymentMethod(PaymentMethod.CASH)) },
            label = { Text(stringResource(Res.string.create_trip_payment_cash)) },
        )
        FilterChip(
            selected = false,
            onClick = {},
            enabled = false,
            label = { Text(stringResource(Res.string.create_trip_payment_wallet)) },
        )
    }

    LoadingButton(
        onClick = { onAction(CreateTripAction.Submit) },
        isLoading = uiState.isSubmitting,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(Res.string.create_trip_submit_button),
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
private fun QuoteCard(quote: TripQuote) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.create_trip_quote_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(
                    Res.string.create_trip_quote_distance,
                    quote.distanceKm.toDistanceString()
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            QuoteRow(
                label = stringResource(Res.string.create_trip_quote_base),
                value = quote.priceBase.toCurrencyString(),
            )
            QuoteRow(
                label = stringResource(Res.string.create_trip_quote_per_distance),
                value = quote.priceDistance.toCurrencyString(),
            )
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.create_trip_quote_total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = quote.priceTotal.toCurrencyString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun QuoteRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
