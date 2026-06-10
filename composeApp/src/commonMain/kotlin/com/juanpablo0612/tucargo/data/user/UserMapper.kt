package com.juanpablo0612.tucargo.data.user

import com.juanpablo0612.tucargo.domain.model.User
import com.juanpablo0612.tucargo.domain.model.UserRole
import com.juanpablo0612.tucargo.domain.model.UserStatus
import com.juanpablo0612.tucargo.domain.model.UserVehicle

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    role = try { UserRole.valueOf(role) } catch (e: Exception) { UserRole.CLIENT },
    fullName = fullName,
    phone = phone,
    isOnline = isOnline,
    isVerified = isVerified,
    walletBalance = walletBalance,
    currentTripId = currentTripId,
    ratingAvg = ratingAvg,
    ratingCount = ratingCount,
    status = try { UserStatus.valueOf(status) } catch (e: Exception) { UserStatus.ACTIVE },
    fcmToken = fcmToken,
    vehicle = vehicle?.let { UserVehicle(plate = it.plate, model = it.model, color = it.color) }
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
    vehicle = vehicle?.let { Vehicle(plate = it.plate, model = it.model, color = it.color) }
)
