package com.juanpablo0612.tucargo.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val role: String = "CLIENT",
    val fullName: String = "",
    val phone: String = "",
    val isOnline: Boolean = false,
    val isVerified: Boolean = false,
    val walletBalance: Double = 0.0,
    val currentTripId: String? = null,
    val rating: Double = 0.0,
    val ratingAvg: Double = 0.0,
    val ratingCount: Int = 0,
    val status: String = "ACTIVE",
    val fcmToken: String = "",
    val vehicle: UserVehicle? = null
)

data class UserVehicle(
    val plate: String = "",
    val model: String = "",
    val color: String = ""
)
