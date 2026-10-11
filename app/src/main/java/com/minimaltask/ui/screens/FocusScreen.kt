package com.minimaltask.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimaltask.viewmodel.BillingViewModel
import com.minimaltask.viewmodel.FocusMode
import com.minimaltask.viewmodel.FocusViewModel
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusScreen(
    onPremium: () -> Unit,
    focusViewModel: FocusViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel()
) {
    val state by focusViewModel.uiState.collectAsStateWithLifecycle()
    val preferences by billingViewModel.preferencesState.collectAsStateWithLifecycle()
    val totalMinutes by focusViewModel.totalFocusMinutes.collectAsStateWithLifecycle()
    val minutes = state.remainingSeconds / 60
    val seconds = state.remainingSeconds % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Focus", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text("%02d:%02d".format(minutes, seconds), style = MaterialTheme.typography.displayLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FocusMode.entries.forEach { mode ->
                FilterChip(
                    selected = state.selectedMode == mode,
                    onClick = {
                        if (mode.premium && !preferences.premiumActive) onPremium() else focusViewModel.selectMode(mode, preferences.premiumActive)
                    },
                    label = { Text(if (mode.premium && !preferences.premiumActive) "${mode.label} Premium" else mode.label) }
                )
            }
        }
        if (state.selectedMode == FocusMode.CUSTOM && preferences.premiumActive) {
            // Testo locale: l'utente può cancellare e riscrivere liberamente; il valore
            // viene applicato solo quando rientra nell'intervallo consentito.
            var customText by remember { mutableStateOf(state.customMinutes.toString()) }
            val customValue = customText.toIntOrNull()
            val customValid = customValue != null &&
                customValue in FocusViewModel.MIN_CUSTOM_MINUTES..FocusViewModel.MAX_CUSTOM_MINUTES
            OutlinedTextField(
                value = customText,
                onValueChange = { text ->
                    customText = text.filter(Char::isDigit).take(3)
                    customText.toIntOrNull()
                        ?.takeIf { it in FocusViewModel.MIN_CUSTOM_MINUTES..FocusViewModel.MAX_CUSTOM_MINUTES }
                        ?.let(focusViewModel::setCustomMinutes)
                },
                label = { Text("Minuti") },
                isError = !customValid,
                supportingText = {
                    Text("Da ${FocusViewModel.MIN_CUSTOM_MINUTES} a ${FocusViewModel.MAX_CUSTOM_MINUTES} minuti")
                },
                enabled = !state.running,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { if (state.running) focusViewModel.pause() else focusViewModel.start() }) {
                Text(if (state.running) "Pausa" else "Avvia")
            }
            Button(onClick = focusViewModel::reset) { Text("Reset") }
        }
        Text("Totale focus completato: $totalMinutes min", modifier = Modifier.fillMaxWidth())
    }
}
