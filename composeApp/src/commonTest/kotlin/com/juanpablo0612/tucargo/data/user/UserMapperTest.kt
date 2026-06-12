package com.juanpablo0612.tucargo.data.user

import com.juanpablo0612.tucargo.domain.model.UserRole
import com.juanpablo0612.tucargo.domain.model.UserStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class UserMapperTest {

    @Test
    fun userDto_toDomain_mapsCorrectly() {
        val dto = UserDto(
            id = "uid",
            role = "DRIVER",
            status = "ACTIVE"
        )

        val domain = dto.toDomain()

        assertEquals("uid", domain.id)
        assertEquals(UserRole.DRIVER, domain.role)
        assertEquals(UserStatus.ACTIVE, domain.status)
    }

    @Test
    fun userDto_toDomain_fallbackOnUnknownRole() {
        val dto = UserDto(role = "SUPER_ADMIN")
        val domain = dto.toDomain()
        assertEquals(UserRole.CLIENT, domain.role)
    }
}
