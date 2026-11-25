@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.gradetracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gradetracker.data.Discipline
import com.example.gradetracker.data.Grade
import com.example.gradetracker.domain.GradeCalculator
import com.example.gradetracker.domain.GradeFilter
import com.example.gradetracker.domain.GradeRules
import com.example.gradetracker.domain.SortOrder
import com.example.gradetracker.domain.formatAverage
import com.example.gradetracker.domain.formatDate
import com.example.gradetracker.domain.formatMonth
import java.time.YearMonth

@Composable
fun DisciplineScreen(
    disciplineId: Long,
    data: AppData,
    onBack: () -> Unit,
    onAddGrade: () -> Unit,
    onEditGrade: (Long) -> Unit,
    onDeleteGrade: (Grade) -> Unit
) {
    val discipline = data.disciplines.firstOrNull { it.id == disciplineId }
    if (discipline == null) {
        Scaffold(topBar = { AppTopBar("Дисциплина", onBack) }) { padding ->
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                if (!data.loaded) CircularProgressIndicator() else Text("Дисциплина не найдена")
            }
        }
    } else {
        DisciplineContent(
            discipline = discipline,
            all = data.grades.filter { it.disciplineId == disciplineId },
            onBack = onBack,
            onAddGrade = onAddGrade,
            onEditGrade = onEditGrade,
            onDeleteGrade = onDeleteGrade
        )
    }
}

@Composable
private fun DisciplineContent(
    discipline: Discipline,
    all: List<Grade>,
    onBack: () -> Unit,
    onAddGrade: () -> Unit,
    onEditGrade: (Long) -> Unit,
    onDeleteGrade: (Grade) -> Unit
) {
    var workType by rememberSaveable { mutableStateOf<String?>(null) }
    var monthKey by rememberSaveable { mutableStateOf<String?>(null) }
    var sort by rememberSaveable { mutableStateOf(SortOrder.DATE_DESC) }
    var toDelete by remember { mutableStateOf<Grade?>(null) }

    val months = all.map { GradeFilter.monthOf(it) }.distinct().sortedDescending()
    val month = monthKey?.let { key -> runCatching { YearMonth.parse(key) }.getOrNull() }
    val filtered = GradeFilter.apply(all, workType, month, sort)
    val filtersActive = workType != null || month != null

    val avgAll = GradeCalculator.average(all.map { it.value })
    val avgFiltered = GradeCalculator.average(filtered.map { it.value })

    Scaffold(
        topBar = { AppTopBar(discipline.name, onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGrade) {
                Icon(Icons.Default.Add, contentDescription = "Добавить оценку")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (GradeCalculator.isLow(avgAll)) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Средний балл по дисциплине", style = MaterialTheme.typography.titleSmall)
                        Text(formatAverage(avgAll), style = MaterialTheme.typography.displaySmall)
                        val teacher = discipline.teacherName
                        if (!teacher.isNullOrBlank()) Text("Преподаватель: $teacher", style = MaterialTheme.typography.bodySmall)
                        if (all.isEmpty()) {
                            Text("Оценок пока нет", style = MaterialTheme.typography.bodySmall)
                        } else if (filtersActive) {
                            Text(
                                "По выбранным фильтрам: ${formatAverage(avgFiltered)} (оценок ${filtered.size} из ${all.size})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text("Оценок: ${all.size}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            if (all.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DropdownField(
                                label = "Вид",
                                options = listOf("Все") + GradeRules.WORK_TYPES,
                                selected = workType ?: "Все",
                                onSelect = { i -> workType = if (i == 0) null else GradeRules.WORK_TYPES[i - 1] },
                                modifier = Modifier.weight(1f)
                            )
                            DropdownField(
                                label = "Период",
                                options = listOf("Все") + months.map { formatMonth(it) },
                                selected = month?.let { formatMonth(it) } ?: "Все",
                                onSelect = { i -> monthKey = if (i == 0) null else months[i - 1].toString() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            DropdownField(
                                label = "Сортировка",
                                options = SortOrder.values().map { it.label },
                                selected = sort.label,
                                onSelect = { i -> sort = SortOrder.values()[i] },
                                modifier = Modifier.weight(1f)
                            )
                            if (filtersActive) {
                                TextButton(onClick = { workType = null; monthKey = null }) { Text("Сбросить") }
                            }
                        }
                    }
                }
            }
            if (all.isEmpty()) {
                item {
                    EmptyState(
                        "Оценок по этой дисциплине пока нет",
                        "Нажмите «+», чтобы добавить первую оценку."
                    )
                }
            } else if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        "Ничего не найдено",
                        "По выбранным фильтрам оценок нет. Измените или сбросьте фильтры."
                    )
                }
            } else {
                items(filtered, key = { it.id }) { g ->
                    GradeRow(g, onEdit = { onEditGrade(g.id) }, onDelete = { toDelete = g })
                }
            }
        }
    }

    toDelete?.let { g ->
        ConfirmDialog(
            title = "Удалить оценку?",
            text = "Оценка ${g.value} («${g.workType}», ${formatDate(g.date)}) будет удалена.",
            confirmText = "Удалить",
            onConfirm = { onDeleteGrade(g); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}

private fun gradeColor(value: Int): Color = when (value) {
    5 -> Color(0xFF2E7D32)
    4 -> Color(0xFF7CB342)
    3 -> Color(0xFFF9A825)
    else -> Color(0xFFC62828)
}

@Composable
private fun GradeRow(g: Grade, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(gradeColor(g.value)),
                contentAlignment = Alignment.Center
            ) {
                Text(g.value.toString(), style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(g.workType, style = MaterialTheme.typography.titleMedium)
                Text(formatDate(g.date), style = MaterialTheme.typography.bodySmall)
                if (g.comment.isNotBlank()) {
                    Text(g.comment, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Изменить оценку") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Удалить оценку") }
        }
    }
}
