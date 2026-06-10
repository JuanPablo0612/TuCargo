package com.juanpablo0612.tucargo.domain.trip

import com.juanpablo0612.tucargo.domain.model.AppError

sealed interface TrackingState {
    data object Idle : TrackingState
    data class Tracking(val tripId: String) : TrackingState
    data class Error(val error: AppError) : TrackingState
}
