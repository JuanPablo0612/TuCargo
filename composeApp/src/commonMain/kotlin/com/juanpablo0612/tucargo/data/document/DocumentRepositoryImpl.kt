package com.juanpablo0612.tucargo.data.document

import com.juanpablo0612.tucargo.data.common.safeCall
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.storage.FirebaseStorage

class DocumentRepositoryImpl(
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore
) : DocumentRepository {

    override suspend fun uploadDocuments(
        userId: String,
        frontBytes: ByteArray,
        backBytes: ByteArray
    ): Result<Unit> = safeCall {
        val frontRef = storage.reference.child("documents/$userId/front.jpg")
        val backRef = storage.reference.child("documents/$userId/back.jpg")

        frontRef.putData(frontBytes.toStorageData())
        backRef.putData(backBytes.toStorageData())

        val frontUrl = frontRef.getDownloadUrl()
        val backUrl = backRef.getDownloadUrl()

        val kycDocuments = listOf(
            KycDocumentDto(id = "id_front", type = "ID_FRONT", imageUrl = frontUrl),
            KycDocumentDto(id = "id_back", type = "ID_BACK", imageUrl = backUrl)
        )

        firestore.collection("users").document(userId).update(
            mapOf("kyc_documents" to kycDocuments)
        )
    }
}
