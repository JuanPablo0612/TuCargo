package com.juanpablo0612.tucargo.core.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class FormValidatorsTest {

    @Test
    fun email_validates_correctly() {
        assertNull(FormValidators.email("test@example.com"))
        assertEquals(FieldError.EmailRequired, FormValidators.email(""))
        assertEquals(FieldError.EmailInvalid, FormValidators.email("invalid-email"))
    }

    @Test
    fun password_validates_correctly() {
        // Assuming PasswordValidator.isValid requires 6 chars + letter + digit
        assertNull(FormValidators.password("pass123"))
        assertEquals(FieldError.PasswordRequired, FormValidators.password(""))
        assertEquals(FieldError.PasswordTooShort, FormValidators.password("12345"))
        assertEquals(FieldError.PasswordWeak, FormValidators.password("password"))
    }

    @Test
    fun phone_validates_correctly() {
        assertNull(FormValidators.phone("+573001234567"))
        assertEquals(FieldError.PhoneRequired, FormValidators.phone(""))
        assertEquals(FieldError.PhoneInvalid, FormValidators.phone("1234567"))
    }
}
