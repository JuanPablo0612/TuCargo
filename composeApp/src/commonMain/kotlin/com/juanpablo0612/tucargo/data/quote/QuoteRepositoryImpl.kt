package com.juanpablo0612.tucargo.data.quote

import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.Cop
import com.juanpablo0612.tucargo.domain.model.QuoteResult
import dev.gitlive.firebase.functions.FirebaseFunctions
import kotlinx.serialization.Serializable

@Serializable
private data class CreateQuoteRequest(
    val originLat: Double,
    val originLng: Double,
    val originAddr: String,
    val destLat: Double,
    val destLng: Double,
    val destAddr: String
)

@Serializable
private data class CreateQuoteResponse(
    val quoteId: String = "",
    val distanceKm: Double = 0.0,
    val polyline: String = "",
    val baseFare: Int = 0,
    val perKmFare: Int = 0,
    val totalPrice: Int = 0,
    val commissionFee: Int = 0,
    val validUntil: Long = 0L
)

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
                CreateQuoteRequest(
                    originLat = originLat,
                    originLng = originLng,
                    originAddr = originAddr,
                    destLat = destLat,
                    destLng = destLng,
                    destAddr = destAddr
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

        val data = response.data<CreateQuoteResponse>()
        if (data.quoteId.isEmpty()) throw AppError.DataCorruption("Missing quoteId")
        if (data.totalPrice == 0) throw AppError.DataCorruption("Missing totalPrice")
        if (data.validUntil == 0L) throw AppError.DataCorruption("Missing validUntil")

        QuoteResult(
            id = data.quoteId,
            distanceKm = data.distanceKm,
            polyline = data.polyline,
            baseFare = Cop(data.baseFare),
            perKmFare = Cop(data.perKmFare),
            totalPrice = Cop(data.totalPrice),
            commissionFee = Cop(data.commissionFee),
            originLat = originLat,
            originLng = originLng,
            originAddr = originAddr,
            destLat = destLat,
            destLng = destLng,
            destAddr = destAddr,
            validUntil = data.validUntil
        )
    }
}
