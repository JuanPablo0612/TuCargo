package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.data.document.DocumentRepository

class UploadDocumentsUseCase(private val documentRepository: DocumentRepository) {
    suspend operator fun invoke(
        userId: String,
        frontBytes: ByteArray,
        backBytes: ByteArray
    ): Result<Unit> = documentRepository.uploadDocuments(userId, frontBytes, backBytes)
}
