package com.juanpablo0612.tucargo.data.user

import com.juanpablo0612.tucargo.domain.model.User
import com.juanpablo0612.tucargo.domain.model.UserVehicle

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    role = role,
    fullName = fullName,
    phone = phone,
    isOnline = isOnline,
    isVerified = isVerified,
    walletBalance = walletBalance,
    currentTripId = currentTripId,
    rating = rating,
    ratingAvg = ratingAvg,
    ratingCount = ratingCount,
    status = status,
    fcmToken = fcmToken,
    vehicle = vehicle?.let { UserVehicle(plate = it.plate, model = it.model, color = it.color) }
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    email = email,
    role = role,
    fullName = fullName,
    phone = phone,
    isOnline = isOnline,
    isVerified = isVerified,
    walletBalance = walletBalance,
    currentTripId = currentTripId,
    rating = rating,
    ratingAvg = ratingAvg,
    ratingCount = ratingCount,
    status = status,
    fcmToken = fcmToken,
    vehicle = vehicle?.let { Vehicle(plate = it.plate, model = it.model, color = it.color) }
)
