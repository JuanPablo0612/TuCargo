package com.juanpablo0612.tucargo.data.document

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.KycDocument
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.storage.FirebaseStorage
import dev.gitlive.firebase.storage.storageMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DocumentRepositoryImpl(
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers
) : DocumentRepository {

    companion object {
        private const val DOCUMENTS_PATH = "documents"
        private const val USERS_COLLECTION = "users"
        private const val KYC_DOCUMENTS_SUBCOLLECTION = "kyc_documents"
        // Must stay in sync with the size limit enforced in storage.rules.
        const val MAX_DOCUMENT_SIZE_BYTES = 5 * 1024 * 1024
    }

    private fun kycDocumentsCollection(userId: String) =
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(KYC_DOCUMENTS_SUBCOLLECTION)

    override suspend fun uploadDocument(
        userId: String,
        type: KycDocumentType,
        imageBytes: ByteArray
    ): Result<Unit> = safeCall {
        if (imageBytes.size > MAX_DOCUMENT_SIZE_BYTES) throw AppError.Validation.FileTooLarge
        withContext(dispatchers.io) {
            val docId = type.name.lowercase()
            val ref = storage.reference.child("$DOCUMENTS_PATH/$userId/$docId.jpg")
            ref.putData(
                imageBytes.toStorageData(),
                storageMetadata { contentType = "image/jpeg" }
            )
            val url = ref.getDownloadUrl()

            val dto = KycDocumentDto(
                id = docId,
                type = type.name,
                imageUrl = url,
                status = "PENDING",
                rejectionReason = null
            )

            try {
                // Full set (not merge) so a re-upload always resets the review
                // status to PENDING, as required by the Firestore rules.
                kycDocumentsCollection(userId).document(docId).set(dto)
            } catch (e: Exception) {
                try { ref.delete() } catch (_: Exception) {}
                throw e
            }
        }
    }

    override suspend fun getDocumentsForUser(userId: String): Result<List<KycDocument>> = safeCall {
        withContext(dispatchers.io) {
            kycDocumentsCollection(userId).get().documents
                .map { it.data<KycDocumentDto>().toDomain() }
        }
    }

    override fun observeDocumentsForUser(userId: String): Flow<List<KycDocument>> =
        kycDocumentsCollection(userId).snapshots
            .map { snapshot -> snapshot.documents.map { it.data<KycDocumentDto>().toDomain() } }
}
