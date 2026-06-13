package com.juanpablo0612.tucargo.data.document

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.data.common.safeCall
import com.juanpablo0612.tucargo.data.user.UserDto
import com.juanpablo0612.tucargo.domain.model.KycDocument
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.model.KycStatus
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
        private const val KYC_DOCUMENTS_FIELD = "kyc_documents"
    }

    override suspend fun uploadDocuments(
        userId: String,
        frontBytes: ByteArray,
        backBytes: ByteArray
    ): Result<Unit> = safeCall {
        withContext(dispatchers.io) {
            coroutineScope {
                val frontRef = storage.reference.child("$DOCUMENTS_PATH/$userId/front.jpg")
                val backRef = storage.reference.child("$DOCUMENTS_PATH/$userId/back.jpg")

                val frontUpload = async { frontRef.putData(frontBytes.toStorageData()) }
                val backUpload = async { backRef.putData(backBytes.toStorageData()) }

                frontUpload.await()
                backUpload.await()

                val frontUrl = frontRef.getDownloadUrl()
                val backUrl = backRef.getDownloadUrl()

                val kycDocuments = listOf(
                    KycDocumentDto(id = "id_front", type = "ID_FRONT", imageUrl = frontUrl),
                    KycDocumentDto(id = "id_back", type = "ID_BACK", imageUrl = backUrl)
                )

                try {
                    firestore.collection(USERS_COLLECTION).document(userId).update(
                        mapOf(KYC_DOCUMENTS_FIELD to kycDocuments)
                    )
                } catch (e: Exception) {
                    try { frontRef.delete() } catch (_: Exception) {}
                    try { backRef.delete() } catch (_: Exception) {}
                    throw e
                }
            }
        }
    }

    override suspend fun uploadDocument(
        userId: String,
        type: KycDocumentType,
        imageBytes: ByteArray
    ): Result<Unit> = safeCall {
        withContext(dispatchers.io) {
            val fileName = "${type.name.lowercase()}.jpg"
            val ref = storage.reference.child("$DOCUMENTS_PATH/$userId/$fileName")
            ref.putData(imageBytes.toStorageData())
            val url = ref.getDownloadUrl()

            val newDto = KycDocumentDto(
                id = type.name.lowercase(),
                type = type.name,
                imageUrl = url
            )

            val docRef = firestore.collection(USERS_COLLECTION).document(userId)
            val existing = runCatching {
                docRef.get().data<UserDto>().kycDocuments ?: emptyList()
            }.getOrElse { emptyList() }

            val updated = existing.filter { it.type != type.name } + newDto
            try {
                docRef.update(mapOf(KYC_DOCUMENTS_FIELD to updated))
            } catch (e: Exception) {
                try { ref.delete() } catch (_: Exception) {}
                throw e
            }
        }
    }

    override suspend fun getDocumentsForUser(userId: String): Result<List<KycDocument>> = safeCall {
        withContext(dispatchers.io) {
            val dto = firestore.collection(USERS_COLLECTION).document(userId).get().data<UserDto>()
            (dto.kycDocuments ?: emptyList()).map { it.toDomain() }
        }
    }

    override fun observeDocumentsForUser(userId: String): Flow<List<KycDocument>> =
        kycDocumentsCollection(userId).snapshots
            .map { snapshot -> snapshot.documents.map { it.data<KycDocumentDto>().toDomain() } }

    override suspend fun updateDocumentStatus(
        userId: String,
        type: KycDocumentType,
        status: KycStatus,
        rejectionReason: String?
    ): Result<Unit> = safeCall {
        withContext(dispatchers.io) {
            kycDocumentsCollection(userId).document(type.name.lowercase()).update(
                mapOf(
                    "status" to status.name,
                    "rejection_reason" to rejectionReason
                )
            )
        }
    }
        firestore.collection(USERS_COLLECTION).document(userId).snapshots
            .map { snap ->
                runCatching {
                    val dto = snap.data<UserDto>()
                    (dto.kycDocuments ?: emptyList()).map { it.toDomain() }
                }.getOrElse { emptyList() }
            }
}
