package com.minimaltask.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimaltask.data.model.Task
import com.minimaltask.viewmodel.CategoryViewModel
import com.minimaltask.viewmodel.TaskViewModel
import kotlinx.coroutines.flow.map

private sealed interface TaskLoad {
    data object Loading : TaskLoad
    data class Loaded(val task: Task?) : TaskLoad
}

@Composable
fun EditTaskScreen(
    taskId: Int,
    onBack: () -> Unit,
    taskViewModel: TaskViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    // Il Flow va creato una sola volta per taskId, altrimenti ogni ricomposizione
    // ricomincerebbe la raccolta mostrando di nuovo lo stato iniziale.
    val taskFlow = remember(taskId) { taskViewModel.task(taskId).map<Task?, TaskLoad> { TaskLoad.Loaded(it) } }
    val load by taskFlow.collectAsStateWithLifecycle(initialValue = TaskLoad.Loading)
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle()

    when (val current = load) {
        TaskLoad.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is TaskLoad.Loaded -> {
            val task = current.task
            if (task == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Task non trovato") }
            } else {
                TaskFormScreen(
                    title = "Modifica task",
                    initialTask = task,
                    onBack = onBack,
                    categories = categories.map { it.name },
                    onSave = {
                        taskViewModel.saveTask(it)
                        onBack()
                    },
                    onDelete = {
                        taskViewModel.delete(it)
                        onBack()
                    }
                )
            }
        }
    }
}
