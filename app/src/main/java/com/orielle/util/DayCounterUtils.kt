package com.orielle.util

import java.util.*

/**
 * Utility functions for calculating day counts and streaks.
 */
object DayCounterUtils {

    /**
     * Calculates the number of days between two dates.
     * @param startDate The starting date
     * @param endDate The ending date (defaults to current date)
     * @return Number of days between the dates
     */
    fun getDaysBetween(startDate: Date, endDate: Date = Date()): Int {
        val start = Calendar.getInstance().apply { time = startDate }
        val end = Calendar.getInstance().apply { time = endDate }

        // Set both dates to start of day for accurate day counting
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)

        end.set(Calendar.HOUR_OF_DAY, 0)
        end.set(Calendar.MINUTE, 0)
        end.set(Calendar.SECOND, 0)
        end.set(Calendar.MILLISECOND, 0)

        val diffInMillis = end.timeInMillis - start.timeInMillis
        return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
    }

    /**
     * Calculates the number of days since a given start date.
     * @param startDate The starting date
     * @return Number of days since the start date (minimum 1)
     */
    fun getDaysSince(startDate: Date): Int {
        val days = getDaysBetween(startDate)
        val result = maxOf(1, days + 1) // Ensure minimum of 1 day
        println("DayCounterUtils: Start date: $startDate, Days between: $days, Result: $result")
        return result
    }

    /**
     * Gets a fallback start date if no mood check-ins exist.
     * This uses a more reasonable date - 30 days ago from today.
     * @return A fallback start date
     */
    fun getFallbackStartDate(): Date {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        // Use 30 days ago as fallback (more reasonable than January 1st)
        calendar.add(Calendar.DAY_OF_MONTH, -30)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val fallbackDate = calendar.time
        println("DayCounterUtils: Today: $today, Fallback date (30 days ago): $fallbackDate")
        return fallbackDate
    }

    /**
     * Formats the day count for display.
     * @param dayCount The number of days
     * @return Formatted string like "Day 1", "Day 42", etc.
     */
    fun formatDayCount(dayCount: Int): String {
        return "Day $dayCount"
    }

    /**
     * Debug function to test day counter logic
     */
    fun debugDayCounter(): String {
        val fallbackDate = getFallbackStartDate()
        val daysSince = getDaysSince(fallbackDate)
        val today = Date()

        return "Fallback date: $fallbackDate, Today: $today, Days since: $daysSince"
    }
}
