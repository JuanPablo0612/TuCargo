package com.juanpablo0612.tucargo.data.document

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KycDocumentDto(
    val id: String = "",
    val type: String = "",
    @SerialName("image_url")
    val imageUrl: String = "",
    val status: String = "PENDING",
    @SerialName("rejection_reason")
    val rejectionReason: String? = null
)
