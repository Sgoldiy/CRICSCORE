package com.cricpeak.scorelive.billing

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BillingViewModel(application: Application) : AndroidViewModel(application) {
    private val premiumManager = PremiumManager(application)
    private val billingManager = BillingManager(application, premiumManager, viewModelScope)

    val isPremium: StateFlow<Boolean> = premiumManager.isPremium
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val products: StateFlow<List<ProductDetails>> = billingManager.products

    fun buyProduct(activity: Activity, productDetails: ProductDetails) {
        billingManager.launchBillingFlow(activity, productDetails)
    }
}
