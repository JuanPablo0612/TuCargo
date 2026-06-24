package com.juanpablo0612.tucargo.domain.model

data class DriverTaxProfile(
    val documentType: String,
    val documentNumber: String,
    val billingEmail: String,
    val residenceMunicipalityId: String,
    val ciiuCode: String
)
