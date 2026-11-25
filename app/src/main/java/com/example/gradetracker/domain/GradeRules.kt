package com.example.gradetracker.domain

/** Правила оценивания (согласованы в Спринте 1). */
object GradeRules {
    const val MIN_GRADE = 2
    const val MAX_GRADE = 5

    /** Дисциплины со средним ниже порога подсвечиваются. */
    const val LOW_AVERAGE_THRESHOLD = 3.5

    const val MAX_NAME_LENGTH = 60
    const val MAX_COMMENT_LENGTH = 200

    val WORK_TYPES = listOf("Контрольная", "Практическая", "Лабораторная", "Ответ", "Экзамен", "Другое")
}
