package com.juanpablo0612.tucargo.features.admin.review

sealed interface AdminDriverReviewError {
    data object LoadError : AdminDriverReviewError
    data object ReviewError : AdminDriverReviewError
    data object VerifyError : AdminDriverReviewError
}
