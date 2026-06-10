package com.juanpablo0612.tucargo.data.document

import com.juanpablo0612.tucargo.core.coroutines.AppDispatchers
import com.juanpablo0612.tucargo.data.common.safeCall
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class DocumentRepositoryImpl(
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore,
    private val dispatchers: AppDispatchers
) : DocumentRepository {

    companion object {
        private const val DOCUMENTS_PATH = "documents"
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
                    firestore.collection("users").document(userId).update(
                        mapOf("kyc_documents" to kycDocuments)
                    )
                } catch (e: Exception) {
                    // Best-effort rollback
                    try { frontRef.delete() } catch(_: Exception) {}
                    try { backRef.delete() } catch(_: Exception) {}
                    throw e
                }
            }
        }
    }
}
