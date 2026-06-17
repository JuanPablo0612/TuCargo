package com.juanpablo0612.tucargo.data.quote

import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.Cop
import com.juanpablo0612.tucargo.domain.model.QuoteResult
import dev.gitlive.firebase.functions.FirebaseFunctions

class QuoteRepositoryImpl(private val functions: FirebaseFunctions) : QuoteRepository {

    override suspend fun createQuote(
        clientId: String,
        originLat: Double,
        originLng: Double,
        originAddr: String,
        destLat: Double,
        destLng: Double,
        destAddr: String
    ): Result<QuoteResult> = safeCall {
        val callable = functions.httpsCallable("createQuote")
        println("TuCargo: CF invoke — calling createQuote")
        val response = runCatching {
            callable.invoke(
                mapOf(
                    "originLat" to originLat,
                    "originLng" to originLng,
                    "originAddr" to originAddr,
                    "destLat" to destLat,
                    "destLng" to destLng,
                    "destAddr" to destAddr
                )
            )
        }.getOrElse { e ->
            println("TuCargo: CF invoke FAILED — ${e::class.qualifiedName}: ${e.message}")
            e.cause?.let { println("TuCargo: CF invoke cause — ${it::class.qualifiedName}: ${it.message}") }
            val msg = e.message ?: ""
            when {
                msg.contains("SAME_ORIGIN_DEST") -> throw AppError.Validation.SameOriginDest
                msg.contains("QUOTE_OUT_OF_RANGE") -> throw AppError.Validation.QuoteOutOfRange
                msg.contains("NO_ROUTE") -> throw AppError.Validation.NoRoute
                msg.contains("SERVICE_UNAVAILABLE") -> throw AppError.Validation.ServiceUnavailable
                else -> throw e
            }
        }

        val data = response.data<Map<String, Any?>?>() ?: throw AppError.DataCorruption("Empty response from createQuote")

        QuoteResult(
            id = data["quoteId"] as? String ?: throw AppError.DataCorruption("Missing quoteId"),
            distanceKm = (data["distanceKm"] as? Number)?.toDouble() ?: 0.0,
            polyline = data["polyline"] as? String ?: "",
            baseFare = Cop((data["baseFare"] as? Number)?.toInt() ?: 0),
            perKmFare = Cop((data["perKmFare"] as? Number)?.toInt() ?: 0),
            totalPrice = Cop((data["totalPrice"] as? Number)?.toInt() ?: throw AppError.DataCorruption("Missing totalPrice")),
            commissionFee = Cop((data["commissionFee"] as? Number)?.toInt() ?: throw AppError.DataCorruption("Missing commissionFee")),
            originLat = originLat,
            originLng = originLng,
            originAddr = originAddr,
            destLat = destLat,
            destLng = destLng,
            destAddr = destAddr,
            validUntil = (data["validUntil"] as? Number)?.toLong() ?: throw AppError.DataCorruption("Missing validUntil")
        )
    }
}
