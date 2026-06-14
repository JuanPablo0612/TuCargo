package com.juanpablo0612.tucargo.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CopTest {

    @Test
    fun negativAmount_throwsIllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> { Cop(-1) }
    }

    @Test
    fun zero_formatsCorrectly() {
        assertEquals("$ 0", Cop(0).format())
    }

    @Test
    fun threeDigits_formatsWithoutSeparator() {
        assertEquals("$ 500", Cop(500).format())
    }

    @Test
    fun baseFare_formatsWithPeriodSeparator() {
        assertEquals("$ 35.000", Cop(35000).format())
    }

    @Test
    fun largeAmount_formatsCorrectly() {
        assertEquals("$ 155.000", Cop(155000).format())
    }

    @Test
    fun sixDigits_formatsCorrectly() {
        assertEquals("$ 100.000", Cop(100000).format())
    }

    @Test
    fun plus_addsTwoValues() {
        assertEquals(Cop(60000), Cop(35000) + Cop(25000))
    }

    @Test
    fun minus_subtractsValues() {
        assertEquals(Cop(25000), Cop(60000) - Cop(35000))
    }

    @Test
    fun minus_negativeResult_throwsIllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> { Cop(1000) - Cop(2000) }
    }
}
