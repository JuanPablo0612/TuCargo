package com.juanpablo0612.tucargo.data.user

import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.User
import com.juanpablo0612.tucargo.domain.model.UserRole
import com.juanpablo0612.tucargo.domain.model.UserStatus
import com.juanpablo0612.tucargo.domain.model.UserVehicle
import com.juanpablo0612.tucargo.domain.model.VehicleType

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    // Strict on purpose: silently defaulting a mis-read DRIVER/ADMIN to
    // CLIENT would route the user to the wrong home screen.
    role = try {
        UserRole.valueOf(role)
    } catch (e: IllegalArgumentException) {
        throw AppError.DataCorruption("Unknown user role '$role' for user $id")
    },
    fullName = fullName,
    phone = phone,
    isOnline = isOnline,
    isVerified = isVerified,
    walletBalance = walletBalance,
    currentTripId = currentTripId,
    ratingAvg = ratingAvg,
    ratingCount = ratingCount,
    status = try {
        UserStatus.valueOf(status)
    } catch (e: IllegalArgumentException) {
        logError("UserMapper", "Unknown user status '$status' for user $id, defaulting to ACTIVE")
        UserStatus.ACTIVE
    },
    fcmToken = fcmToken,
    vehicle = vehicle?.let {
        UserVehicle(
            plate = it.plate,
            model = it.model,
            color = it.color,
            year = it.year,
            type = try { VehicleType.valueOf(it.type) } catch (e: Exception) { VehicleType.MOTORCYCLE }
        )
    }
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    email = email,
    role = role.name,
    fullName = fullName,
    phone = phone,
    isOnline = isOnline,
    isVerified = isVerified,
    walletBalance = walletBalance,
    currentTripId = currentTripId,
    ratingAvg = ratingAvg,
    ratingCount = ratingCount,
    status = status.name,
    fcmToken = fcmToken,
    vehicle = vehicle?.let {
        Vehicle(
            plate = it.plate,
            model = it.model,
            color = it.color,
            year = it.year,
            type = it.type.name
        )
    }
)
