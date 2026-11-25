package com.example.gradetracker

import com.example.gradetracker.data.Grade
import com.example.gradetracker.domain.GradeCalculator
import com.example.gradetracker.domain.TargetResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class GradeCalculatorTest {
    private val math = listOf(5, 4, 3, 5) // среднее 4,25 — пример из задания

    @Test fun average_ofExample_is425() = assertEquals(4.25, GradeCalculator.average(math)!!, 1e-9)

    @Test fun average_ofEmpty_isNull_noDivisionByZero() = assertNull(GradeCalculator.average(emptyList()))

    @Test fun isLow_belowThresholdOnly() {
        assertTrue(GradeCalculator.isLow(3.49))
        assertTrue(!GradeCalculator.isLow(3.5))
        assertTrue(!GradeCalculator.isLow(null))
    }

    @Test fun target_44_needsFive() {
        val r = GradeCalculator.requiredNextGrade(math, 4.4)
        assertEquals(5, (r as TargetResult.Needed).grade)
    }

    @Test fun target_40_needsThree() {
        val r = GradeCalculator.requiredNextGrade(math, 4.0) as TargetResult.Needed
        assertEquals(3, r.grade)
        assertEquals(3.0, r.exact, 1e-9)
    }

    @Test fun target_43_roundsUpToFive() {
        // (17 + 4) / 5 = 4,2 < 4,3, а (17 + 5) / 5 = 4,4 >= 4,3
        assertEquals(5, (GradeCalculator.requiredNextGrade(math, 4.3) as TargetResult.Needed).grade)
    }

    @Test fun target_45_isUnreachable() {
        val r = GradeCalculator.requiredNextGrade(math, 4.5) as TargetResult.Unreachable
        assertEquals(4.4, r.bestPossibleAverage, 1e-9)
    }

    @Test fun target_30_isAlreadyAchieved() {
        assertTrue(GradeCalculator.requiredNextGrade(math, 3.0) is TargetResult.AlreadyAchieved)
    }

    @Test fun target_onEmptyList_equalsTargetRoundedUp() {
        assertEquals(4, (GradeCalculator.requiredNextGrade(emptyList(), 4.0) as TargetResult.Needed).grade)
        assertEquals(5, (GradeCalculator.requiredNextGrade(emptyList(), 4.1) as TargetResult.Needed).grade)
    }

    @Test fun target_outOfRange_isInvalid() {
        assertTrue(GradeCalculator.requiredNextGrade(math, 1.5) is TargetResult.InvalidTarget)
        assertTrue(GradeCalculator.requiredNextGrade(math, 5.5) is TargetResult.InvalidTarget)
        assertTrue(GradeCalculator.requiredNextGrade(math, Double.NaN) is TargetResult.InvalidTarget)
    }

    @Test fun dynamics_groupsByMonth_withCumulativeAndDelta() {
        fun g(day: String, v: Int) = Grade(disciplineId = 1, value = v, date = LocalDate.parse(day).toEpochDay(), workType = "Ответ")
        val stats = GradeCalculator.monthlyDynamics(
            listOf(g("2026-02-05", 5), g("2026-01-10", 5), g("2026-01-20", 3))
        )
        assertEquals(2, stats.size)
        assertEquals(YearMonth.of(2026, 1), stats[0].month)
        assertEquals(4.0, stats[0].average, 1e-9)
        assertEquals(null, stats[0].delta)
        assertEquals(5.0, stats[1].average, 1e-9)
        assertEquals(13.0 / 3, stats[1].cumulativeAverage, 1e-9)
        assertEquals(1.0, stats[1].delta!!, 1e-9)
    }

    @Test fun dynamics_emptyInput_isEmpty() = assertTrue(GradeCalculator.monthlyDynamics(emptyList()).isEmpty())
}
