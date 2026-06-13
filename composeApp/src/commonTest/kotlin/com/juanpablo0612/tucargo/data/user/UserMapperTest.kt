package com.juanpablo0612.tucargo.data.user

import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.UserRole
import com.juanpablo0612.tucargo.domain.model.UserStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
    fun userDto_toDomain_mapsAdminRole() {
        val dto = UserDto(role = "ADMIN")
        assertEquals(UserRole.ADMIN, dto.toDomain().role)
    }

    @Test
    fun userDto_toDomain_throwsOnUnknownRole() {
        val dto = UserDto(role = "SUPER_ADMIN")
        assertFailsWith<AppError.DataCorruption> { dto.toDomain() }
    }

    @Test
    fun userDto_toDomain_fallbackOnUnknownStatus() {
        val dto = UserDto(role = "CLIENT", status = "FROZEN")
        assertEquals(UserStatus.ACTIVE, dto.toDomain().status)
    }
}
