package com.juanpablo0612.tucargo.features.admin.review

import com.juanpablo0612.tucargo.domain.model.KycDocumentType

sealed interface AdminDriverReviewAction {
    data class Approve(val type: KycDocumentType) : AdminDriverReviewAction
    data class Reject(val type: KycDocumentType, val reason: String) : AdminDriverReviewAction
    data object VerifyDriver : AdminDriverReviewAction
    data object Refresh : AdminDriverReviewAction
}
