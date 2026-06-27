package com.minimaltask.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minimaltask.viewmodel.BillingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    billingViewModel: BillingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val preferences by billingViewModel.preferencesState.collectAsStateWithLifecycle()
    val product by billingViewModel.productDetails.collectAsStateWithLifecycle()
    val message by billingViewModel.billingMessage.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("MinimalTask Premium", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text(if (preferences.premiumActive) "Premium attivo" else "Acquisto una tantum: premium_unlock")
            PremiumFeature("Temi chiaro, scuro e 3 varianti colorate")
            PremiumFeature("Statistiche avanzate con grafici settimanali e mensili")
            PremiumFeature("Pomodoro, Deep Work e sessioni personalizzate")
            PremiumFeature("Esportazione locale TXT e PDF")
            PremiumFeature("Filtri intelligenti")
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !preferences.premiumActive,
                onClick = {
                    (context as? Activity)?.let(billingViewModel::buyPremium)
                }
            ) {
                Text(if (preferences.premiumActive) "Già sbloccato" else product?.oneTimePurchaseOfferDetails?.formattedPrice ?: "Acquista")
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = billingViewModel::restorePurchases) {
                Text("Ripristina acquisto")
            }
            message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun PremiumFeature(text: String) {
    Card(Modifier.fillMaxWidth()) {
        Text(text, modifier = Modifier.padding(14.dp))
    }
}
