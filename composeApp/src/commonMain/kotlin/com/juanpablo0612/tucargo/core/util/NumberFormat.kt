package com.juanpablo0612.tucargo.core.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

fun Double.roundToDecimals(decimals: Int): String {
    val factor = 10.0.pow(decimals).toLong()
    val rounded = (this * factor).roundToLong()
    val intPart = rounded / factor
    val fracPart = abs(rounded % factor).toString().padStart(decimals, '0')
    return "$intPart.$fracPart"
}
