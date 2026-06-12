package com.juanpablo0612.tucargo.data.trip

import com.juanpablo0612.tucargo.domain.model.TripStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class TripMapperTest {

    @Test
    fun tripDto_toDomain_mapsCorrectly() {
        val dto = TripDto(
            id = "123",
            status = "ASSIGNED",
            priceTotal = 50000.0
        )

        val domain = dto.toDomain()

        assertEquals("123", domain.id)
        assertEquals(TripStatus.ASSIGNED, domain.status)
        assertEquals(50000.0, domain.priceTotal)
    }

    @Test
    fun tripDto_toDomain_fallbackOnUnknownStatus() {
        val dto = TripDto(status = "UNKNOWN_STATUS")
        val domain = dto.toDomain()
        assertEquals(TripStatus.SEARCHING, domain.status)
    }

    @Test
    fun trip_toDto_mapsCorrectly() {
        val domain = com.juanpablo0612.tucargo.domain.model.Trip(
            id = "123",
            status = TripStatus.COMPLETED
        )

        val dto = domain.toDto()

        assertEquals("123", dto.id)
        assertEquals("COMPLETED", dto.status)
    }

    @Test
    fun tripDto_toDomain_mapsClientAndDriverLocationFields() {
        val dto = TripDto(
            clientName = "Ana",
            clientPhone = "+573001234567",
            driverLastLat = 4.61,
            driverLastLng = -74.08
        )

        val domain = dto.toDomain()

        assertEquals("Ana", domain.clientName)
        assertEquals("+573001234567", domain.clientPhone)
        assertEquals(4.61, domain.driverLastLat)
        assertEquals(-74.08, domain.driverLastLng)
    }
}
