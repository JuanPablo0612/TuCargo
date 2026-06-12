package com.juanpablo0612.tucargo.features.admin.review

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.domain.model.KycDocument
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import com.juanpablo0612.tucargo.domain.model.KycStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class AdminDriverReviewState(
    val isLoading: Boolean = true,
    val documents: ImmutableList<KycDocument> = persistentListOf(),
    val processingType: KycDocumentType? = null,
    val isVerifying: Boolean = false,
    val isDriverVerified: Boolean = false,
    val error: AdminDriverReviewError? = null
) {
    val allDocumentsApproved: Boolean
        get() = documents
            .filter { it.status == KycStatus.APPROVED }
            .map { it.type }
            .containsAll(KycDocumentType.entries)
}
