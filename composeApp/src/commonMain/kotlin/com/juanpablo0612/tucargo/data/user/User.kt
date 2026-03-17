// RUTA: composeApp/src/commonMain/kotlin/com/juanpablo0612/tucargo/data/user/User.kt

package com.juanpablo0612.tucargo.data.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val email: String = "",
    @SerialName("full_name")
    val fullName: String = "",
    val phone: String = "",
    val role: String = "CLIENT",
    @SerialName("fcm_token")
    val fcmToken: String = "",

    // Campos exclusivos de conductor
    @SerialName("is_online")
    val isOnline: Boolean = false,
    @SerialName("is_verified")
    val isVerified: Boolean = false,
    @SerialName("wallet_balance")
    val walletBalance: Double = 0.0,
    @SerialName("current_trip_id")
    val currentTripId: String? = null,
    val rating: Double = 0.0,
    val vehicle: Vehicle? = null
)

@Serializable
data class Vehicle(
    val plate: String = "",
    val model: String = "",
    val color: String = ""
)

@Serializable
data class KycDocument(
    val id: String = "",
    val type: String = "",
    @SerialName("image_url")
    val imageUrl: String = "",
    val status: String = "PENDING",
    @SerialName("rejection_reason")
    val rejectionReason: String? = null
)

@Serializable
data class WalletTransaction(
    val id: String = "",
    val amount: Double = 0.0,
    val type: String = "",
    @SerialName("reference_id")
    val referenceId: String = "",
    val description: String = "",
    val status: String = "COMPLETED"
)
