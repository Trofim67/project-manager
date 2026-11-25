@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.gradetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gradetracker.domain.GradeCalculator
import com.example.gradetracker.domain.GradeRules
import com.example.gradetracker.domain.TargetResult
import com.example.gradetracker.domain.Validator
import com.example.gradetracker.domain.formatAverage

@Composable
fun CalculatorScreen(data: AppData, onBack: () -> Unit) {
    var selectedId by rememberSaveable { mutableStateOf(0L) } // 0 = общий балл
    var targetText by rememberSaveable { mutableStateOf("") }

    val selected = data.disciplines.firstOrNull { it.id == selectedId }
    val values = (if (selected == null) data.grades else data.grades.filter { it.disciplineId == selected.id })
        .map { it.value }
    val current = GradeCalculator.average(values)

    val target = Validator.parseDecimal(targetText)
    val result = target?.let { GradeCalculator.requiredNextGrade(values, it) }

    Scaffold(topBar = { AppTopBar("Целевой средний балл", onBack) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DropdownField(
                label = "Для",
                options = listOf("Общего балла") + data.disciplines.map { it.name },
                selected = selected?.name ?: "Общего балла",
                onSelect = { i -> selectedId = if (i == 0) 0L else data.disciplines[i - 1].id },
                modifier = Modifier.fillMaxWidth()
            )

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Сейчас", style = MaterialTheme.typography.titleSmall)
                    Text(formatAverage(current), style = MaterialTheme.typography.headlineMedium)
                    Text(
                        if (values.isEmpty()) "Оценок пока нет"
                        else "Оценок: ${values.size}, сумма: ${values.sum()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            OutlinedTextField(
                value = targetText,
                onValueChange = { targetText = it.take(5) },
                label = { Text("Желаемый средний балл (${GradeRules.MIN_GRADE}–${GradeRules.MAX_GRADE})") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = targetText.isNotBlank() && target == null,
                supportingText = {
                    if (targetText.isNotBlank() && target == null) Text("Введите число, например 4,5")
                },
                modifier = Modifier.fillMaxWidth()
            )

            when (result) {
                null -> if (targetText.isBlank()) {
                    Text(
                        "Введите желаемый средний балл — приложение рассчитает, какая оценка нужна за следующую работу.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is TargetResult.InvalidTarget -> ResultCard(
                    "Недопустимый целевой балл",
                    "Желаемый средний балл должен быть от ${GradeRules.MIN_GRADE} до ${GradeRules.MAX_GRADE}.",
                    bad = true
                )
                is TargetResult.Needed -> ResultCard(
                    "Нужна оценка: ${result.grade}",
                    "Расчёт: ${formatAverage(target)} × (${values.size} + 1) − ${values.sum()} = ${formatAverage(result.exact)}. " +
                        "Оценки целые, поэтому берём ближайшую не ниже расчётной.",
                    bad = false
                )
                is TargetResult.AlreadyAchieved -> ResultCard(
                    "Цель уже достигается",
                    "При любой допустимой оценке (даже ${GradeRules.MIN_GRADE}) средний балл будет не ниже ${formatAverage(target)}.",
                    bad = false
                )
                is TargetResult.Unreachable -> ResultCard(
                    "Цель недостижима одной оценкой",
                    "Даже за ${GradeRules.MAX_GRADE} средний балл станет ${formatAverage(result.bestPossibleAverage)}. " +
                        "Потребуется несколько хороших оценок подряд.",
                    bad = true
                )
            }
        }
    }
}

@Composable
private fun ResultCard(title: String, text: String, bad: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (bad) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
        }
    }
}
