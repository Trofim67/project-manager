package com.example.gradetracker.domain

import com.example.gradetracker.data.Grade
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.ceil

sealed interface TargetResult {
    /** Нужна оценка не ниже [grade]; [exact] — расчётное (дробное) значение по формуле. */
    data class Needed(val grade: Int, val exact: Double) : TargetResult

    /** Цель достигается при любой допустимой оценке. */
    data class AlreadyAchieved(val exact: Double) : TargetResult

    /** Даже за максимальную оценку цель не достигается. */
    data class Unreachable(val exact: Double, val bestPossibleAverage: Double) : TargetResult

    /** Целевой балл вне допустимого диапазона. */
    object InvalidTarget : TargetResult
}

data class PeriodStat(
    val month: YearMonth,
    val count: Int,
    val average: Double,
    val cumulativeAverage: Double,
    /** Изменение среднего за период относительно предыдущего периода (null у первого). */
    val delta: Double?
)

object GradeCalculator {
    private const val EPS = 1e-9

    /** Среднее арифметическое; для пустого списка — null (без деления на ноль). */
    fun average(values: List<Int>): Double? =
        if (values.isEmpty()) null else values.sum().toDouble() / values.size

    fun isLow(average: Double?): Boolean =
        average != null && average < GradeRules.LOW_AVERAGE_THRESHOLD

    /**
     * Требуемая следующая оценка = цель × (n + 1) − сумма.
     * Оценки целые, поэтому результат округляется вверх до ближайшей оценки.
     */
    fun requiredNextGrade(values: List<Int>, target: Double): TargetResult {
        if (target.isNaN() || target < GradeRules.MIN_GRADE || target > GradeRules.MAX_GRADE) {
            return TargetResult.InvalidTarget
        }
        val n = values.size
        val sum = values.sum()
        val exact = target * (n + 1) - sum
        val needed = ceil(exact - EPS).toInt()
        return when {
            needed <= GradeRules.MIN_GRADE -> TargetResult.AlreadyAchieved(exact)
            needed > GradeRules.MAX_GRADE -> TargetResult.Unreachable(
                exact, (sum + GradeRules.MAX_GRADE).toDouble() / (n + 1)
            )
            else -> TargetResult.Needed(needed, exact)
        }
    }

    /** Динамика по календарным месяцам: средний за месяц и накопленный средний. */
    fun monthlyDynamics(grades: List<Grade>): List<PeriodStat> {
        val byMonth = grades
            .groupBy { YearMonth.from(LocalDate.ofEpochDay(it.date)) }
            .toSortedMap()
        var runningSum = 0
        var runningCount = 0
        var prevAvg: Double? = null
        val result = mutableListOf<PeriodStat>()
        for ((month, list) in byMonth) {
            val values = list.map { it.value }
            val avg = values.sum().toDouble() / values.size
            runningSum += values.sum()
            runningCount += values.size
            result += PeriodStat(
                month = month,
                count = values.size,
                average = avg,
                cumulativeAverage = runningSum.toDouble() / runningCount,
                delta = prevAvg?.let { avg - it }
            )
            prevAvg = avg
        }
        return result
    }
}
