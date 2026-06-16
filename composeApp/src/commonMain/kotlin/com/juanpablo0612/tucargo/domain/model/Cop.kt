package com.juanpablo0612.tucargo.domain.model

import kotlin.jvm.JvmInline

@JvmInline
value class Cop(val amount: Int) {

    init {
        require(amount >= 0) { "Cop amount cannot be negative: $amount" }
    }

    fun format(): String {
        val s = amount.toString()
        val sb = StringBuilder("$ ")
        val remainder = s.length % 3
        if (remainder > 0) sb.append(s.substring(0, remainder))
        var i = remainder
        while (i < s.length) {
            if (sb.length > 2) sb.append('.')
            sb.append(s.substring(i, i + 3))
            i += 3
        }
        return sb.toString()
    }

    operator fun plus(other: Cop): Cop = Cop(amount + other.amount)

    operator fun minus(other: Cop): Cop {
        val result = amount - other.amount
        require(result >= 0) { "Cop subtraction would produce a negative value: $amount - ${other.amount}" }
        return Cop(result)
    }
}
