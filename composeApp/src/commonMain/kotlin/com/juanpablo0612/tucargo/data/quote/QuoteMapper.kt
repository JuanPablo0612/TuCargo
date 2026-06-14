package com.juanpablo0612.tucargo.data.quote

import com.juanpablo0612.tucargo.domain.model.Cop
import com.juanpablo0612.tucargo.domain.model.QuoteResult

fun QuoteDto.toDomain(): QuoteResult = QuoteResult(
    id = id,
    distanceKm = distanceKm,
    polyline = polyline,
    baseFare = Cop(baseFare),
    perKmFare = Cop(perKmFare),
    totalPrice = Cop(totalPrice),
    commissionFee = Cop(commissionFee),
    originLat = originLat,
    originLng = originLng,
    originAddr = originAddr,
    destLat = destLat,
    destLng = destLng,
    destAddr = destAddr,
    validUntil = validUntil
)
