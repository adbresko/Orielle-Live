package com.orielle.data.manager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.orielle.domain.manager.AnalyticsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AnalyticsManager {

    private val analytics = FirebaseAnalytics.getInstance(context)

    override suspend fun trackSignUp(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
    }

    override suspend fun trackSignIn(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    override suspend fun trackMoodCheckIn(mood: String, hasNotes: Boolean) {
        val bundle = Bundle().apply {
            putString("mood", mood)
            putString("has_notes", if (hasNotes) "true" else "false")
        }
        analytics.logEvent("mood_check_in", bundle)
    }

    override suspend fun trackJournalEntryCreated(hasMood: Boolean, wordCount: Int) {
        val bundle = Bundle().apply {
            putString("has_mood", if (hasMood) "true" else "false")
            putLong("word_count", wordCount.toLong())
        }
        analytics.logEvent("journal_entry_created", bundle)
    }

    override suspend fun trackChatMessageSent(messageLength: Int, conversationId: String) {
        val bundle = Bundle().apply {
            putLong("message_length", messageLength.toLong())
            putString("conversation_id", conversationId)
        }
        analytics.logEvent("chat_message_sent", bundle)
    }

    override suspend fun trackPremiumSubscription(plan: String, price: Double) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, plan)
            putDouble(FirebaseAnalytics.Param.PRICE, price)
            putString(FirebaseAnalytics.Param.CURRENCY, "USD")
        }
        analytics.logEvent(FirebaseAnalytics.Event.PURCHASE, bundle)
    }

    override suspend fun trackFeatureUsage(feature: String) {
        val bundle = Bundle().apply {
            putString("feature_name", feature)
        }
        analytics.logEvent("feature_usage", bundle)
    }

    override suspend fun trackScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    @SuppressLint("InvalidAnalyticsName")
    override suspend fun trackUserEngagement(action: String, value: String?) {
        val bundle = Bundle().apply {
            putString("action", action)
            value?.let { putString("value", it) }
        }
        analytics.logEvent("user_engagement", bundle)
    }

    override suspend fun setUserProperties(userId: String, properties: Map<String, String>) {
        analytics.setUserId(userId)
        properties.forEach { (key, value) ->
            analytics.setUserProperty(key, value)
        }
    }

    override suspend fun trackCustomEvent(eventName: String, parameters: Map<String, Any>) {
        val bundle = Bundle().apply {
            parameters.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Long -> putLong(key, value)
                    is Int -> putLong(key, value.toLong())
                    is Double -> putDouble(key, value)
                    is Boolean -> putString(key, if (value) "true" else "false")
                }
            }
        }
        analytics.logEvent(eventName, bundle)
    }
}