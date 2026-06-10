package com.juanpablo0612.tucargo.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CLIENT,
    val fullName: String = "",
    val phone: String = "",
    val isOnline: Boolean = false,
    val isVerified: Boolean = false,
    val walletBalance: Double = 0.0,
    val currentTripId: String? = null,
    val ratingAvg: Double = 0.0,
    val ratingCount: Int = 0,
    val status: UserStatus = UserStatus.ACTIVE,
    val fcmToken: String = "",
    val vehicle: UserVehicle? = null
)

enum class UserRole {
    CLIENT,
    DRIVER
}

enum class UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}

data class UserVehicle(
    val plate: String = "",
    val model: String = "",
    val color: String = ""
)
