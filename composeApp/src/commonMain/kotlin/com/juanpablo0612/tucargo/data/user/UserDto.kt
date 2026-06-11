package com.juanpablo0612.tucargo.data.user

import com.juanpablo0612.tucargo.data.document.KycDocumentDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String = "",
    val email: String = "",
    val role: String = "CLIENT",
    @SerialName("full_name")
    val fullName: String = "",
    val phone: String = "",
    @SerialName("is_online")
    val isOnline: Boolean = false,
    @SerialName("is_verified")
    val isVerified: Boolean = false,
    @SerialName("wallet_balance")
    val walletBalance: Double = 0.0,
    @SerialName("current_trip_id")
    val currentTripId: String? = null,
    @SerialName("rating_avg")
    val ratingAvg: Double = 0.0,
    @SerialName("rating_count")
    val ratingCount: Int = 0,
    val status: String = "ACTIVE",
    @SerialName("fcm_token")
    val fcmToken: String = "",
    val vehicle: Vehicle? = null,
    @SerialName("kyc_documents")
    val kycDocuments: List<KycDocumentDto>? = null
)

@Serializable
data class Vehicle(
    val plate: String = "",
    val model: String = "",
    val color: String = "",
    val year: Int = 0,
    val type: String = "MOTORCYCLE"
)
