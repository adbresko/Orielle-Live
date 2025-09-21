package com.orielle.util

import java.util.*

/**
 * Utility class for generating time-based greetings.
 * Provides dynamic greetings based on the current time of day.
 */
object GreetingUtils {

    /**
     * Gets a time-appropriate greeting based on the current time.
     *
     * @param userName The user's name to include in the greeting
     * @param date The date/time to base the greeting on (defaults to current time)
     * @return Formatted greeting string
     */
    fun getTimeBasedGreeting(userName: String?, date: Date = Date()): String {
        val calendar = Calendar.getInstance().apply { time = date }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            in 18..21 -> "Good evening"
            else -> "Good evening" // 22-4 (late night/early morning)
        }

        val displayName = userName ?: "User"
        return "$greeting, $displayName."
    }

    /**
     * Gets a more casual, varied greeting based on time and random selection.
     *
     * @param userName The user's name to include in the greeting
     * @param date The date/time to base the greeting on (defaults to current time)
     * @return Formatted greeting string with variety
     */
    fun getCasualGreeting(userName: String?, date: Date = Date()): String {
        val calendar = Calendar.getInstance().apply { time = date }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val displayName = userName ?: "User"

        val morningGreetings = listOf(
            "Good morning",
            "Rise and shine",
            "Morning",
            "Good day"
        )

        val afternoonGreetings = listOf(
            "Good afternoon",
            "Afternoon",
            "Hope your day is going well",
            "Good day"
        )

        val eveningGreetings = listOf(
            "Good evening",
            "Evening",
            "Hope you had a good day",
            "Good evening"
        )

        val lateNightGreetings = listOf(
            "Good evening",
            "Still up?",
            "Evening",
            "Good evening"
        )

        val greeting = when (hour) {
            in 5..11 -> morningGreetings.random()
            in 12..17 -> afternoonGreetings.random()
            in 18..21 -> eveningGreetings.random()
            else -> lateNightGreetings.random()
        }

        return "$greeting, $displayName."
    }
}
