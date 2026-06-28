package com.minimaltask.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimaltask.data.model.Priority
import com.minimaltask.ui.components.CategoryChip
import com.minimaltask.ui.components.PrioritySelector
import com.minimaltask.ui.components.TaskItem
import com.minimaltask.viewmodel.BillingViewModel
import com.minimaltask.viewmodel.CategoryViewModel
import com.minimaltask.viewmodel.SmartFilter
import com.minimaltask.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    onPremium: () -> Unit,
    taskViewModel: TaskViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel()
) {
    val tasks by taskViewModel.activeTasks.collectAsStateWithLifecycle()
    val completed by taskViewModel.completedTasks.collectAsStateWithLifecycle()
    val filters by taskViewModel.currentFilters.collectAsStateWithLifecycle()
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle()
    val preferences by billingViewModel.preferencesState.collectAsStateWithLifecycle()
    var showArchive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MinimalTask") },
                actions = {
                    IconButton(onClick = onPremium) {
                        Icon(Icons.Outlined.Diamond, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Outlined.Add, null) },
                text = { Text("Task") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = filters.query,
                        onValueChange = taskViewModel::updateQuery,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Cerca") }
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryChip("Tutte", filters.category == null) { taskViewModel.updateCategory(null) }
                        categories.forEach { category ->
                            CategoryChip(category.name, filters.category == category.name) {
                                taskViewModel.updateCategory(category.name)
                            }
                        }
                    }
                    PrioritySelector(
                        selected = filters.priority,
                        onSelected = { taskViewModel.updatePriority(it) },
                        allowClear = true
                    )
                    AnimatedVisibility(preferences.premiumActive) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SmartFilter.entries.forEach { smart ->
                                CategoryChip(
                                    name = smart.label(),
                                    selected = filters.smartFilter == smart,
                                    onClick = { taskViewModel.updateSmartFilter(smart) }
                                )
                            }
                        }
                    }
                    if (!preferences.premiumActive) {
                        Button(onClick = onPremium) { Text("Sblocca filtri intelligenti") }
                    }
                }
            }
            if (tasks.isEmpty()) {
                item { Text("Nessun task attivo", style = MaterialTheme.typography.titleMedium) }
            }
            items(tasks, key = { it.id }) { task ->
                TaskItem(task = task, onClick = { onEdit(task.id) }, onComplete = { taskViewModel.complete(task) })
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Archivio completati", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { showArchive = !showArchive }) {
                        Text(if (showArchive) "Nascondi" else "Mostra")
                    }
                }
            }
            if (showArchive) {
                items(completed, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        onClick = { onEdit(task.id) },
                        onComplete = { },
                        onRestore = { taskViewModel.restore(task) }
                    )
                }
            }
        }
    }
}

private fun SmartFilter.label(): String = when (this) {
    SmartFilter.NONE -> "Tutti"
    SmartFilter.QUICK -> "Veloci"
    SmartFilter.URGENT -> "Urgenti"
    SmartFilter.TODAY -> "Oggi"
    SmartFilter.HIGH_FOCUS -> "Alta concentrazione"
}
