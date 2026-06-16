package com.juanpablo0612.tucargo.domain.usecase.document

import com.juanpablo0612.tucargo.data.document.DocumentRepository
import com.juanpablo0612.tucargo.domain.model.KycDocument
import kotlinx.coroutines.flow.Flow

class ObserveKycDocumentsUseCase(private val documentRepository: DocumentRepository) {
    operator fun invoke(userId: String): Flow<List<KycDocument>> =
        documentRepository.observeDocumentsForUser(userId)
}
