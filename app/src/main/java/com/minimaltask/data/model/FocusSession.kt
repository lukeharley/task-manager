package com.minimaltask.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startedAt: Long,
    val durationMinutes: Int,
    val completed: Boolean
)
