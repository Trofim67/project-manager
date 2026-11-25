package com.example.gradetracker.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GradeRepository(private val db: AppDatabase) {
    private val disciplineDao = db.disciplineDao()
    private val gradeDao = db.gradeDao()

    val disciplines: Flow<List<Discipline>> =
        disciplineDao.observeAll().map { list -> list.sortedBy { it.name.lowercase() } }

    val grades: Flow<List<Grade>> = gradeDao.observeAll()

    suspend fun saveDiscipline(d: Discipline) {
        if (d.id == 0L) disciplineDao.insert(d) else disciplineDao.update(d)
    }

    /** Оценки дисциплины удаляются каскадно (ForeignKey.CASCADE). */
    suspend fun deleteDiscipline(d: Discipline) = disciplineDao.delete(d)

    suspend fun saveGrade(g: Grade) {
        if (g.id == 0L) gradeDao.insert(g) else gradeDao.update(g)
    }

    suspend fun deleteGrade(g: Grade) = gradeDao.delete(g)

    suspend fun clearAll() = db.withTransaction {
        gradeDao.deleteAll()
        disciplineDao.deleteAll()
    }

    /** Добавляет тестовые дисциплины; уже существующие по названию пропускаются. */
    suspend fun loadSampleData() = db.withTransaction {
        val existing = disciplineDao.getAll().map { it.name.lowercase() }.toSet()
        SampleData.disciplines.forEach { s ->
            if (s.name.lowercase() in existing) return@forEach
            val id = disciplineDao.insert(Discipline(name = s.name, teacherName = s.teacher))
            gradeDao.insertAll(
                s.grades.map {
                    Grade(
                        disciplineId = id,
                        value = it.value,
                        date = SampleData.dateOf(it),
                        workType = it.type,
                        comment = it.comment
                    )
                }
            )
        }
    }
}
