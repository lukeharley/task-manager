package com.minimaltask.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.minimaltask.data.model.Priority
import com.minimaltask.data.model.Task
import com.minimaltask.ui.components.PrioritySelector
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val DEFAULT_CATEGORY = "Generale"
private const val REMINDER_DELAY_MILLIS = 60 * 60 * 1_000L
private val DateFormatter = DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.ITALIAN)
private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ITALIAN)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskFormScreen(
    title: String,
    initialTask: Task?,
    onBack: () -> Unit,
    onSave: (Task) -> Unit,
    categories: List<String> = emptyList(),
    onDelete: ((Task) -> Unit)? = null
) {
    val zone = ZoneId.systemDefault()
    var taskTitle by remember(initialTask?.id) { mutableStateOf(initialTask?.title.orEmpty()) }
    var description by remember(initialTask?.id) { mutableStateOf(initialTask?.description.orEmpty()) }
    var category by remember(initialTask?.id) { mutableStateOf(initialTask?.category ?: DEFAULT_CATEGORY) }
    var priority by remember(initialTask?.id) { mutableStateOf(Priority.fromValue(initialTask?.priority ?: Priority.MEDIUM.value)) }
    var estimatedMinutes by remember(initialTask?.id) { mutableStateOf(initialTask?.estimatedMinutes?.toString().orEmpty()) }
    var dueDate by remember(initialTask?.id) {
        mutableStateOf(initialTask?.dueDate?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() })
    }
    var highFocus by remember(initialTask?.id) { mutableStateOf(initialTask?.highFocus ?: false) }
    // Un promemoria già passato non conta più come attivo.
    val pendingReminder = initialTask?.reminderTime?.takeIf { it > System.currentTimeMillis() }
    var reminderEnabled by remember(initialTask?.id) { mutableStateOf(pendingReminder != null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Indietro") }
                },
                actions = {
                    if (initialTask != null && onDelete != null) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Elimina task")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(taskTitle, { taskTitle = it }, Modifier.fillMaxWidth(), label = { Text("Titolo") }, singleLine = true)
            OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Descrizione") }, minLines = 3)
            OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text("Categoria") }, singleLine = true)
            if (categories.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { name ->
                        FilterChip(
                            selected = category.trim().equals(name, ignoreCase = true),
                            onClick = { category = name },
                            label = { Text(name) }
                        )
                    }
                }
            }
            PrioritySelector(selected = priority, onSelected = { if (it != null) priority = it })
            OutlinedTextField(
                value = estimatedMinutes,
                onValueChange = { estimatedMinutes = it.filter(Char::isDigit).take(3) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Stima minuti") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text("Scadenza", style = MaterialTheme.typography.titleSmall)
            val today = LocalDate.now()
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = dueDate == null, onClick = { dueDate = null }, label = { Text("Nessuna") })
                FilterChip(selected = dueDate == today, onClick = { dueDate = today }, label = { Text("Oggi") })
                FilterChip(
                    selected = dueDate == today.plusDays(1),
                    onClick = { dueDate = today.plusDays(1) },
                    label = { Text("Domani") }
                )
                val custom = dueDate?.takeIf { it != today && it != today.plusDays(1) }
                FilterChip(
                    selected = custom != null,
                    onClick = { showDatePicker = true },
                    label = { Text(custom?.format(DateFormatter) ?: "Scegli data…") }
                )
            }

            ToggleRow("Alta concentrazione", highFocus) { highFocus = it }
            ToggleRow(
                label = if (reminderEnabled && pendingReminder != null) {
                    "Promemoria alle ${Instant.ofEpochMilli(pendingReminder).atZone(zone).format(TimeFormatter)}"
                } else {
                    "Promemoria tra 1 ora"
                },
                checked = reminderEnabled
            ) { reminderEnabled = it }
            Spacer(Modifier.height(8.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = taskTitle.isNotBlank(),
                onClick = {
                    val cleanCategory = category.trim().ifBlank { DEFAULT_CATEGORY }
                    // Modificando un task si conserva il promemoria già programmato invece di spostarlo.
                    val reminderTime = if (reminderEnabled) {
                        pendingReminder ?: (System.currentTimeMillis() + REMINDER_DELAY_MILLIS)
                    } else null
                    onSave(
                        (initialTask ?: Task(title = taskTitle.trim(), category = cleanCategory)).copy(
                            title = taskTitle.trim(),
                            description = description.trim().ifBlank { null },
                            category = cleanCategory,
                            priority = priority.value,
                            reminderTime = reminderTime,
                            estimatedMinutes = estimatedMinutes.toIntOrNull(),
                            dueDate = dueDate?.atStartOfDay(zone)?.toInstant()?.toEpochMilli(),
                            highFocus = highFocus
                        )
                    )
                }
            ) { Text("Salva") }
        }
    }

    if (showDatePicker) {
        // Il DatePicker lavora in millisecondi UTC a mezzanotte, indipendentemente dal fuso locale.
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (dueDate ?: LocalDate.now())
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        dueDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annulla") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (confirmDelete && initialTask != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Eliminare il task?") },
            text = { Text("\"${initialTask.title}\" verrà eliminato definitivamente.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete(initialTask)
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Annulla") }
            }
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
