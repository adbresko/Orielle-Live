package com.orielle.util

import java.util.*

/**
 * Utility functions for date operations, specifically for calendar day logic.
 * Ensures proper handling of local timezone for daily check-in resets.
 */
object DateUtils {

    /**
     * Gets the current calendar day in the user's local timezone.
     * This ensures that the daily check-in resets at midnight local time,
     * not on a 24-hour timer from when the user last checked in.
     *
     * @return Date representing the current calendar day
     */
    fun getCurrentCalendarDay(): Date {
        val calendar = Calendar.getInstance()
        // Reset time to start of day (00:00:00) in local timezone
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * Checks if two dates are on the same calendar day in local timezone.
     *
     * @param date1 First date to compare
     * @param date2 Second date to compare
     * @return true if both dates are on the same calendar day
     */
    fun isSameCalendarDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Gets the start of the calendar day for a given date in local timezone.
     *
     * @param date The date to get the start of day for
     * @return Date representing the start of the calendar day (00:00:00)
     */
    fun getStartOfCalendarDay(date: Date): Date {
        val calendar = Calendar.getInstance().apply { time = date }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * Gets the end of the calendar day for a given date in local timezone.
     *
     * @param date The date to get the end of day for
     * @return Date representing the end of the calendar day (23:59:59.999)
     */
    fun getEndOfCalendarDay(date: Date): Date {
        val calendar = Calendar.getInstance().apply { time = date }
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }
}
