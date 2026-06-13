package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.data.document.DocumentRepository
import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.model.KycStatus

class SetDriverVerifiedUseCase(
    private val userRepository: UserRepository,
    private val documentRepository: DocumentRepository
) {
    /**
     * Marks the driver as verified, but only after re-checking that every
     * required KYC document is APPROVED.
     */
    suspend operator fun invoke(driverId: String): Result<Unit> = safeCall {
        val documents = documentRepository.getDocumentsForUser(driverId).getOrThrow()
        val approvedTypes = documents
            .filter { it.status == KycStatus.APPROVED }
            .map { it.type }
            .toSet()
        if (!approvedTypes.containsAll(KycDocumentType.entries)) {
            throw AppError.Validation.KycIncomplete
        }
        userRepository.setDriverVerified(driverId, verified = true).getOrThrow()
    }
}
