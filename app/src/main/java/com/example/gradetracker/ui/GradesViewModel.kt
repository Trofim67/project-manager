package com.example.gradetracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradetracker.data.AppDatabase
import com.example.gradetracker.data.Discipline
import com.example.gradetracker.data.Grade
import com.example.gradetracker.data.GradeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** [loaded] = false, пока данные из базы ещё не прочитаны (чтобы не мигала «пустая» заглушка). */
data class AppData(
    val disciplines: List<Discipline> = emptyList(),
    val grades: List<Grade> = emptyList(),
    val loaded: Boolean = false
)

class GradesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = GradeRepository(AppDatabase.get(app))

    val data: StateFlow<AppData> =
        combine(repo.disciplines, repo.grades) { d, g -> AppData(d, g, true) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppData())

    fun saveDiscipline(d: Discipline) { viewModelScope.launch { repo.saveDiscipline(d) } }
    fun deleteDiscipline(d: Discipline) { viewModelScope.launch { repo.deleteDiscipline(d) } }
    fun saveGrade(g: Grade) { viewModelScope.launch { repo.saveGrade(g) } }
    fun deleteGrade(g: Grade) { viewModelScope.launch { repo.deleteGrade(g) } }
    fun loadSampleData() { viewModelScope.launch { repo.loadSampleData() } }
    fun clearAll() { viewModelScope.launch { repo.clearAll() } }
}
