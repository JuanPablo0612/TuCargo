package com.juanpablo0612.tucargo.data.document

import com.juanpablo0612.tucargo.core.logging.logError
import com.juanpablo0612.tucargo.domain.model.KycDocument
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.model.KycStatus

fun KycDocumentDto.toDomain(): KycDocument = KycDocument(
    id = id,
    type = try {
        KycDocumentType.valueOf(type)
    } catch (e: IllegalArgumentException) {
        logError("KycDocumentMapper", "Unknown document type '$type' for document $id, defaulting to ID_FRONT")
        KycDocumentType.ID_FRONT
    },
    imageUrl = imageUrl,
    status = try {
        KycStatus.valueOf(status)
    } catch (e: IllegalArgumentException) {
        logError("KycDocumentMapper", "Unknown document status '$status' for document $id, defaulting to PENDING")
        KycStatus.PENDING
    },
    rejectionReason = rejectionReason
)
