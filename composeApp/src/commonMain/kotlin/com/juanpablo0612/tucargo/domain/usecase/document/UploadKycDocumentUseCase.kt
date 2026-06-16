package com.juanpablo0612.tucargo.domain.usecase.document

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.data.document.DocumentRepository
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.withContext

private const val MAX_FILE_BYTES = 5 * 1024 * 1024 // 5 MB

class UploadKycDocumentUseCase(
    private val documentRepository: DocumentRepository,
    private val dispatchers: AppDispatchers
) {
    suspend operator fun invoke(
        userId: String,
        type: KycDocumentType,
        file: PlatformFile
    ): Result<Unit> = safeCall {
        val bytes = withContext(dispatchers.io) { file.readBytes() }

        if (bytes.isEmpty()) {
            throw AppError.Validation.InvalidTrip
        }
        if (bytes.size > MAX_FILE_BYTES) {
            throw AppError.Validation.FileTooLarge
        }

        documentRepository.uploadDocument(userId, type, bytes).getOrThrow()
    }
}
