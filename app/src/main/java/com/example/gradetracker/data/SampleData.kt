package com.example.gradetracker.data

import java.time.LocalDate

/**
 * Тестовые данные. Контрольные значения (для ручной проверки расчётов):
 *  Математика 5,4,3,5 -> 4,25 | Физика 3,3,4,3 -> 3,25 | Информатика 5,5,4,5 -> 4,75
 *  История 4,4 -> 4,00 | Литература — без оценок.
 *  Всего 14 оценок, сумма 57, общий средний = 4,07.
 */
object SampleData {
    data class SampleGrade(
        val monthsAgo: Long,
        val day: Int,
        val value: Int,
        val type: String,
        val comment: String = ""
    )

    data class SampleDiscipline(val name: String, val teacher: String?, val grades: List<SampleGrade>)

    fun dateOf(g: SampleGrade, today: LocalDate = LocalDate.now()): Long {
        val base = today.minusMonths(g.monthsAgo)
        // В текущем месяце берём 1-е число, чтобы дата не оказалась в будущем.
        val day = if (g.monthsAgo == 0L) 1 else g.day.coerceAtMost(base.lengthOfMonth())
        return base.withDayOfMonth(day).toEpochDay()
    }

    val disciplines = listOf(
        SampleDiscipline(
            "Математика", "Иванова А. П.", listOf(
                SampleGrade(3, 10, 5, "Контрольная"),
                SampleGrade(2, 5, 4, "Практическая", "Небольшая ошибка в вычислениях"),
                SampleGrade(1, 15, 3, "Контрольная"),
                SampleGrade(0, 1, 5, "Ответ", "Устный ответ у доски")
            )
        ),
        SampleDiscipline(
            "Физика", "Петров С. В.", listOf(
                SampleGrade(3, 12, 3, "Лабораторная"),
                SampleGrade(2, 20, 3, "Контрольная"),
                SampleGrade(1, 8, 4, "Лабораторная"),
                SampleGrade(0, 1, 3, "Практическая")
            )
        ),
        SampleDiscipline(
            "Информатика", "Сидорова Е. Н.", listOf(
                SampleGrade(3, 7, 5, "Практическая"),
                SampleGrade(2, 14, 5, "Лабораторная"),
                SampleGrade(1, 22, 4, "Контрольная"),
                SampleGrade(0, 1, 5, "Ответ")
            )
        ),
        SampleDiscipline(
            "История", null, listOf(
                SampleGrade(2, 3, 4, "Ответ"),
                SampleGrade(1, 18, 4, "Контрольная")
            )
        ),
        SampleDiscipline("Литература", "Кузнецова Т. И.", emptyList())
    )
}
