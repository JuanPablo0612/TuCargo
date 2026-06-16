package com.juanpablo0612.tucargo.features.auth.driverdocs

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import io.github.vinceglb.filekit.PlatformFile

@Immutable
data class DriverDocsUploadState(
    val isLoading: Boolean = false,
    val documents: Map<KycDocumentType, PlatformFile?> = KycDocumentType.entries.associateWith { null },
    val documentErrors: Map<KycDocumentType, Boolean> = emptyMap(),
    val uploadError: DriverDocsError? = null,
    val isUploadComplete: Boolean = false
)
