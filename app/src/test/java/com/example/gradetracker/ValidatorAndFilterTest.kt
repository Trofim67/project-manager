package com.example.gradetracker

import com.example.gradetracker.data.Grade
import com.example.gradetracker.domain.GradeFilter
import com.example.gradetracker.domain.SortOrder
import com.example.gradetracker.domain.Validator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class ValidatorTest {
    @Test fun gradeValue_validRange() {
        listOf("2", "3", "4", "5", " 4 ").forEach { assertNull(it, Validator.gradeValue(it)) }
    }

    @Test fun gradeValue_invalid() {
        listOf("", "1", "6", "0", "-3", "abc", "4.5", "10").forEach { assertNotNull(it, Validator.gradeValue(it)) }
    }

    @Test fun date_futureRejected_todayAccepted() {
        val today = LocalDate.of(2026, 11, 24)
        assertNull(Validator.date(today.toEpochDay(), today))
        assertNotNull(Validator.date(today.plusDays(1).toEpochDay(), today))
    }

    @Test fun disciplineName_rules() {
        assertNotNull(Validator.disciplineName("   ", emptyList()))
        assertNotNull(Validator.disciplineName("a".repeat(61), emptyList()))
        assertNotNull(Validator.disciplineName("математика", listOf("Математика")))
        assertNull(Validator.disciplineName("Физика", listOf("Математика")))
    }

    @Test fun parseDecimal_acceptsCommaAndDot() {
        assertEquals(4.5, Validator.parseDecimal("4,5")!!, 1e-9)
        assertEquals(4.5, Validator.parseDecimal("4.5")!!, 1e-9)
        assertNull(Validator.parseDecimal("abc"))
    }
}

class GradeFilterTest {
    private fun g(id: Long, day: String, v: Int, type: String) =
        Grade(id = id, disciplineId = 1, value = v, date = LocalDate.parse(day).toEpochDay(), workType = type)

    private val grades = listOf(
        g(1, "2026-09-10", 5, "Контрольная"),
        g(2, "2026-10-05", 3, "Лабораторная"),
        g(3, "2026-10-20", 4, "Контрольная")
    )

    @Test fun filterByType() =
        assertEquals(listOf(3L, 1L), GradeFilter.apply(grades, "Контрольная", null, SortOrder.DATE_DESC).map { it.id })

    @Test fun filterByMonth() =
        assertEquals(listOf(2L, 3L), GradeFilter.apply(grades, null, YearMonth.of(2026, 10), SortOrder.DATE_ASC).map { it.id })

    @Test fun filterByTypeAndMonth() =
        assertEquals(listOf(3L), GradeFilter.apply(grades, "Контрольная", YearMonth.of(2026, 10), SortOrder.DATE_DESC).map { it.id })

    @Test fun sortByValue() {
        assertEquals(listOf(1L, 3L, 2L), GradeFilter.apply(grades, null, null, SortOrder.VALUE_DESC).map { it.id })
        assertEquals(listOf(2L, 3L, 1L), GradeFilter.apply(grades, null, null, SortOrder.VALUE_ASC).map { it.id })
    }

    @Test fun emptyResult() =
        assertEquals(emptyList<Grade>(), GradeFilter.apply(grades, "Экзамен", null, SortOrder.DATE_DESC))
}
