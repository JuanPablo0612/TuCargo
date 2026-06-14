package com.juanpablo0612.tucargo.data.trip

import com.juanpablo0612.tucargo.domain.model.Cop
import com.juanpablo0612.tucargo.domain.model.OfferResponse
import com.juanpablo0612.tucargo.domain.model.TripOffer

fun TripOfferDto.toDomain(): TripOffer = TripOffer(
    id = id,
    tripId = tripId,
    driverId = driverId,
    attempt = attempt,
    sentAt = sentAt,
    expiresAt = expiresAt,
    response = try { OfferResponse.valueOf(response) } catch (e: Exception) { OfferResponse.PENDING },
    totalPrice = Cop(totalPrice),
    commissionFee = Cop(commissionFee),
    distanceKm = distanceKm,
    originAddr = originAddr,
    destAddr = destAddr
)
