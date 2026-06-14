package com.juanpablo0612.tucargo.data.quote

import com.juanpablo0612.tucargo.domain.model.QuoteResult

interface QuoteRepository {
    suspend fun createQuote(
        clientId: String,
        originLat: Double,
        originLng: Double,
        originAddr: String,
        destLat: Double,
        destLng: Double,
        destAddr: String
    ): Result<QuoteResult>
}
