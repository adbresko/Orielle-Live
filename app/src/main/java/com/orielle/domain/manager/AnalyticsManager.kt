package com.orielle.domain.manager

import kotlinx.coroutines.flow.Flow

/**
 * Interface for analytics tracking across the app.
 * This provides a clean abstraction for tracking user actions and events.
 */
interface AnalyticsManager {

    /**
     * Track user sign-up event
     */
    suspend fun trackSignUp(method: String)

    /**
     * Track user sign-in event
     */
    suspend fun trackSignIn(method: String)

    /**
     * Track mood check-in event
     */
    suspend fun trackMoodCheckIn(mood: String, hasNotes: Boolean)

    /**
     * Track journal entry creation
     */
    suspend fun trackJournalEntryCreated(hasMood: Boolean, wordCount: Int)

    /**
     * Track chat message sent to Orielle
     */
    suspend fun trackChatMessageSent(messageLength: Int, conversationId: String)

    /**
     * Track premium subscription event
     */
    suspend fun trackPremiumSubscription(plan: String, price: Double)

    /**
     * Track feature usage
     */
    suspend fun trackFeatureUsage(feature: String)

    /**
     * Track screen view
     */
    suspend fun trackScreenView(screenName: String)

    /**
     * Track user engagement
     */
    suspend fun trackUserEngagement(action: String, value: String? = null)

    /**
     * Set user properties for analytics
     */
    suspend fun setUserProperties(userId: String, properties: Map<String, String>)

    /**
     * Track custom event
     */
    suspend fun trackCustomEvent(eventName: String, parameters: Map<String, Any> = emptyMap())
}