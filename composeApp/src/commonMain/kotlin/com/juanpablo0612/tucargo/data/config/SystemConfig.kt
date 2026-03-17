// RUTA: composeApp/src/commonMain/kotlin/com/juanpablo0612/tucargo/data/config/SystemConfig.kt

package com.juanpablo0612.tucargo.data.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SystemConfig(
    @SerialName("base_price")
    val basePrice: Double = 35000.0,
    @SerialName("base_km_included")
    val baseKmIncluded: Double = 1.0,
    @SerialName("price_per_km")
    val pricePerKm: Double = 5000.0,
    @SerialName("commission_percentage")
    val commissionPercentage: Double = 0.15,
    @SerialName("min_wallet_balance")
    val minWalletBalance: Double = 5000.0,
    @SerialName("android_version_min")
    val androidVersionMin: String = "1.0.0",
    @SerialName("maintenance_mode")
    val maintenanceMode: Boolean = false
)
