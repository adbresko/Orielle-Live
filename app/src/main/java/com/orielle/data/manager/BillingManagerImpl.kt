package com.orielle.data.manager

import android.content.Context
import com.android.billingclient.api.*
import com.orielle.domain.manager.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BillingManager {

    private val billingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
        }
        .enablePendingPurchases()
        .build()

    private val _isPremium = MutableStateFlow(false)
    override val isPremium: Flow<Boolean> = _isPremium.asStateFlow()

    companion object {
        private const val PREMIUM_SUBSCRIPTION_ID = "orielle_premium_monthly"
        private const val PREMIUM_SUBSCRIPTION_YEARLY_ID = "orielle_premium_yearly"
    }

    init {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("Billing client connected")
                    checkSubscriptionStatus()
                } else {
                    Timber.e("Billing client connection failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Timber.w("Billing client disconnected")
            }
        })
    }

    override suspend fun getSubscriptionStatus(): SubscriptionStatus {
        return try {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val purchases = billingClient.queryPurchasesAsync(params)

            val hasActiveSubscription = purchases.purchasesList.any { purchase ->
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        (purchase.products.contains(PREMIUM_SUBSCRIPTION_ID) ||
                                purchase.products.contains(PREMIUM_SUBSCRIPTION_YEARLY_ID))
            }

            if (hasActiveSubscription) {
                SubscriptionStatus.Subscribed
            } else {
                SubscriptionStatus.NotSubscribed
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting subscription status")
            SubscriptionStatus.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun purchasePremium(): PurchaseResult {
        return try {
            // For now, return an error indicating that purchase flow needs Activity context
            // This will be implemented properly when we have the Activity context
            PurchaseResult.Error("Purchase flow requires Activity context - implement in UI layer")
        } catch (e: Exception) {
            Timber.e(e, "Error initiating purchase")
            PurchaseResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun restorePurchases(): RestoreResult {
        return try {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val purchases = billingClient.queryPurchasesAsync(params)

            val hasValidPurchase = purchases.purchasesList.any { purchase ->
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        (purchase.products.contains(PREMIUM_SUBSCRIPTION_ID) ||
                                purchase.products.contains(PREMIUM_SUBSCRIPTION_YEARLY_ID))
            }

            if (hasValidPurchase) {
                _isPremium.value = true
                RestoreResult.Success
            } else {
                RestoreResult.NoPurchasesFound
            }
        } catch (e: Exception) {
            Timber.e(e, "Error restoring purchases")
            RestoreResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun getAvailableProducts(): List<SubscriptionProduct> {
        return try {
            // Return mock products for now - these will be configured in Google Play Console
            listOf(
                SubscriptionProduct(
                    productId = PREMIUM_SUBSCRIPTION_ID,
                    title = "Orielle Premium Monthly",
                    description = "Unlock all premium features with monthly subscription",
                    price = "$4.99",
                    billingPeriod = "Monthly"
                ),
                SubscriptionProduct(
                    productId = PREMIUM_SUBSCRIPTION_YEARLY_ID,
                    title = "Orielle Premium Yearly",
                    description = "Unlock all premium features with yearly subscription (Save 40%)",
                    price = "$29.99",
                    billingPeriod = "Yearly"
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting available products")
            emptyList()
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
                        _isPremium.value = true
                        Timber.d("Purchase acknowledged successfully")
                    } else {
                        Timber.e("Failed to acknowledge purchase: ${billingResult.debugMessage}")
                    }
                }
            } else {
                _isPremium.value = true
            }
        }
    }

    private fun checkSubscriptionStatus() {
        // Implementation to check current subscription status
        // This would typically query the billing client for active purchases
    }
}