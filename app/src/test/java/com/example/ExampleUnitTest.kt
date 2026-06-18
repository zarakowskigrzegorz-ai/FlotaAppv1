package com.example

import com.example.data.model.InspectionStatus
import com.example.util.DateUtils
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {
    
    @Test
    fun testDateParsing() {
        val dateIso = DateUtils.parseDate("2026-06-18")
        assertNotNull(dateIso)
        assertEquals(2026, dateIso?.year)
        assertEquals(6, dateIso?.monthValue)
        assertEquals(18, dateIso?.dayOfMonth)

        val datePolish = DateUtils.parseDate("28.06.2026")
        assertNotNull(datePolish)
        assertEquals(2026, datePolish?.year)
        assertEquals(6, datePolish?.monthValue)
        assertEquals(28, datePolish?.dayOfMonth)

        val invalidDate = DateUtils.parseDate("niepoprawny-plik")
        assertNull(invalidDate)
    }

    @Test
    fun testInspectionStatusCalculation() {
        val refDate = LocalDate.of(2026, 6, 18)

        // Case 1: Inspection date is before today -> OVERDUE
        val statusOverdue = InspectionStatus.calculate("2026-05-10", refDate)
        assertEquals(InspectionStatus.OVERDUE, statusOverdue)

        // Case 2: Inspection date is in 7 days -> ENDING_SOON (within 30 days)
        val statusSoon = InspectionStatus.calculate("2026-06-25", refDate)
        assertEquals(InspectionStatus.ENDING_SOON, statusSoon)

        // Case 3: Inspection date is in September (far future) -> OK
        val statusOk = InspectionStatus.calculate("2026-09-12", refDate)
        assertEquals(InspectionStatus.OK, statusOk)
    }
}
