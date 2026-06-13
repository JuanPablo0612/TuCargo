package com.juanpablo0612.tucargo.domain.usecase

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.data.document.DocumentRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.withContext

class UploadDocumentsUseCase(
    private val documentRepository: DocumentRepository,
    private val dispatchers: AppDispatchers
) {
    suspend operator fun invoke(
        userId: String,
        frontFile: PlatformFile,
        backFile: PlatformFile
    ): Result<Unit> = withContext(dispatchers.io) {
        val frontBytes = frontFile.readBytes()
        val backBytes = backFile.readBytes()
        documentRepository.uploadDocuments(userId, frontBytes, backBytes)
    }
}
