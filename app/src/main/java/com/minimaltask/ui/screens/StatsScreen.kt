package com.minimaltask.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimaltask.viewmodel.BillingViewModel
import com.minimaltask.viewmodel.StatsViewModel

@Composable
fun StatsScreen(
    onPremium: () -> Unit,
    statsViewModel: StatsViewModel = hiltViewModel(),
    billingViewModel: BillingViewModel = hiltViewModel()
) {
    val preferences by billingViewModel.preferencesState.collectAsStateWithLifecycle()
    val state by statsViewModel.uiState.collectAsStateWithLifecycle()
    if (!preferences.premiumActive) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Statistiche Premium", style = MaterialTheme.typography.headlineMedium)
            Text("Sblocca i grafici settimanali, mensili e il riepilogo focus.")
            Button(onClick = onPremium) { Text("Vai a Premium") }
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Statistiche", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text("Task completati per giorno")
        BarChart(values = state.weeklyCompleted, modifier = Modifier.fillMaxWidth().height(160.dp))
        Text("Ultimi 30 giorni")
        BarChart(values = state.monthlyCompleted, modifier = Modifier.fillMaxWidth().height(160.dp))
        Text("Tempo totale focus: ${state.totalFocusMinutes} minuti", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun BarChart(values: List<Int>, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.secondary
    Canvas(modifier = modifier) {
        drawBars(values, color)
    }
}

private fun DrawScope.drawBars(values: List<Int>, color: Color) {
    val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    val gap = 4.dp.toPx()
    val width = (size.width - gap * (values.size - 1)) / values.size.coerceAtLeast(1)
    values.forEachIndexed { index, value ->
        val barHeight = size.height * (value.toFloat() / max.toFloat())
        drawRoundRect(
            color = color,
            topLeft = Offset(index * (width + gap), size.height - barHeight),
            size = Size(width, barHeight.coerceAtLeast(2.dp.toPx())),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
    }
}
