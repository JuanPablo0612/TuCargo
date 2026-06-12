package com.juanpablo0612.tucargo.features.admin.review

import com.juanpablo0612.tucargo.domain.model.KycDocument
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.model.KycStatus
import com.juanpablo0612.tucargo.domain.usecase.ObserveKycDocumentsUseCase
import com.juanpablo0612.tucargo.domain.usecase.ReviewKycDocumentUseCase
import com.juanpablo0612.tucargo.domain.usecase.SetDriverVerifiedUseCase
import com.juanpablo0612.tucargo.testutil.FakeDocumentRepository
import com.juanpablo0612.tucargo.testutil.FakeUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AdminDriverReviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var documentRepository: FakeDocumentRepository
    private lateinit var userRepository: FakeUserRepository

    private fun docs(status: (KycDocumentType) -> KycStatus): List<KycDocument> =
        KycDocumentType.entries.map { type ->
            KycDocument(id = type.name.lowercase(), type = type, status = status(type))
        }

    private fun buildViewModel() = AdminDriverReviewViewModel(
        driverId = "d1",
        observeKycDocumentsUseCase = ObserveKycDocumentsUseCase(documentRepository),
        reviewKycDocumentUseCase = ReviewKycDocumentUseCase(documentRepository),
        setDriverVerifiedUseCase = SetDriverVerifiedUseCase(userRepository, documentRepository)
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        documentRepository = FakeDocumentRepository()
        userRepository = FakeUserRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun verify_isBlockedWhileDocumentsArePending() = runTest {
        val pendingDocs = docs { if (it == KycDocumentType.SOAT) KycStatus.PENDING else KycStatus.APPROVED }
        documentRepository.documentsFlow.value = pendingDocs
        documentRepository.documentsResult = Result.success(pendingDocs)
        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.allDocumentsApproved)

        viewModel.onAction(AdminDriverReviewAction.VerifyDriver)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(userRepository.lastVerifiedDriver)
        assertFalse(viewModel.uiState.value.isDriverVerified)
    }

    @Test
    fun verify_succeedsWhenAllDocumentsAreApproved() = runTest {
        val approvedDocs = docs { KycStatus.APPROVED }
        documentRepository.documentsFlow.value = approvedDocs
        documentRepository.documentsResult = Result.success(approvedDocs)
        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.allDocumentsApproved)

        viewModel.onAction(AdminDriverReviewAction.VerifyDriver)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("d1" to true, userRepository.lastVerifiedDriver)
        assertTrue(viewModel.uiState.value.isDriverVerified)
    }

    @Test
    fun reject_passesReasonToTheRepository() = runTest {
        documentRepository.documentsFlow.value = docs { KycStatus.PENDING }
        val viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(AdminDriverReviewAction.Reject(KycDocumentType.SOAT, "Blurry photo"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            Triple(KycDocumentType.SOAT, KycStatus.REJECTED, "Blurry photo"),
            documentRepository.lastStatusUpdate
        )
    }
}
