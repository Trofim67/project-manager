@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.gradetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gradetracker.data.Discipline
import com.example.gradetracker.data.Grade
import com.example.gradetracker.domain.GradeCalculator
import com.example.gradetracker.domain.GradeRules
import com.example.gradetracker.domain.formatAverage

@Composable
fun HomeScreen(
    data: AppData,
    onOpenDiscipline: (Long) -> Unit,
    onAddDiscipline: () -> Unit,
    onEditDiscipline: (Long) -> Unit,
    onDeleteDiscipline: (Discipline) -> Unit,
    onDynamics: () -> Unit,
    onCalculator: () -> Unit,
    onLoadSample: () -> Unit,
    onClearAll: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Discipline?>(null) }
    var confirmClear by remember { mutableStateOf(false) }
    val byDiscipline = remember(data.grades) { data.grades.groupBy { it.disciplineId } }
    val overall = GradeCalculator.average(data.grades.map { it.value })

    Scaffold(
        topBar = {
            AppTopBar("Успеваемость") {
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Загрузить тестовые данные") },
                            onClick = { menuOpen = false; onLoadSample() }
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить все данные") },
                            onClick = { menuOpen = false; confirmClear = true }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDiscipline) {
                Icon(Icons.Default.Add, contentDescription = "Добавить дисциплину")
            }
        }
    ) { padding ->
        if (!data.loaded) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { SummaryCard(overall, data.grades.size, data.disciplines.size) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onDynamics, modifier = Modifier.weight(1f)) { Text("Динамика") }
                        Button(onClick = onCalculator, modifier = Modifier.weight(1f)) { Text("Целевой балл") }
                    }
                }
                if (data.disciplines.isEmpty()) {
                    item {
                        EmptyState(
                            "Дисциплин пока нет",
                            "Добавьте первую дисциплину кнопкой «+» или загрузите тестовые данные."
                        ) {
                            Button(onClick = onLoadSample) { Text("Загрузить тестовые данные") }
                        }
                    }
                } else {
                    items(data.disciplines, key = { it.id }) { d ->
                        DisciplineCard(
                            d = d,
                            grades = byDiscipline[d.id].orEmpty(),
                            onOpen = { onOpenDiscipline(d.id) },
                            onEdit = { onEditDiscipline(d.id) },
                            onDelete = { toDelete = d }
                        )
                    }
                }
            }
        }
    }

    toDelete?.let { d ->
        val count = byDiscipline[d.id]?.size ?: 0
        ConfirmDialog(
            title = "Удалить дисциплину?",
            text = if (count > 0)
                "Вместе с дисциплиной «${d.name}» будут удалены все её оценки ($count). Действие нельзя отменить."
            else "Дисциплина «${d.name}» будет удалена.",
            confirmText = "Удалить",
            onConfirm = { onDeleteDiscipline(d); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
    if (confirmClear) {
        ConfirmDialog(
            title = "Удалить все данные?",
            text = "Будут удалены все дисциплины и оценки. Действие нельзя отменить.",
            confirmText = "Удалить всё",
            onConfirm = { onClearAll(); confirmClear = false },
            onDismiss = { confirmClear = false }
        )
    }
}

@Composable
private fun SummaryCard(overall: Double?, gradesCount: Int, disciplinesCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Общий средний балл", style = MaterialTheme.typography.titleMedium)
            Text(formatAverage(overall), style = MaterialTheme.typography.displayMedium)
            Text(
                if (overall == null) "Оценок пока нет — средний балл не рассчитывается"
                else "Оценок: $gradesCount • Дисциплин: $disciplinesCount",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Среднее арифметическое по всем записям, все оценки имеют одинаковый вес",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun DisciplineCard(
    d: Discipline,
    grades: List<Grade>,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val avg = GradeCalculator.average(grades.map { it.value })
    val low = GradeCalculator.isLow(avg)
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (low) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(d.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                val teacher = d.teacherName
                if (!teacher.isNullOrBlank()) {
                    Text(teacher, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    if (grades.isEmpty()) "Оценок пока нет" else "Оценок: ${grades.size}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (low) {
                    Text(
                        "Требует внимания: средний балл ниже ${formatAverage(GradeRules.LOW_AVERAGE_THRESHOLD)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatAverage(avg),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Изменить дисциплину") }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Удалить дисциплину") }
                }
            }
        }
    }
}
