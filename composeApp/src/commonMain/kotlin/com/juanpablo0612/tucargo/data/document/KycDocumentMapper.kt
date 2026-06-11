package com.juanpablo0612.tucargo.data.document

import com.juanpablo0612.tucargo.domain.model.KycDocument
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.model.KycStatus

fun KycDocumentDto.toDomain(): KycDocument = KycDocument(
    id = id,
    type = try { KycDocumentType.valueOf(type) } catch (e: Exception) { KycDocumentType.ID_FRONT },
    imageUrl = imageUrl,
    status = try { KycStatus.valueOf(status) } catch (e: Exception) { KycStatus.PENDING },
    rejectionReason = rejectionReason
)
