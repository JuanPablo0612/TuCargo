package com.juanpablo0612.tucargo.core.ui

import com.juanpablo0612.tucargo.domain.model.formatCopAmount
import kotlin.math.roundToLong

// All money in the app is whole Colombian pesos; delegate to the single COP formatter
// (`"$ 12.345"`) so prices and balances render identically everywhere.
fun Double.toCurrencyString(): String = formatCopAmount(this.roundToLong())
fun Int.toCurrencyString(): String = formatCopAmount(this.toLong())
fun Long.toCurrencyString(): String = formatCopAmount(this)

fun Double.toDistanceString(): String {
    val tenths = kotlin.math.round(this * 10).toLong()
    return "${tenths / 10}.${kotlin.math.abs(tenths % 10)}"
}
