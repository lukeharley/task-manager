package com.minimaltask.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimaltask.viewmodel.CategoryViewModel
import com.minimaltask.viewmodel.TaskViewModel

@Composable
fun AddTaskScreen(
    onBack: () -> Unit,
    taskViewModel: TaskViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle()
    TaskFormScreen(
        title = "Nuovo task",
        initialTask = null,
        onBack = onBack,
        categories = categories.map { it.name },
        onSave = {
            taskViewModel.saveTask(it)
            onBack()
        }
    )
}
