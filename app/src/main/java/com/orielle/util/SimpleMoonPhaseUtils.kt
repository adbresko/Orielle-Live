package com.orielle.util

import java.util.*

/**
 * Simple moon phase utility that doesn't require network calls.
 * Uses local astronomical calculations for moon phase determination.
 */
object SimpleMoonPhaseUtils {

    /**
     * Moon phase types with their visual representations
     */
    enum class MoonPhase(
        val displayName: String,
        val emoji: String,
        val description: String
    ) {
        NEW_MOON("New Moon", "🌑", "New Moon"),
        WAXING_CRESCENT("Waxing Crescent", "🌒", "Waxing Crescent"),
        FIRST_QUARTER("First Quarter", "🌓", "First Quarter"),
        WAXING_GIBBOUS("Waxing Gibbous", "🌔", "Waxing Gibbous"),
        FULL_MOON("Full Moon", "🌕", "Full Moon"),
        WANING_GIBBOUS("Waning Gibbous", "🌖", "Waning Gibbous"),
        LAST_QUARTER("Last Quarter", "🌗", "Last Quarter"),
        WANING_CRESCENT("Waning Crescent", "🌘", "Waning Crescent")
    }

    /**
     * Calculates the current moon phase for a given date using local calculations.
     *
     * @param date The date to calculate moon phase for
     * @return MoonPhase enum representing the current phase
     */
    fun getMoonPhase(date: Date = Date()): MoonPhase {
        val calendar = Calendar.getInstance().apply { time = date }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Calculate days since a known new moon (January 6, 2000)
        val daysSinceEpoch = getDaysSinceEpoch(year, month, day)
        val knownNewMoon = 6.0 // January 6, 2000
        val lunarCycle = 29.53059 // Average lunar cycle in days
        val daysSinceNewMoon = (daysSinceEpoch - knownNewMoon) % lunarCycle

        return when {
            daysSinceNewMoon < 1.845 -> MoonPhase.NEW_MOON
            daysSinceNewMoon < 5.535 -> MoonPhase.WAXING_CRESCENT
            daysSinceNewMoon < 9.225 -> MoonPhase.FIRST_QUARTER
            daysSinceNewMoon < 12.915 -> MoonPhase.WAXING_GIBBOUS
            daysSinceNewMoon < 16.605 -> MoonPhase.FULL_MOON
            daysSinceNewMoon < 20.295 -> MoonPhase.WANING_GIBBOUS
            daysSinceNewMoon < 23.985 -> MoonPhase.LAST_QUARTER
            else -> MoonPhase.WANING_CRESCENT
        }
    }

    /**
     * Gets the moon phase data for display in the date line.
     *
     * @param date The date to get moon phase for
     * @return Formatted string with moon phase emoji and name
     */
    fun getMoonPhaseDisplay(date: Date = Date()): String {
        val moonPhase = getMoonPhase(date)
        return "${moonPhase.emoji} ${moonPhase.displayName}"
    }

    /**
     * Calculates days since epoch (January 1, 2000) for moon phase calculations.
     */
    private fun getDaysSinceEpoch(year: Int, month: Int, day: Int): Double {
        val epochYear = 2000
        val epochMonth = 1
        val epochDay = 1

        var days = 0.0

        // Add years
        for (y in epochYear until year) {
            days += if (isLeapYear(y)) 366.0 else 365.0
        }

        // Add months
        val monthDays = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        for (m in epochMonth until month) {
            days += monthDays[m - 1].toDouble()
            if (m == 2 && isLeapYear(year)) days += 1.0
        }

        // Add days
        days += (day - epochDay).toDouble()

        return days
    }

    /**
     * Checks if a year is a leap year.
     */
    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }
}
