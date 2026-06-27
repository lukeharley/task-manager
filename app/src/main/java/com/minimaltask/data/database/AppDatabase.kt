package com.minimaltask.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.minimaltask.data.model.Category
import com.minimaltask.data.model.FocusSession
import com.minimaltask.data.model.Task

@Database(
    entities = [Task::class, Category::class, FocusSession::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun focusSessionDao(): FocusSessionDao
}
