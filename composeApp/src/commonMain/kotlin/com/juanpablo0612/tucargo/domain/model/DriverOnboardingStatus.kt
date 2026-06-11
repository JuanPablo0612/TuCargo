package com.juanpablo0612.tucargo.domain.model

sealed interface DriverOnboardingStatus {
    data object IncompleteVehicle : DriverOnboardingStatus
    data class IncompleteDocs(val missingTypes: List<KycDocumentType>) : DriverOnboardingStatus
    data object PendingReview : DriverOnboardingStatus
    data object Verified : DriverOnboardingStatus
}
