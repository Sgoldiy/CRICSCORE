package com.cricpeak.scorelive.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingManager(
    private val context: Context,
    private val premiumManager: PremiumManager,
    private val scope: CoroutineScope
) : PurchasesUpdatedListener {

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products = _products.asStateFlow()

    private val productIds = listOf(
        "monthly_sub",
        "half_yearly_sub",
        "yearly_sub",
        "lifetime_access"
    )

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    checkPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart connection
                startConnection()
            }
        })
    }

    private fun queryProducts() {
        val productList = productIds.map { productId ->
            val productType = if (productId == "lifetime_access") {
                BillingClient.ProductType.INAPP
            } else {
                BillingClient.ProductType.SUBS
            }
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                println("Products loaded: ${productDetailsList.size}")
                _products.value = productDetailsList
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {

        if (!billingClient.isReady) {
            println("Billing not ready yet ❌")
            return
        }

        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull()
            ?.offerToken

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .apply {
                    offerToken?.let { setOfferToken(it) }
                }
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)

        println("Billing launch result: ${result.responseCode}")
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        scope.launch {
                            premiumManager.setPremiumStatus(true)
                        }
                    }
                }
            } else {
                scope.launch {
                    premiumManager.setPremiumStatus(true)
                }
            }
        }
    }

    private fun checkPurchases() {
        // Check Subscriptions
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val isPremium = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                if (isPremium) {
                    scope.launch { premiumManager.setPremiumStatus(true) }
                } else {
                    // Also check INAPP for lifetime
                    checkLifetimePurchase()
                }
            }
        }
    }

    private fun checkLifetimePurchase() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val isPremium = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                scope.launch { premiumManager.setPremiumStatus(isPremium) }
            }
        }
    }
}
