package com.juanpablo0612.tucargo.core.ui

fun Double.toCurrencyString(): String {
    val cents = kotlin.math.round(this * 100).toLong()
    val intPart = cents / 100
    val decPart = kotlin.math.abs(cents % 100)
    return "$$intPart.${decPart.toString().padStart(2, '0')}"
}

fun Double.toDistanceString(): String {
    val tenths = kotlin.math.round(this * 10).toLong()
    return "${tenths / 10}.${kotlin.math.abs(tenths % 10)}"
}
