package com.juanpablo0612.tucargo.domain.model

enum class KycDocumentType {
    ID_FRONT,
    ID_BACK,
    DRIVER_LICENSE,
    SOAT,
    VEHICLE_TECH_REVIEW,
    VEHICLE_REGISTRATION_CARD
}

enum class KycStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class KycDocument(
    val id: String = "",
    val type: KycDocumentType,
    val imageUrl: String = "",
    val status: KycStatus = KycStatus.PENDING,
    val rejectionReason: String? = null
)
