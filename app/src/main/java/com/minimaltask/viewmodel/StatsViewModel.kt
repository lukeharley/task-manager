package com.minimaltask.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimaltask.data.model.Task
import com.minimaltask.data.repository.FocusRepository
import com.minimaltask.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val weeklyCompleted: List<Int> = List(7) { 0 },
    val monthlyCompleted: List<Int> = List(30) { 0 },
    val totalFocusMinutes: Int = 0
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    taskRepository: TaskRepository,
    focusRepository: FocusRepository
) : ViewModel() {
    val uiState: StateFlow<StatsUiState> = combine(
        taskRepository.observeAllCompleted(),
        focusRepository.observeTotalCompletedMinutes()
    ) { completedTasks, focusMinutes ->
        StatsUiState(
            weeklyCompleted = bucketCompleted(completedTasks, 7),
            monthlyCompleted = bucketCompleted(completedTasks, 30),
            totalFocusMinutes = focusMinutes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    private fun bucketCompleted(tasks: List<Task>, days: Int): List<Int> {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        return (days - 1 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            tasks.count { task ->
                task.completedAt?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == day } == true
            }
        }
    }
}
