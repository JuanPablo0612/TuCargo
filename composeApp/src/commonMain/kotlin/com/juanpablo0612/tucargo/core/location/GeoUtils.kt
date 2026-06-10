package com.juanpablo0612.tucargo.core.location

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GeoUtils {
    /**
     * Calcula la distancia en metros entre dos coordenadas usando la fórmula de Haversine.
     */
    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6_371_000.0 // Radio de la Tierra en metros
        val toRad = PI / 180.0
        val phi1 = lat1 * toRad
        val phi2 = lat2 * toRad
        val dPhi = (lat2 - lat1) * toRad
        val dLambda = (lon2 - lon1) * toRad
        val a = sin(dPhi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dLambda / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
