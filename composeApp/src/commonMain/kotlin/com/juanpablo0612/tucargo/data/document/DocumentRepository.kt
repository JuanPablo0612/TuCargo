package com.juanpablo0612.tucargo.data.document

interface DocumentRepository {
    suspend fun uploadDocuments(
        userId: String,
        frontBytes: ByteArray,
        backBytes: ByteArray
    ): Result<Unit>
}
