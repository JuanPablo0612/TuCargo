package com.juanpablo0612.tucargo.domain.usecase.quote

import com.juanpablo0612.tucargo.data.quote.QuoteRepository
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.QuoteResult

class RequestQuoteUseCase(private val quoteRepository: QuoteRepository) {

    suspend operator fun invoke(
        clientId: String,
        originLat: Double,
        originLng: Double,
        originAddr: String,
        destLat: Double,
        destLng: Double,
        destAddr: String
    ): Result<QuoteResult> {
        if (originLat == destLat && originLng == destLng) {
            return Result.failure(AppError.Validation.SameOriginDest)
        }
        return quoteRepository.createQuote(
            clientId = clientId,
            originLat = originLat,
            originLng = originLng,
            originAddr = originAddr,
            destLat = destLat,
            destLng = destLng,
            destAddr = destAddr
        )
    }
}
