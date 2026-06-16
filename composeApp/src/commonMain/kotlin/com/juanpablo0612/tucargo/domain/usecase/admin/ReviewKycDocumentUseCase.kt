package com.juanpablo0612.tucargo.domain.usecase.admin

import com.juanpablo0612.tucargo.data.document.DocumentRepository
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.model.KycStatus

class ReviewKycDocumentUseCase(private val documentRepository: DocumentRepository) {
    suspend operator fun invoke(
        userId: String,
        type: KycDocumentType,
        approve: Boolean,
        rejectionReason: String? = null
    ): Result<Unit> = documentRepository.updateDocumentStatus(
        userId = userId,
        type = type,
        status = if (approve) KycStatus.APPROVED else KycStatus.REJECTED,
        rejectionReason = rejectionReason.takeUnless { approve }
    )
}
