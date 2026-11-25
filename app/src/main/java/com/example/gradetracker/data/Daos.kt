package com.example.gradetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DisciplineDao {
    @Query("SELECT * FROM disciplines ORDER BY id")
    fun observeAll(): Flow<List<Discipline>>

    @Query("SELECT * FROM disciplines")
    suspend fun getAll(): List<Discipline>

    @Insert
    suspend fun insert(discipline: Discipline): Long

    @Update
    suspend fun update(discipline: Discipline)

    @Delete
    suspend fun delete(discipline: Discipline)

    @Query("DELETE FROM disciplines")
    suspend fun deleteAll()
}

@Dao
interface GradeDao {
    @Query("SELECT * FROM grades ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<Grade>>

    @Insert
    suspend fun insert(grade: Grade): Long

    @Insert
    suspend fun insertAll(grades: List<Grade>)

    @Update
    suspend fun update(grade: Grade)

    @Delete
    suspend fun delete(grade: Grade)

    @Query("DELETE FROM grades")
    suspend fun deleteAll()
}
