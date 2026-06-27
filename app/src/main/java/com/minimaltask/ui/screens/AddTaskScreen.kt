package com.minimaltask.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.minimaltask.data.model.Task
import com.minimaltask.viewmodel.TaskViewModel

@Composable
fun AddTaskScreen(
    onBack: () -> Unit,
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    TaskFormScreen(
        title = "Nuovo task",
        initialTask = null,
        onBack = onBack,
        onSave = {
            taskViewModel.saveTask(it)
            onBack()
        }
    )
}
