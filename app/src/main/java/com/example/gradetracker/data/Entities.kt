package com.example.gradetracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Учебная дисциплина. Связь с оценками: 1 дисциплина — N оценок. */
@Entity(tableName = "disciplines")
data class Discipline(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val teacherName: String? = null
)

/**
 * Оценка. date хранится как epochDay (количество дней от 1970-01-01).
 * При удалении дисциплины её оценки удаляются каскадно (UI предупреждает об этом).
 */
@Entity(
    tableName = "grades",
    foreignKeys = [
        ForeignKey(
            entity = Discipline::class,
            parentColumns = ["id"],
            childColumns = ["disciplineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("disciplineId")]
)
data class Grade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val disciplineId: Long,
    val value: Int,
    val date: Long,
    val workType: String,
    val comment: String = ""
)
