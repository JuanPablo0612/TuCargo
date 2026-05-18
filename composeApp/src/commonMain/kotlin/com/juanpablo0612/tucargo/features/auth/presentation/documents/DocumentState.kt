package com.juanpablo0612.tucargo.features.auth.presentation.documents

import androidx.compose.runtime.Immutable
import com.juanpablo0612.tucargo.core.ui.event.UiEvent
import io.github.vinceglb.filekit.PlatformFile

@Immutable
data class DocumentState(
    val isLoading: Boolean = false,
    val idFront: PlatformFile? = null,
    val idBack: PlatformFile? = null,
    val error: DocumentError? = null,
    val navigationEvent: UiEvent<Unit>? = null
)

sealed interface DocumentAction {
    data class OnFrontPhotoSelected(val file: PlatformFile) : DocumentAction
    data class OnBackPhotoSelected(val file: PlatformFile) : DocumentAction
    data object OnSubmit : DocumentAction
    data object OnBackClick : DocumentAction
}
