package com.juanpablo0612.tucargo.domain.usecase.user

import com.juanpablo0612.tucargo.data.document.DocumentRepository
import com.juanpablo0612.tucargo.data.user.UserRepository
import com.juanpablo0612.tucargo.domain.model.DriverOnboardingStatus
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.model.KycStatus

class GetDriverOnboardingStatusUseCase(
    private val userRepository: UserRepository,
    private val documentRepository: DocumentRepository
) {
    private val requiredDocTypes = KycDocumentType.entries.toList()

    suspend operator fun invoke(): Result<DriverOnboardingStatus> = runCatching {
        val user = userRepository.getCurrentUser().getOrThrow()
        if (user.isVerified) return@runCatching DriverOnboardingStatus.Verified
        if (user.vehicle == null) return@runCatching DriverOnboardingStatus.IncompleteVehicle
        val docs = documentRepository.getDocumentsForUser(user.id).getOrThrow()
        val uploadedTypes = docs
            .filter { it.status != KycStatus.REJECTED }
            .map { it.type }
        val missing = requiredDocTypes.filter { it !in uploadedTypes }
        if (missing.isNotEmpty()) DriverOnboardingStatus.IncompleteDocs(missing)
        else DriverOnboardingStatus.PendingReview
    }
}
