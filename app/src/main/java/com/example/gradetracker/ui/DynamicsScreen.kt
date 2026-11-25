@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.gradetracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradetracker.domain.GradeCalculator
import com.example.gradetracker.domain.GradeRules
import com.example.gradetracker.domain.PeriodStat
import com.example.gradetracker.domain.formatAverage
import com.example.gradetracker.domain.formatDelta
import com.example.gradetracker.domain.formatMonth

@Composable
fun DynamicsScreen(data: AppData, onBack: () -> Unit) {
    var selectedId by rememberSaveable { mutableStateOf(0L) } // 0 = все дисциплины
    val selected = data.disciplines.firstOrNull { it.id == selectedId }
    val grades = if (selected == null) data.grades else data.grades.filter { it.disciplineId == selected.id }
    val stats = GradeCalculator.monthlyDynamics(grades)

    Scaffold(topBar = { AppTopBar("Динамика успеваемости", onBack) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DropdownField(
                label = "Дисциплина",
                options = listOf("Все дисциплины") + data.disciplines.map { it.name },
                selected = selected?.name ?: "Все дисциплины",
                onSelect = { i -> selectedId = if (i == 0) 0L else data.disciplines[i - 1].id },
                modifier = Modifier.fillMaxWidth()
            )
            if (stats.isEmpty()) {
                EmptyState(
                    "Пока нечего показывать",
                    "Динамика строится по месяцам. Добавьте оценки, и здесь появятся график и таблица."
                )
            } else {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Средний балл по месяцам", style = MaterialTheme.typography.titleSmall)
                        DynamicsChart(stats)
                        Text(
                            "● средний за месяц     ● накопленный средний",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        TableRow("Период", "Оценок", "За месяц", "Накоплен.", "Изменение", header = true)
                        HorizontalDivider()
                        stats.forEach { s ->
                            TableRow(
                                formatMonth(s.month),
                                s.count.toString(),
                                formatAverage(s.average),
                                formatAverage(s.cumulativeAverage),
                                formatDelta(s.delta)
                            )
                        }
                    }
                }
                Text(
                    "Среднее арифметическое оценок за каждый месяц; «Накоплен.» — среднее по всем оценкам с начала учёта.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TableRow(a: String, b: String, c: String, d: String, e: String, header: Boolean = false) {
    val weight = if (header) FontWeight.Bold else FontWeight.Normal
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(a, Modifier.weight(2.2f), fontWeight = weight, style = MaterialTheme.typography.bodySmall)
        Text(b, Modifier.weight(1f), fontWeight = weight, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
        Text(c, Modifier.weight(1.3f), fontWeight = weight, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
        Text(d, Modifier.weight(1.3f), fontWeight = weight, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
        Text(e, Modifier.weight(1.3f), fontWeight = weight, textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DynamicsChart(stats: List<PeriodStat>) {
    val monthColor = MaterialTheme.colorScheme.primary
    val cumulativeColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val minG = GradeRules.MIN_GRADE.toFloat()
    val maxG = GradeRules.MAX_GRADE.toFloat()

    Canvas(Modifier.fillMaxWidth().height(200.dp).padding(top = 8.dp)) {
        val left = 28.dp.toPx()
        val right = size.width - 12.dp.toPx()
        val top = 10.dp.toPx()
        val bottom = size.height - 10.dp.toPx()
        val paint = android.graphics.Paint().apply {
            color = labelColor
            textSize = 12.sp.toPx()
            isAntiAlias = true
        }

        fun yOf(v: Float): Float = bottom - (v - minG) / (maxG - minG) * (bottom - top)
        fun xOf(i: Int): Float =
            if (stats.size == 1) (left + right) / 2f else left + (right - left) * i / (stats.size - 1)

        for (g in GradeRules.MIN_GRADE..GradeRules.MAX_GRADE) {
            val y = yOf(g.toFloat())
            drawLine(gridColor, Offset(left, y), Offset(right, y), 1.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText(g.toString(), 8.dp.toPx(), y + 4.dp.toPx(), paint)
        }

        fun series(values: List<Float>, color: Color) {
            val pts = values.mapIndexed { i, v -> Offset(xOf(i), yOf(v)) }
            for (i in 0 until pts.size - 1) drawLine(color, pts[i], pts[i + 1], 3.dp.toPx())
            pts.forEach { drawCircle(color, 5.dp.toPx(), it) }
        }
        series(stats.map { it.cumulativeAverage.toFloat() }, cumulativeColor)
        series(stats.map { it.average.toFloat() }, monthColor)
    }
}
