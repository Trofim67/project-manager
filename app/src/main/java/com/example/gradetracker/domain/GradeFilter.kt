package com.example.gradetracker.domain

import com.example.gradetracker.data.Grade
import java.time.LocalDate
import java.time.YearMonth

enum class SortOrder(val label: String) {
    DATE_DESC("Сначала новые"),
    DATE_ASC("Сначала старые"),
    VALUE_DESC("Оценка: по убыванию"),
    VALUE_ASC("Оценка: по возрастанию")
}

object GradeFilter {
    fun monthOf(g: Grade): YearMonth = YearMonth.from(LocalDate.ofEpochDay(g.date))

    fun apply(grades: List<Grade>, workType: String?, month: YearMonth?, sort: SortOrder): List<Grade> {
        val filtered = grades.filter { g ->
            (workType == null || g.workType == workType) &&
                (month == null || monthOf(g) == month)
        }
        return when (sort) {
            SortOrder.DATE_DESC -> filtered.sortedWith(compareByDescending<Grade> { it.date }.thenByDescending { it.id })
            SortOrder.DATE_ASC -> filtered.sortedWith(compareBy<Grade> { it.date }.thenBy { it.id })
            SortOrder.VALUE_DESC -> filtered.sortedWith(compareByDescending<Grade> { it.value }.thenByDescending { it.date })
            SortOrder.VALUE_ASC -> filtered.sortedWith(compareBy<Grade> { it.value }.thenByDescending { it.date })
        }
    }
}
