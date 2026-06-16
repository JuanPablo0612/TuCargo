package com.juanpablo0612.tucargo.features.auth.documents

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.domain.model.KycDocument

@Immutable
data class KycPendingState(
    val isLoading: Boolean = true,
    val documents: List<KycDocument> = emptyList(),
    val isVerified: Boolean = false
)
