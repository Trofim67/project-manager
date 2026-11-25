package com.example.gradetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Discipline::class, Grade::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun disciplineDao(): DisciplineDao
    abstract fun gradeDao(): GradeDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "grades.db"
                ).build().also { instance = it }
            }
    }
}
