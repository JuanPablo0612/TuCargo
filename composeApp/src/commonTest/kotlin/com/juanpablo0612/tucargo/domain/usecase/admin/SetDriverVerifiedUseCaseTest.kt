package com.juanpablo0612.tucargo.domain.usecase.admin

import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.KycDocument
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.model.KycStatus
import com.juanpablo0612.tucargo.testutil.FakeDocumentRepository
import com.juanpablo0612.tucargo.testutil.FakeUserRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SetDriverVerifiedUseCaseTest {

    private val userRepository = FakeUserRepository()
    private val documentRepository = FakeDocumentRepository()
    private val useCase = SetDriverVerifiedUseCase(userRepository, documentRepository)

    private fun docs(status: (KycDocumentType) -> KycStatus): List<KycDocument> =
        KycDocumentType.entries.map { type ->
            KycDocument(id = type.name.lowercase(), type = type, status = status(type))
        }

    @Test
    fun pendingDocument_blocksVerification() = runTest {
        documentRepository.documentsResult = Result.success(
            docs { if (it == KycDocumentType.SOAT) KycStatus.PENDING else KycStatus.APPROVED }
        )

        val result = useCase("d1")

        assertTrue(result.exceptionOrNull() is AppError.Validation.KycIncomplete)
        assertNull(userRepository.lastVerifiedDriver)
    }

    @Test
    fun missingDocument_blocksVerification() = runTest {
        documentRepository.documentsResult = Result.success(
            docs { KycStatus.APPROVED }.dropLast(1)
        )

        val result = useCase("d1")

        assertTrue(result.exceptionOrNull() is AppError.Validation.KycIncomplete)
        assertNull(userRepository.lastVerifiedDriver)
    }

    @Test
    fun allApproved_verifiesTheDriver() = runTest {
        documentRepository.documentsResult = Result.success(docs { KycStatus.APPROVED })

        val result = useCase("d1")

        assertTrue(result.isSuccess)
        assertEquals("d1" to true, userRepository.lastVerifiedDriver)
    }
}
