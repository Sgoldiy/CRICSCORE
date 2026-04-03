package com.cricpeak.scorelive.billing

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import kotlinx.coroutines.flow.*

class BillingViewModel(application: Application) : AndroidViewModel(application) {

    private val premiumManager = PremiumManager(application)
    private val billingManager = BillingManager(application, premiumManager, viewModelScope)

    val isPremium: StateFlow<Boolean> = premiumManager.isPremium
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val products: StateFlow<List<ProductDetails>> = billingManager.products

    fun buyProduct(activity: Activity, product: ProductDetails) {
        billingManager.launchBillingFlow(activity, product)
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.release() // ✅ IMPORTANT FIX
    }
}