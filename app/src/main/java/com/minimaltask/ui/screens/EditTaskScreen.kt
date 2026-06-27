package com.minimaltask.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimaltask.viewmodel.TaskViewModel

@Composable
fun EditTaskScreen(
    taskId: Int,
    onBack: () -> Unit,
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    val task by taskViewModel.task(taskId).collectAsStateWithLifecycle(initialValue = null)
    if (task == null) {
        Text("Task non trovato")
    } else {
        TaskFormScreen(
            title = "Modifica task",
            initialTask = task,
            onBack = onBack,
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
