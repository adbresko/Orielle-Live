package com.orielle.util

import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Moon phase calculation utility.
 *
 * This object uses a verified astronomical reference point to accurately calculate
 * the moon's phase, illumination, and age for any given date.
 * It is fully compatible with Android API level 14 and higher.
 */
object LocalMoonPhaseUtils {

    // The average length of the synodic lunar cycle in days.
    private const val LUNAR_CYCLE_DAYS = 29.53058861

    // A known reference new moon: January 6, 2000, 18:14:00 UTC.
    // Using Calendar with UTC timezone and clearing it first for accuracy.
    private val KNOWN_NEW_MOON: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear() // Important: clear all fields before setting to ensure accuracy
        set(2000, Calendar.JANUARY, 6, 18, 14, 0)
    }

    /**
     * Data class to hold the complete information about the moon's phase.
     * The `phase` property holds a user-friendly display name.
     */
    data class MoonPhaseInfo(
        val phase: String,        // User-friendly name like "Waxing Crescent"
        val illumination: Double, // Illumination percentage (0.0 to 100.0)
        val age: Double,          // Age in days into the current cycle
        val dayOfCycle: Int       // Integer day of the cycle (0-29)
    )

    /**
     * Internal enum to represent the 8 major moon phases for calculation purposes.
     */
    private enum class Phase(val displayName: String) {
        NEW_MOON("New Moon"),
        WAXING_CRESCENT("Waxing Crescent"),
        FIRST_QUARTER("First Quarter"),
        WAXING_GIBBOUS("Waxing Gibbous"),
        FULL_MOON("Full Moon"),
        WANING_GIBBOUS("Waning Gibbous"),
        LAST_QUARTER("Last Quarter"),
        WANING_CRESCENT("Waning Crescent")
    }

    /**
     * Calculates the moon phase for a given date.
     * @param date The date for which to calculate the moon phase. Defaults to the current date and time.
     * @return A [MoonPhaseInfo] object containing the detailed phase information.
     */
    @JvmOverloads // Allows calling from Java with default parameter
    fun getMoonPhase(date: Date = Date()): MoonPhaseInfo {
        // Calculate the total number of days that have passed since the reference new moon.
        val daysSinceKnownNewMoon = getDaysBetween(KNOWN_NEW_MOON.time, date)

        // The lunar age is the remainder of the total days divided by the lunar cycle length.
        val lunarAge = daysSinceKnownNewMoon % LUNAR_CYCLE_DAYS
        // Ensure the age is positive for dates before the 2000 reference point.
        val currentLunarAge = if (lunarAge < 0) lunarAge + LUNAR_CYCLE_DAYS else lunarAge

        // Calculate illumination percentage (0-100) using a cosine function to model the light change.
        val phaseFraction = currentLunarAge / LUNAR_CYCLE_DAYS
        val illumination = (1 - Math.cos(2 * Math.PI * phaseFraction)) / 2.0 * 100.0

        val phaseEnum = getPhaseFromAge(currentLunarAge)

        return MoonPhaseInfo(
            phase = phaseEnum.displayName, // Return the user-friendly display name directly
            illumination = illumination,
            age = currentLunarAge,
            dayOfCycle = currentLunarAge.toInt()
        )
    }

    /**
     * Determines the correct phase enum based on the moon's age in days.
     */
    private fun getPhaseFromAge(age: Double): Phase {
        return when {
            // The boundaries are set around the exact moments of each phase.
            age < 1.84566 -> Phase.NEW_MOON
            age < 5.53699 -> Phase.WAXING_CRESCENT
            age < 9.22831 -> Phase.FIRST_QUARTER
            age < 12.91963 -> Phase.WAXING_GIBBOUS
            age < 16.61096 -> Phase.FULL_MOON
            age < 20.30228 -> Phase.WANING_GIBBOUS
            age < 23.99361 -> Phase.LAST_QUARTER
            // Anything older than the last quarter is a waning crescent until the cycle resets.
            else -> Phase.WANING_CRESCENT
        }
    }

    /**
     * Helper function to calculate days between two dates.
     */
    private fun getDaysBetween(date1: Date, date2: Date): Double {
        val diffInMillis = date2.time - date1.time
        return diffInMillis / (1000.0 * 60.0 * 60.0 * 24.0)
    }

    /**
     * This function seems unrelated to moon phases but was part of the original class.
     * It is kept here for compatibility with other parts of your app.
     */
    fun getDaysSinceFirstMoodCheckIn(firstCheckInDate: Date?): Int {
        if (firstCheckInDate == null) return 0
        val today = Date()
        val diffInMillis = today.time - firstCheckInDate.time
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}

