package com.minimaltask.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.minimaltask.data.model.Priority

@Composable
fun PrioritySelector(
    selected: Priority?,
    onSelected: (Priority?) -> Unit,
    allowClear: Boolean = false
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (allowClear) {
            FilterChip(
                selected = selected == null,
                onClick = { onSelected(null) },
                label = { Text("Tutte") }
            )
        }
        Priority.entries.forEach { priority ->
            FilterChip(
                selected = selected == priority,
                onClick = { onSelected(priority) },
                label = { Text(priority.label) }
            )
        }
    }
}
