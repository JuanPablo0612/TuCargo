package com.juanpablo0612.tucargo.data.trip

import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.domain.model.CancelledBy
import com.juanpablo0612.tucargo.domain.model.PaymentMethod
import com.juanpablo0612.tucargo.domain.model.Trip
import com.juanpablo0612.tucargo.domain.model.TripLocation
import com.juanpablo0612.tucargo.domain.model.TripStatus

fun TripDto.toDomain(): Trip = Trip(
    id = id,
    status = try {
        TripStatus.valueOf(status)
    } catch (e: IllegalArgumentException) {
        logError("TripMapper", "Unknown trip status '$status' for trip $id, defaulting to REQUESTED")
        TripStatus.REQUESTED
    },
    createdAt = createdAt,
    completedAt = completedAt,
    clientId = clientId,
    clientName = clientName,
    clientPhone = clientPhone,
    driverId = driverId,
    driverName = driverName,
    driverPlate = driverPlate,
    driverPhone = driverPhone,
    driverLastLat = driverLastLat,
    driverLastLng = driverLastLng,
    priceTotal = priceTotal,
    priceBase = priceBase,
    priceDistance = priceDistance,
    commissionFee = commissionFee,
    paymentMethod = try {
        PaymentMethod.valueOf(paymentMethod)
    } catch (e: IllegalArgumentException) {
        logError("TripMapper", "Unknown payment method '$paymentMethod' for trip $id, defaulting to CASH")
        PaymentMethod.CASH
    },
    origin = origin.toDomain(),
    destination = destination.toDomain(),
    distanceKm = distanceKm,
    cargoDescription = cargoDescription,
    deliveryCode = deliveryCode,
    deliveryCodeAttempts = deliveryCodeAttempts,
    arrivedPickupAt = arrivedPickupAt,
    startedAt = startedAt,
    arrivedDropoffAt = arrivedDropoffAt,
    deliveryCodeVerifiedAt = deliveryCodeVerifiedAt,
    quoteId = quoteId,
    cancelledBy = cancelledBy?.let {
        try {
            CancelledBy.valueOf(it)
        } catch (e: IllegalArgumentException) {
            logError("TripMapper", "Unknown cancelledBy '$it' for trip $id, defaulting to null")
            null
        }
    }
)

fun TripLocationDto.toDomain(): TripLocation = TripLocation(
    address = address,
    lat = lat,
    lng = lng
)

fun Trip.toDto(): TripDto = TripDto(
    id = id,
    status = status.name,
    createdAt = createdAt,
    completedAt = completedAt,
    clientId = clientId,
    clientName = clientName,
    clientPhone = clientPhone,
    driverId = driverId,
    driverName = driverName,
    driverPlate = driverPlate,
    driverPhone = driverPhone,
    driverLastLat = driverLastLat,
    driverLastLng = driverLastLng,
    priceTotal = priceTotal,
    priceBase = priceBase,
    priceDistance = priceDistance,
    commissionFee = commissionFee,
    paymentMethod = paymentMethod.name,
    origin = origin.toDto(),
    destination = destination.toDto(),
    distanceKm = distanceKm,
    cargoDescription = cargoDescription,
    deliveryCode = deliveryCode,
    deliveryCodeAttempts = deliveryCodeAttempts,
    arrivedPickupAt = arrivedPickupAt,
    startedAt = startedAt,
    arrivedDropoffAt = arrivedDropoffAt,
    deliveryCodeVerifiedAt = deliveryCodeVerifiedAt,
    quoteId = quoteId,
    cancelledBy = cancelledBy?.name
)

fun TripLocation.toDto(): TripLocationDto = TripLocationDto(
    address = address,
    lat = lat,
    lng = lng
)
