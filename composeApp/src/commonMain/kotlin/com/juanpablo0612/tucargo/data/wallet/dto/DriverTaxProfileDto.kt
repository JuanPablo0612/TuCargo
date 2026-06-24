package com.juanpablo0612.tucargo.data.wallet.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DriverTaxProfileDto(
    @SerialName("document_type")
    val documentType: String = "",
    @SerialName("document_number")
    val documentNumber: String = "",
    @SerialName("billing_email")
    val billingEmail: String = "",
    @SerialName("residence_municipality_id")
    val residenceMunicipalityId: String = "",
    @SerialName("ciiu_code")
    val ciiuCode: String = ""
)
