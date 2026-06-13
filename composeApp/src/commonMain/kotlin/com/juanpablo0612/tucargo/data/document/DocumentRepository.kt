package com.juanpablo0612.tucargo.data.document

import com.juanpablo0612.tucargo.domain.model.KycDocument
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.model.KycStatus
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    suspend fun uploadDocuments(
        userId: String,
        frontBytes: ByteArray,
        backBytes: ByteArray
    ): Result<Unit>

    suspend fun uploadDocument(
        userId: String,
        type: KycDocumentType,
        imageBytes: ByteArray
    ): Result<Unit>

    suspend fun getDocumentsForUser(userId: String): Result<List<KycDocument>>

    fun observeDocumentsForUser(userId: String): Flow<List<KycDocument>>

    // Admin-only (enforced by the Firestore rules).
    suspend fun updateDocumentStatus(
        userId: String,
        type: KycDocumentType,
        status: KycStatus,
        rejectionReason: String?
    ): Result<Unit>
}
