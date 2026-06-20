package com.example.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateUtils {
    private val formatters = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        DateTimeFormatter.ofPattern("d.M.yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy")
    )

    fun parseDate(dateStr: String): LocalDate? {
        val trimmed = dateStr.trim()
        for (formatter in formatters) {
            try {
                return LocalDate.parse(trimmed, formatter)
            } catch (e: DateTimeParseException) {
                // Ignore and try next format
            }
        }
        return null
    }

    fun formatDateToIso(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    fun formatDateToDisplay(dateStr: String): String {
        val parsed = parseDate(dateStr) ?: return dateStr
        return parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}
