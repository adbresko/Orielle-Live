package com.orielle.domain.manager

import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for managing premium subscriptions and billing operations.
 */
interface BillingManager {

    /**
     * Checks if the user has an active premium subscription.
     */
    val isPremium: Flow<Boolean>

    /**
     * Gets the current subscription status.
     */
    suspend fun getSubscriptionStatus(): SubscriptionStatus

    /**
     * Initiates the purchase flow for a premium subscription.
     */
    suspend fun purchasePremium(): PurchaseResult

    /**
     * Restores previous purchases.
     */
    suspend fun restorePurchases(): RestoreResult

    /**
     * Gets available subscription products.
     */
    suspend fun getAvailableProducts(): List<SubscriptionProduct>
}

/**
 * Represents the status of a user's subscription.
 */
sealed class SubscriptionStatus {
    object NotSubscribed : SubscriptionStatus()
    object Subscribed : SubscriptionStatus()
    object Expired : SubscriptionStatus()
    object Pending : SubscriptionStatus()
    data class Error(val message: String) : SubscriptionStatus()
}

/**
 * Represents the result of a purchase operation.
 */
sealed class PurchaseResult {
    object Success : PurchaseResult()
    object Cancelled : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}

/**
 * Represents the result of a restore operation.
 */
sealed class RestoreResult {
    object Success : RestoreResult()
    object NoPurchasesFound : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

/**
 * Represents a subscription product available for purchase.
 */
data class SubscriptionProduct(
    val productId: String,
    val title: String,
    val description: String,
    val price: String,
    val billingPeriod: String
)