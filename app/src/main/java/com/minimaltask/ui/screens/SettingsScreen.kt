package com.minimaltask.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimaltask.data.preferences.AppThemeMode
import com.minimaltask.data.model.Category
import com.minimaltask.viewmodel.BillingViewModel
import com.minimaltask.viewmodel.CategoryViewModel
import com.minimaltask.viewmodel.TaskViewModel

@Composable
fun SettingsScreen(
    onPremium: () -> Unit,
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle()
    val preferences by billingViewModel.preferencesState.collectAsStateWithLifecycle()
    var categoryName by remember { mutableStateOf("") }
    var exportMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Impostazioni", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            Text("Categorie", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Nuova categoria") },
                    singleLine = true
                )
                Button(onClick = {
                    categoryViewModel.add(categoryName)
                    categoryName = ""
                }) { Text("Aggiungi") }
            }
        }
        items(categories, key = { it.id }) { category ->
            CategoryEditorRow(
                category = category,
                onUpdate = categoryViewModel::update,
                onDelete = categoryViewModel::delete
            )
        }
        item { Divider() }
        item {
            Text("Tema", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppThemeMode.entries.forEach { theme ->
                    val locked = theme.name.startsWith("COLOR") && !preferences.premiumActive
                    Button(
                        onClick = { if (locked) onPremium() else billingViewModel.setTheme(theme) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${theme.label()}${if (locked) " Premium" else ""}")
                    }
                }
            }
        }
        item { Divider() }
        item {
            Text("Esportazione locale", style = MaterialTheme.typography.titleMedium)
            if (!preferences.premiumActive) {
                Button(onClick = onPremium) { Text("Sblocca esportazione") }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { taskViewModel.exportTxt { exportMessage = it.absolutePath } }) { Text("TXT") }
                    Button(onClick = { taskViewModel.exportPdf { exportMessage = it.absolutePath } }) { Text("PDF") }
                }
                exportMessage?.let { Text("Salvato in: $it", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun CategoryEditorRow(
    category: Category,
    onUpdate: (Category, String) -> Unit,
    onDelete: (Category) -> Unit
) {
    var name by remember(category.id) { mutableStateOf(category.name) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Button(onClick = { onUpdate(category, name) }) { Text("Salva") }
        IconButton(onClick = { onDelete(category) }) {
            Icon(Icons.Outlined.Delete, null)
        }
    }
}

private fun AppThemeMode.label(): String = when (this) {
    AppThemeMode.LIGHT -> "Chiaro"
    AppThemeMode.DARK -> "Scuro"
    AppThemeMode.COLOR_BLUE -> "Colorato blu"
    AppThemeMode.COLOR_GREEN -> "Colorato verde"
    AppThemeMode.COLOR_ROSE -> "Colorato rosa"
}
