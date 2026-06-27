package com.minimaltask.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minimaltask.data.model.Priority
import com.minimaltask.data.model.Task
import com.minimaltask.data.repository.TaskRepository
import com.minimaltask.export.ExportRepository
import com.minimaltask.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SmartFilter { NONE, QUICK, URGENT, TODAY, HIGH_FOCUS }

data class TaskFilters(
    val query: String = "",
    val category: String? = null,
    val priority: Priority? = null,
    val smartFilter: SmartFilter = SmartFilter.NONE
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val reminderScheduler: ReminderScheduler,
    private val exportRepository: ExportRepository
) : ViewModel() {
    private val filters = MutableStateFlow(TaskFilters())
    val currentFilters: StateFlow<TaskFilters> = filters

    val activeTasks: StateFlow<List<Task>> = filters.flatMapLatest { filter ->
        repository.search(filter.query, filter.category, filter.priority, completed = false)
    }.combine(filters) { tasks, filter -> applySmartFilter(tasks, filter.smartFilter) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val completedTasks: StateFlow<List<Task>> = repository.observeCompletedTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun task(id: Int) = repository.observeTask(id)

    fun updateQuery(query: String) {
        filters.value = filters.value.copy(query = query)
    }

    fun updateCategory(category: String?) {
        filters.value = filters.value.copy(category = category)
    }

    fun updatePriority(priority: Priority?) {
        filters.value = filters.value.copy(priority = priority)
    }

    fun updateSmartFilter(filter: SmartFilter) {
        filters.value = filters.value.copy(smartFilter = filter)
    }

    fun saveTask(task: Task) {
        viewModelScope.launch {
            val id = if (task.id == 0) repository.save(task).toInt() else {
                repository.update(task)
                task.id
            }
            if (task.reminderTime != null) reminderScheduler.schedule(task.copy(id = id)) else reminderScheduler.cancel(id)
        }
    }

    fun complete(task: Task) {
        viewModelScope.launch {
            repository.complete(task.id)
            reminderScheduler.cancel(task.id)
        }
    }

    fun restore(task: Task) {
        viewModelScope.launch { repository.restore(task.id) }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
            reminderScheduler.cancel(task.id)
        }
    }

    fun exportTxt(onDone: (File) -> Unit) {
        viewModelScope.launch { onDone(exportRepository.exportTxt(repository.getAllTasks())) }
    }

    fun exportPdf(onDone: (File) -> Unit) {
        viewModelScope.launch { onDone(exportRepository.exportPdf(repository.getAllTasks())) }
    }

    private fun applySmartFilter(tasks: List<Task>, filter: SmartFilter): List<Task> {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        return when (filter) {
            SmartFilter.NONE -> tasks
            SmartFilter.QUICK -> tasks.filter { (it.estimatedMinutes ?: Int.MAX_VALUE) < 5 }
            SmartFilter.URGENT -> tasks.filter { it.priority == Priority.HIGH.value }
            SmartFilter.TODAY -> tasks.filter { task ->
                task.dueDate?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == today } == true
            }
            SmartFilter.HIGH_FOCUS -> tasks.filter { it.highFocus }
        }
    }
}
