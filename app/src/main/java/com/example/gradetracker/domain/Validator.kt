package com.example.gradetracker.domain

import java.time.LocalDate

/** Проверка ввода. Каждая функция возвращает текст ошибки или null, если всё корректно. */
object Validator {
    fun gradeValue(text: String): String? {
        val v = text.trim().toIntOrNull()
            ?: return "Введите целое число от ${GradeRules.MIN_GRADE} до ${GradeRules.MAX_GRADE}"
        return if (v in GradeRules.MIN_GRADE..GradeRules.MAX_GRADE) null
        else "Оценка должна быть от ${GradeRules.MIN_GRADE} до ${GradeRules.MAX_GRADE}"
    }

    fun date(epochDay: Long, today: LocalDate = LocalDate.now()): String? =
        if (epochDay > today.toEpochDay()) "Дата не может быть в будущем" else null

    fun comment(text: String): String? =
        if (text.length > GradeRules.MAX_COMMENT_LENGTH)
            "Комментарий не длиннее ${GradeRules.MAX_COMMENT_LENGTH} символов" else null

    fun disciplineName(name: String, otherNames: List<String>): String? {
        val n = name.trim()
        return when {
            n.isEmpty() -> "Введите название дисциплины"
            n.length > GradeRules.MAX_NAME_LENGTH -> "Название не длиннее ${GradeRules.MAX_NAME_LENGTH} символов"
            otherNames.any { it.trim().equals(n, ignoreCase = true) } -> "Дисциплина с таким названием уже есть"
            else -> null
        }
    }

    fun teacherName(text: String): String? =
        if (text.length > GradeRules.MAX_NAME_LENGTH)
            "Не длиннее ${GradeRules.MAX_NAME_LENGTH} символов" else null

    /** Разбор десятичного числа; принимает и запятую, и точку. */
    fun parseDecimal(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()
}
