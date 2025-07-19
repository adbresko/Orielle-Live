package com.orielle.data.manager

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.orielle.domain.manager.AnalyticsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManagerImpl @Inject constructor() : AnalyticsManager {

    private val analytics = Firebase.analytics

    override suspend fun trackSignUp(method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }

    override suspend fun trackSignIn(method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }

    override suspend fun trackMoodCheckIn(mood: String, hasNotes: Boolean) {
        analytics.logEvent("mood_check_in") {
            param("mood", mood)
            param("has_notes", if (hasNotes) "true" else "false")
        }
    }

    override suspend fun trackJournalEntryCreated(hasMood: Boolean, wordCount: Int) {
        analytics.logEvent("journal_entry_created") {
            param("has_mood", if (hasMood) "true" else "false")
            param("word_count", wordCount.toLong())
        }
    }

    override suspend fun trackChatMessageSent(messageLength: Int, conversationId: String) {
        analytics.logEvent("chat_message_sent") {
            param("message_length", messageLength.toLong())
            param("conversation_id", conversationId)
        }
    }

    override suspend fun trackPremiumSubscription(plan: String, price: Double) {
        analytics.logEvent(FirebaseAnalytics.Event.PURCHASE) {
            param(FirebaseAnalytics.Param.ITEM_ID, plan)
            param(FirebaseAnalytics.Param.PRICE, price)
            param(FirebaseAnalytics.Param.CURRENCY, "USD")
        }
    }

    override suspend fun trackFeatureUsage(feature: String) {
        analytics.logEvent("feature_usage") {
            param("feature_name", feature)
        }
    }

    override suspend fun trackScreenView(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }

    override suspend fun trackUserEngagement(action: String, value: String?) {
        analytics.logEvent("user_engagement") {
            param("action", action)
            value?.let { param("value", it) }
        }
    }

    override suspend fun setUserProperties(userId: String, properties: Map<String, String>) {
        analytics.setUserId(userId)
        properties.forEach { (key, value) ->
            analytics.setUserProperty(key, value)
        }
    }

    override suspend fun trackCustomEvent(eventName: String, parameters: Map<String, Any>) {
        analytics.logEvent(eventName) {
            parameters.forEach { (key, value) ->
                when (value) {
                    is String -> param(key, value)
                    is Long -> param(key, value)
                    is Int -> param(key, value.toLong())
                    is Double -> param(key, value)
                    is Boolean -> param(key, if (value) "true" else "false")
                }
            }
        }
    }
}