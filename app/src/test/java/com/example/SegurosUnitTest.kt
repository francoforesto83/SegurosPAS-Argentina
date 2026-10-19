package com.example

import com.example.data.model.Cliente
import com.example.data.model.Poliza
import com.example.utils.ArgentinaFormats
import com.example.utils.DateUtils
import com.example.utils.ExportUtils
import org.junit.Assert.*
import org.junit.Test

class SegurosUnitTest {

    @Test
    fun testDateFormattingAndDaysCalculation() {
        val todayIso = DateUtils.todayIso()
        val daysUntilToday = DateUtils.daysUntil(todayIso)
        assertEquals(0L, daysUntilToday)

        val pastDate = "2020-01-01"
        val daysPast = DateUtils.daysUntil(pastDate)
        assertNotNull(daysPast)
        assertTrue(daysPast!! < 0)

        val futureOneYear = DateUtils.addOneYear("2026-10-17")
        assertEquals("2027-10-17", futureOneYear)
    }

    @Test
    fun testArgentinaFormats() {
        // Document formatting
        val formattedDni = ArgentinaFormats.formatDocumento("32456789", "DNI")
        assertEquals("32.456.789", formattedDni)

        val formattedCuit = ArgentinaFormats.formatDocumento("30714589238", "CUIT")
        assertEquals("30-71458923-8", formattedCuit)

        // License plate (patente)
        val mercosur = ArgentinaFormats.formatPatente("af382kr")
        assertEquals("AF 382 KR", mercosur)

        val traditional = ArgentinaFormats.formatPatente("otx492")
        assertEquals("OTX 492", traditional)

        // WhatsApp number sanitizer
        val waClean = ArgentinaFormats.sanitizeWhatsAppNumber("+54 9 11 4455-6677")
        assertEquals("5491144556677", waClean)
    }

    @Test
    fun testExportDataGeneration() {
        val client = Cliente(
            id = 1,
            nombre = "Martín Rossi",
            documentoNumero = "32.456.789",
            localidad = "Palermo",
            provincia = "CABA"
        )
        val (headers, rows) = ExportUtils.buildClientesCsvData(listOf(client))
        assertTrue(headers.contains("Nombre / Razón Social"))
        assertEquals(1, rows.size)
        assertEquals("Martín Rossi", rows[0][2])
    }
}
