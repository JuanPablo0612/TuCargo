package com.juanpablo0612.tucargo.domain.usecase.document

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.data.document.DocumentRepository
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.withContext

private const val MAX_FILE_BYTES = 5 * 1024 * 1024 // 5 MB
// KYC docs only need to be legible for manual review; downscaling + JPEG
// re-encoding shrinks multi-MB camera shots to a few hundred KB, cutting both
// Storage bytes and the egress paid when an admin opens them.
private const val COMPRESSION_QUALITY = 80
private const val MAX_IMAGE_DIMENSION = 1600

class UploadKycDocumentUseCase(
    private val documentRepository: DocumentRepository,
    private val dispatchers: AppDispatchers
) {
    suspend operator fun invoke(
        userId: String,
        type: KycDocumentType,
        file: PlatformFile
    ): Result<Unit> = safeCall {
        val compressed = withContext(dispatchers.io) {
            val bytes = file.readBytes()
            if (bytes.isEmpty()) {
                throw AppError.Validation.EmptyFile
            }
            FileKit.compressImage(
                bytes = bytes,
                quality = COMPRESSION_QUALITY,
                maxWidth = MAX_IMAGE_DIMENSION,
                maxHeight = MAX_IMAGE_DIMENSION,
                imageFormat = ImageFormat.JPEG
            )
        }

        // Enforced against the compressed result (what actually gets uploaded),
        // keeping parity with the storage.rules size limit.
        if (compressed.size > MAX_FILE_BYTES) {
            throw AppError.Validation.FileTooLarge
        }

        documentRepository.uploadDocument(userId, type, compressed).getOrThrow()
    }
}
