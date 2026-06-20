package com.example.data.model

import com.example.util.DateUtils
import java.time.LocalDate

enum class InspectionStatus(val label: String) {
    OVERDUE("PO TERMINIE"),
    ENDING_SOON("KOŃCZY SIĘ"),
    OK("STATUS OK");

    companion object {
        fun calculate(dateStr: String, referenceDate: LocalDate): InspectionStatus {
            val parsedDate = DateUtils.parseDate(dateStr) ?: return OK
            return when {
                parsedDate.isBefore(referenceDate) -> OVERDUE
                parsedDate.isBefore(referenceDate.plusDays(30)) || parsedDate.isEqual(referenceDate.plusDays(30)) -> ENDING_SOON
                else -> OK
            }
        }
    }
}
