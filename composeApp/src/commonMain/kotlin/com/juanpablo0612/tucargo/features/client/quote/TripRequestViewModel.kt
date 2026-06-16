package com.juanpablo0612.tucargo.features.client.quote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.QuoteResult
import com.juanpablo0612.tucargo.domain.usecase.user.GetCurrentUserUseCase
import com.juanpablo0612.tucargo.domain.usecase.quote.RequestQuoteUseCase
import com.juanpablo0612.tucargo.domain.usecase.trip.RequestTripUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class QuoteError {
    SAME_ORIGIN_DEST,
    QUOTE_OUT_OF_RANGE,
    NO_ROUTE,
    SERVICE_UNAVAILABLE,
    TRIP_REQUEST_FAILED,
    UNKNOWN
}

data class QuoteUiState(
    val originLat: Double? = null,
    val originLng: Double? = null,
    val originAddr: String = "",
    val destLat: Double? = null,
    val destLng: Double? = null,
    val destAddr: String = "",
    val cargoDescription: String = "",
    val weightConfirmed: Boolean = false,
    val quote: QuoteResult? = null,
    val isLoading: Boolean = false,
    val error: QuoteError? = null,
    val createdTripId: String? = null,
    val deliveryCode: String? = null
)

class TripRequestViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val requestQuoteUseCase: RequestQuoteUseCase,
    private val requestTripUseCase: RequestTripUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuoteUiState())
    val uiState = _uiState.asStateFlow()

    fun confirmOrigin(lat: Double, lng: Double, addr: String) {
        _uiState.update {
            it.copy(originLat = lat, originLng = lng, originAddr = addr, error = null)
        }
    }

    fun confirmDest(lat: Double, lng: Double, addr: String) {
        _uiState.update {
            it.copy(destLat = lat, destLng = lng, destAddr = addr, error = null)
        }
    }

    fun confirmCargo(description: String, weightConfirmed: Boolean) {
        _uiState.update {
            it.copy(cargoDescription = description, weightConfirmed = weightConfirmed)
        }
    }

    fun requestQuote() {
        val state = _uiState.value
        val originLat = state.originLat ?: return
        val originLng = state.originLng ?: return
        val destLat = state.destLat ?: return
        val destLng = state.destLng ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val clientId = getCurrentUserUseCase().getOrNull()?.id ?: run {
                _uiState.update { it.copy(isLoading = false, error = QuoteError.UNKNOWN) }
                return@launch
            }
            requestQuoteUseCase(
                clientId = clientId,
                originLat = originLat,
                originLng = originLng,
                originAddr = state.originAddr,
                destLat = destLat,
                destLng = destLng,
                destAddr = state.destAddr
            ).fold(
                onSuccess = { quote ->
                    _uiState.update { it.copy(isLoading = false, quote = quote, error = null) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = when (e) {
                                is AppError.Validation.SameOriginDest -> QuoteError.SAME_ORIGIN_DEST
                                is AppError.Validation.QuoteOutOfRange -> QuoteError.QUOTE_OUT_OF_RANGE
                                is AppError.Validation.NoRoute -> QuoteError.NO_ROUTE
                                is AppError.Validation.ServiceUnavailable -> QuoteError.SERVICE_UNAVAILABLE
                                else -> QuoteError.UNKNOWN
                            }
                        )
                    }
                }
            )
        }
    }

    fun requestTrip(quoteId: String, cargoDescription: String, weightConfirmed: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, cargoDescription = cargoDescription, weightConfirmed = weightConfirmed) }
            requestTripUseCase(
                quoteId = quoteId,
                cargoDescription = cargoDescription,
                weightConfirmed = weightConfirmed
            ).fold(
                onSuccess = { (tripId, deliveryCode) ->
                    _uiState.update {
                        it.copy(isLoading = false, createdTripId = tripId, deliveryCode = deliveryCode)
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, error = QuoteError.TRIP_REQUEST_FAILED) }
                }
            )
        }
    }

    fun onNavigated() {
        _uiState.update { it.copy(createdTripId = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
