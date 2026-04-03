package com.cricpeak.scorelive.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingManager(
    private val context: Context,
    private val premiumManager: PremiumManager,
    private val scope: CoroutineScope
) : PurchasesUpdatedListener {

    private val TAG = "BillingManager"

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
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
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    checkPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                startConnection()
            }
        })
    }

    private fun queryProducts() {
        scope.launch {
            val list = mutableListOf<ProductDetails>()

            // SUBS
            val subsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    productIds.filter { it != "lifetime_access" }.map {
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(it)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    }
                ).build()

            val subsResult = billingClient.queryProductDetails(subsParams)
            subsResult.productDetailsList?.let { list.addAll(it) }

            // INAPP
            val inAppParams = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf("lifetime_access").map {
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(it)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    }
                ).build()

            val inAppResult = billingClient.queryProductDetails(inAppParams)
            inAppResult.productDetailsList?.let { list.addAll(it) }

            _products.value = list
        }
    }

    fun launchBillingFlow(activity: Activity, product: ProductDetails) {

        val paramsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product)

        product.subscriptionOfferDetails?.firstOrNull()?.let {
            paramsBuilder.setOfferToken(it.offerToken)
        }

        val billingParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(paramsBuilder.build()))
            .build()

        billingClient.launchBillingFlow(activity, billingParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {

        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { handlePurchase(it) }
        }
    }

    private fun handlePurchase(purchase: Purchase) {

        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        if (!purchase.isAcknowledged) {

            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.acknowledgePurchase(params) {
                if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch { premiumManager.setPremiumStatus(true) }
                }
            }

        } else {
            scope.launch { premiumManager.setPremiumStatus(true) }
        }
    }

    private fun checkPurchases() {
        scope.launch {

            val subs = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            val hasSub = subs.purchasesList.any {
                it.purchaseState == Purchase.PurchaseState.PURCHASED && it.isAcknowledged
            }

            if (hasSub) {
                premiumManager.setPremiumStatus(true)
                return@launch
            }

            val inApp = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

            val hasInApp = inApp.purchasesList.any {
                it.purchaseState == Purchase.PurchaseState.PURCHASED
            }

            premiumManager.setPremiumStatus(hasInApp)
        }
    }

    fun release() {
        billingClient.endConnection()
    }
}