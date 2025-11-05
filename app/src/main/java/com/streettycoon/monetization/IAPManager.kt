package com.streettycoon.monetization

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manager for In-App Purchases using Google Play Billing
 * Handles token pack purchases
 */
class IAPManager(private val activity: Activity) {

    private var billingClient: BillingClient? = null
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled purchase")
            _purchaseState.value = PurchaseState.Cancelled
        } else {
            Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
            _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
        }
    }

    init {
        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(activity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected")
                    queryProducts()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }

    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("token_pack_small")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("token_pack_medium")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("token_pack_large")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Products queried: ${productDetailsList.size}")
                // Store products for display
            }
        }
    }

    /**
     * Purchase a product
     */
    fun purchaseProduct(productId: String) {
        // In a real implementation, you would:
        // 1. Get the ProductDetails for this productId
        // 2. Build the BillingFlowParams
        // 3. Launch the billing flow

        Log.d(TAG, "Purchase requested: $productId")
        _purchaseState.value = PurchaseState.Purchasing

        // TODO: Implement actual purchase flow with ProductDetails
        // For now, just simulate
        _purchaseState.value = PurchaseState.Error("IAP not fully implemented - add product details")
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Verify and acknowledge purchase
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }

            // Grant tokens based on product
            val tokens = when (purchase.products.firstOrNull()) {
                "token_pack_small" -> 100
                "token_pack_medium" -> 550
                "token_pack_large" -> 1500
                else -> 0
            }

            _purchaseState.value = PurchaseState.Success(tokens)
            Log.d(TAG, "Purchase successful: ${purchase.products}, tokens: $tokens")
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged")
            }
        }
    }

    fun disconnect() {
        billingClient?.endConnection()
    }

    companion object {
        private const val TAG = "IAPManager"
    }
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Purchasing : PurchaseState()
    data class Success(val tokens: Int) : PurchaseState()
    object Cancelled : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}
