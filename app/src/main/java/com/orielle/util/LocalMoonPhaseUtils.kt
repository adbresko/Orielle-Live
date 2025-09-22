package com.orielle.util

import java.util.*

/**
 * Simple local moon phase calculation utility
 * Based on approximate lunar cycle calculations
 */
object LocalMoonPhaseUtils {

    private const val LUNAR_CYCLE_DAYS = 29.53059
    private val KNOWN_NEW_MOON = Calendar.getInstance().apply {
        set(2000, 0, 6, 18, 14, 0) // January 6, 2000 18:14 UTC
    }

    data class MoonPhaseInfo(
        val phase: String,
        val illumination: Double,
        val age: Int,
        val dayOfCycle: Int
    )

    fun getMoonPhase(date: Date = Date()): MoonPhaseInfo {
        val calendar = Calendar.getInstance().apply { time = date }
        val daysSinceKnownNewMoon = getDaysBetween(KNOWN_NEW_MOON.time, date)
        val lunarAge = (daysSinceKnownNewMoon % LUNAR_CYCLE_DAYS)
        val dayOfCycle = (lunarAge % LUNAR_CYCLE_DAYS).toInt()

        val illumination = when {
            lunarAge < LUNAR_CYCLE_DAYS / 4 -> {
                // Waxing crescent
                (lunarAge / (LUNAR_CYCLE_DAYS / 4)) * 100
            }
            lunarAge < LUNAR_CYCLE_DAYS / 2 -> {
                // First quarter to full
                100 - ((lunarAge - LUNAR_CYCLE_DAYS / 4) / (LUNAR_CYCLE_DAYS / 4)) * 100
            }
            lunarAge < 3 * LUNAR_CYCLE_DAYS / 4 -> {
                // Waning gibbous
                ((lunarAge - LUNAR_CYCLE_DAYS / 2) / (LUNAR_CYCLE_DAYS / 4)) * 100
            }
            else -> {
                // Last quarter to new
                100 - ((lunarAge - 3 * LUNAR_CYCLE_DAYS / 4) / (LUNAR_CYCLE_DAYS / 4)) * 100
            }
        }

        val phase = when {
            lunarAge < 1.84566 -> "new moon"
            lunarAge < 5.53699 -> "waxing crescent"
            lunarAge < 9.22831 -> "first quarter"
            lunarAge < 12.91963 -> "waxing gibbous"
            lunarAge < 16.61096 -> "full moon"
            lunarAge < 20.30228 -> "waning gibbous"
            lunarAge < 23.99361 -> "last quarter"
            else -> "waning crescent"
        }

        return MoonPhaseInfo(
            phase = phase,
            illumination = illumination.coerceIn(0.0, 100.0),
            age = lunarAge.toInt(),
            dayOfCycle = dayOfCycle
        )
    }

    private fun getDaysBetween(date1: Date, date2: Date): Double {
        val diffInMillis = date2.time - date1.time
        return diffInMillis / (1000.0 * 60.0 * 60.0 * 24.0)
    }

    fun getDaysSinceFirstMoodCheckIn(firstCheckInDate: Date?): Int {
        if (firstCheckInDate == null) return 0
        val today = Date()
        val diffInMillis = today.time - firstCheckInDate.time
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}
