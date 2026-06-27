package com.minimaltask.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.minimaltask.data.preferences.AppPreferences
import com.minimaltask.data.preferences.AppThemeMode
import com.minimaltask.data.preferences.UserPreferences
import com.minimaltask.data.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val preferences: AppPreferences
) : ViewModel() {
    val preferencesState: StateFlow<UserPreferences> = preferences.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())
    val productDetails: StateFlow<ProductDetails?> = billingRepository.productDetails
    val billingMessage: StateFlow<String?> = billingRepository.billingMessage

    fun buyPremium(activity: Activity) = billingRepository.launchPurchase(activity)
    fun restorePurchases() = billingRepository.restorePurchases()

    fun setTheme(themeMode: AppThemeMode) {
        viewModelScope.launch { preferences.setTheme(themeMode) }
    }
}
