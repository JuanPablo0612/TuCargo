package com.juanpablo0612.tucargo.features.auth.presentation.driverdocs

import com.juanpablo0612.tucargo.domain.model.KycDocumentType
import io.github.vinceglb.filekit.PlatformFile

sealed interface DriverDocsAction {
    data class OnDocumentSelected(val type: KycDocumentType, val file: PlatformFile) : DriverDocsAction
    data object OnSubmit : DriverDocsAction
    data object OnBackClick : DriverDocsAction
}
