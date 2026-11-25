@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.gradetracker.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gradetracker.data.Discipline
import com.example.gradetracker.data.Grade
import com.example.gradetracker.domain.GradeRules
import com.example.gradetracker.domain.Validator
import com.example.gradetracker.domain.formatDate
import java.time.LocalDate

@Composable
private fun Placeholder(title: String, onBack: () -> Unit, loading: Boolean, message: String) {
    Scaffold(topBar = { AppTopBar(title, onBack) }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            if (loading) CircularProgressIndicator() else Text(message)
        }
    }
}

// ───────────────────────── Форма оценки ─────────────────────────

@Composable
fun GradeFormScreen(
    disciplineId: Long,
    gradeId: Long,
    data: AppData,
    onSave: (Grade) -> Unit,
    onBack: () -> Unit
) {
    val discipline = data.disciplines.firstOrNull { it.id == disciplineId }
    val existing = if (gradeId == 0L) null else data.grades.firstOrNull { it.id == gradeId }
    when {
        !data.loaded -> Placeholder("Оценка", onBack, true, "")
        discipline == null -> Placeholder("Оценка", onBack, false, "Дисциплина не найдена")
        gradeId != 0L && existing == null -> Placeholder("Оценка", onBack, false, "Оценка не найдена")
        else -> GradeForm(discipline, existing, onSave, onBack)
    }
}

@Composable
private fun GradeForm(discipline: Discipline, existing: Grade?, onSave: (Grade) -> Unit, onBack: () -> Unit) {
    var valueText by rememberSaveable { mutableStateOf(existing?.value?.toString() ?: "") }
    var dateEpoch by rememberSaveable { mutableStateOf(existing?.date ?: LocalDate.now().toEpochDay()) }
    var workType by rememberSaveable { mutableStateOf(existing?.workType ?: GradeRules.WORK_TYPES.first()) }
    var comment by rememberSaveable { mutableStateOf(existing?.comment ?: "") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val valueError = Validator.gradeValue(valueText)
    val dateError = Validator.date(dateEpoch)
    val commentError = Validator.comment(comment)
    val valid = valueError == null && dateError == null && commentError == null

    fun showDatePicker() {
        val d = LocalDate.ofEpochDay(dateEpoch)
        DatePickerDialog(
            context,
            { _, y, m, day -> dateEpoch = LocalDate.of(y, m + 1, day).toEpochDay() },
            d.year, d.monthValue - 1, d.dayOfMonth
        ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
    }

    Scaffold(
        topBar = { AppTopBar(if (existing == null) "Новая оценка" else "Изменение оценки", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Дисциплина: ${discipline.name}", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = valueText,
                onValueChange = { valueText = it.take(3) },
                label = { Text("Оценка (${GradeRules.MIN_GRADE}–${GradeRules.MAX_GRADE})") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = (submitted || valueText.isNotEmpty()) && valueError != null,
                supportingText = {
                    if ((submitted || valueText.isNotEmpty()) && valueError != null) Text(valueError)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                OutlinedButton(onClick = { showDatePicker() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Дата: ${formatDate(dateEpoch)}")
                }
                if (dateError != null) {
                    Text(dateError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            DropdownField(
                label = "Тип работы",
                options = GradeRules.WORK_TYPES,
                selected = workType,
                onSelect = { workType = GradeRules.WORK_TYPES[it] },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Комментарий (необязательно)") },
                isError = commentError != null,
                supportingText = { Text(commentError ?: "${comment.length}/${GradeRules.MAX_COMMENT_LENGTH}") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Отмена") }
                Button(
                    onClick = {
                        submitted = true
                        if (valid) {
                            onSave(
                                Grade(
                                    id = existing?.id ?: 0,
                                    disciplineId = discipline.id,
                                    value = valueText.trim().toInt(),
                                    date = dateEpoch,
                                    workType = workType,
                                    comment = comment.trim()
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Сохранить") }
            }
        }
    }
}

// ───────────────────────── Форма дисциплины ─────────────────────────

@Composable
fun DisciplineFormScreen(
    disciplineId: Long,
    data: AppData,
    onSave: (Discipline) -> Unit,
    onBack: () -> Unit
) {
    val existing = if (disciplineId == 0L) null else data.disciplines.firstOrNull { it.id == disciplineId }
    when {
        !data.loaded -> Placeholder("Дисциплина", onBack, true, "")
        disciplineId != 0L && existing == null -> Placeholder("Дисциплина", onBack, false, "Дисциплина не найдена")
        else -> DisciplineForm(
            existing = existing,
            otherNames = data.disciplines.filter { it.id != disciplineId }.map { it.name },
            onSave = onSave,
            onBack = onBack
        )
    }
}

@Composable
private fun DisciplineForm(
    existing: Discipline?,
    otherNames: List<String>,
    onSave: (Discipline) -> Unit,
    onBack: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(existing?.name ?: "") }
    var teacher by rememberSaveable { mutableStateOf(existing?.teacherName ?: "") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val nameError = Validator.disciplineName(name, otherNames)
    val teacherError = Validator.teacherName(teacher)

    Scaffold(
        topBar = { AppTopBar(if (existing == null) "Новая дисциплина" else "Изменение дисциплины", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название дисциплины") },
                singleLine = true,
                isError = (submitted || name.isNotEmpty()) && nameError != null,
                supportingText = {
                    if ((submitted || name.isNotEmpty()) && nameError != null) Text(nameError)
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = teacher,
                onValueChange = { teacher = it },
                label = { Text("Преподаватель (необязательно)") },
                singleLine = true,
                isError = teacherError != null,
                supportingText = { if (teacherError != null) Text(teacherError) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Отмена") }
                Button(
                    onClick = {
                        submitted = true
                        if (nameError == null && teacherError == null) {
                            onSave(
                                Discipline(
                                    id = existing?.id ?: 0,
                                    name = name.trim(),
                                    teacherName = teacher.trim().ifEmpty { null }
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Сохранить") }
            }
        }
    }
}
