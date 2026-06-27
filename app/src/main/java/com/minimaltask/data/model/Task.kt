package com.minimaltask.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String? = null,
    val priority: Int = Priority.MEDIUM.value,
    val category: String,
    val createdAt: Long = System.currentTimeMillis(),
    val completed: Boolean = false,
    val reminderTime: Long? = null,
    val completedAt: Long? = null,
    val estimatedMinutes: Int? = null,
    val dueDate: Long? = null,
    val highFocus: Boolean = false
)
