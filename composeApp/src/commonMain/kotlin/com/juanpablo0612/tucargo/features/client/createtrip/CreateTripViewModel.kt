package com.juanpablo0612.tucargo.features.client.createtrip

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.core.validation.FieldError
import com.juanpablo0612.tucargo.core.validation.FormValidators
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.PaymentMethod
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripLocation
import com.juanpablo0612.tucargo.domain.model.TripStatus
import com.juanpablo0612.tucargo.domain.usecase.CalculateTripPriceUseCase
import com.juanpablo0612.tucargo.domain.usecase.CreateTripUseCase
import com.juanpablo0612.tucargo.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class CreateTripViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val calculateTripPriceUseCase: CalculateTripPriceUseCase,
    private val createTripUseCase: CreateTripUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTripState())
    val uiState = _uiState.asStateFlow()

    val originAddressState = TextFieldState()
    val destinationAddressState = TextFieldState()
    val cargoDescriptionState = TextFieldState()

    fun onAction(action: CreateTripAction) {
        when (action) {
            is CreateTripAction.OnMapClick -> onMapClick(action.latitude, action.longitude)
            CreateTripAction.NextStep -> nextStep()
            CreateTripAction.PreviousStep -> previousStep()
            is CreateTripAction.SelectPaymentMethod -> selectPaymentMethod(action.method)
            CreateTripAction.Submit -> submit()
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(createdTripId = null) }
    }

    private fun onMapClick(latitude: Double, longitude: Double) {
        _uiState.update {
            when (it.step) {
                CreateTripStep.ORIGIN -> it.copy(
                    originLat = latitude,
                    originLng = longitude,
                    originPinMissing = false
                )
                CreateTripStep.DESTINATION -> it.copy(
                    destinationLat = latitude,
                    destinationLng = longitude,
                    destinationPinMissing = false
                )
                CreateTripStep.DETAILS -> it
            }
        }
    }

    private fun selectPaymentMethod(method: PaymentMethod) {
        // Wallet payments are not implemented yet; the UI renders the option
        // disabled, this is just the backstop.
        if (method == PaymentMethod.WALLET) return
        _uiState.update { it.copy(paymentMethod = method) }
    }

    private fun previousStep() {
        _uiState.update {
            it.copy(
                step = when (it.step) {
                    CreateTripStep.ORIGIN, CreateTripStep.DESTINATION -> CreateTripStep.ORIGIN
                    CreateTripStep.DETAILS -> CreateTripStep.DESTINATION
                },
                error = null
            )
        }
    }

    private fun nextStep() {
        when (_uiState.value.step) {
            CreateTripStep.ORIGIN -> validateOriginAndAdvance()
            CreateTripStep.DESTINATION -> validateDestinationAndQuote()
            CreateTripStep.DETAILS -> Unit
        }
    }

    private fun validateOriginAndAdvance() {
        val state = _uiState.value
        val addressError = FormValidators.required(
            originAddressState.text.toString().trim(), FieldError.AddressRequired
        )
        val pinMissing = state.originLat == null || state.originLng == null
        _uiState.update { it.copy(originAddressError = addressError, originPinMissing = pinMissing) }
        if (addressError == null && !pinMissing) {
            _uiState.update { it.copy(step = CreateTripStep.DESTINATION) }
        }
    }

    private fun validateDestinationAndQuote() {
        val state = _uiState.value
        val addressError = FormValidators.required(
            destinationAddressState.text.toString().trim(), FieldError.AddressRequired
        )
        val pinMissing = state.destinationLat == null || state.destinationLng == null
        _uiState.update {
            it.copy(destinationAddressError = addressError, destinationPinMissing = pinMissing)
        }
        if (addressError != null || pinMissing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isQuoteLoading = true, error = null) }
            calculateTripPriceUseCase(
                originLat = state.originLat!!,
                originLng = state.originLng!!,
                destinationLat = state.destinationLat!!,
                destinationLng = state.destinationLng!!
            ).fold(
                onSuccess = { quote ->
                    _uiState.update {
                        it.copy(isQuoteLoading = false, quote = quote, step = CreateTripStep.DETAILS)
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isQuoteLoading = false,
                            error = if (e is AppError.Validation.InvalidTrip) {
                                CreateTripError.InvalidTrip
                            } else {
                                CreateTripError.QuoteError
                            }
                        )
                    }
                }
            )
        }
    }

    private fun submit() {
        val state = _uiState.value
        val quote = state.quote ?: return
        val cargoError = FormValidators.required(
            cargoDescriptionState.text.toString().trim(), FieldError.CargoDescriptionRequired
        )
        _uiState.update { it.copy(cargoDescriptionError = cargoError) }
        if (cargoError != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val user = getCurrentUserUseCase().getOrNull()
            if (user == null) {
                _uiState.update {
                    it.copy(isSubmitting = false, error = CreateTripError.UserNotAuthenticated)
                }
                return@launch
            }

            val trip = Trip(
                status = TripStatus.REQUESTED,
                clientId = user.id,
                clientName = user.fullName,
                clientPhone = user.phone,
                priceTotal = quote.priceTotal.toInt(),
                priceBase = quote.priceBase.toInt(),
                priceDistance = quote.priceDistance.toInt(),
                commissionFee = quote.commissionFee.toInt(),
                paymentMethod = state.paymentMethod,
                origin = TripLocation(
                    address = originAddressState.text.toString().trim(),
                    lat = state.originLat ?: 0.0,
                    lng = state.originLng ?: 0.0
                ),
                destination = TripLocation(
                    address = destinationAddressState.text.toString().trim(),
                    lat = state.destinationLat ?: 0.0,
                    lng = state.destinationLng ?: 0.0
                ),
                distanceKm = quote.distanceKm,
                cargoDescription = cargoDescriptionState.text.toString().trim(),
                deliveryCode = Random.nextInt(1000, 10000).toString()
            )

            createTripUseCase(trip).fold(
                onSuccess = { tripId ->
                    _uiState.update { it.copy(isSubmitting = false, createdTripId = tripId) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = if (e is AppError.Validation.InvalidTrip) {
                                CreateTripError.InvalidTrip
                            } else {
                                CreateTripError.SubmitError
                            }
                        )
                    }
                }
            )
        }
    }
}
