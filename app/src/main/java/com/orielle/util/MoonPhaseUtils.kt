package com.orielle.util

import java.util.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL
import java.text.SimpleDateFormat

/**
 * Utility class for fetching moon phases from Visual Crossing Weather API.
 * Uses real-time API data with local calculations as fallback.
 */
object MoonPhaseUtils {

    private const val VISUAL_CROSSING_API_BASE_URL = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline"
    private const val API_KEY = "XVDC8VU48LGVWBEPQJHT2T4WB" // Your Visual Crossing API key
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class MoonPhaseData(
        val date: String,
        val phase: String,
        val illumination: Double,
        val age: Double
    )

    @Serializable
    data class VisualCrossingResponse(
        val days: List<DayData>
    )

    @Serializable
    data class DayData(
        val datetime: String,
        val moonphase: Double? = null
    )

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

    // Cache for moon phase data to avoid repeated API calls
    private val moonPhaseCache = mutableMapOf<String, MoonPhaseData>()

    /**
     * Fetches moon phase data from the API for a given date.
     *
     * @param date The date to get moon phase for
     * @return MoonPhaseData or null if API call fails
     */
    suspend fun fetchMoonPhaseData(date: Date = Date()): MoonPhaseData? {
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)

        // Check cache first
        moonPhaseCache[dateString]?.let { return it }

        // Skip API call if no key is provided
        if (API_KEY == "YOUR_API_KEY") {
            println("🌙 No API key provided, using local calculation")
            return null
        }

        return try {
            // Use a default location (you can make this configurable)
            val location = "New York,NY,US"
            val url = "$VISUAL_CROSSING_API_BASE_URL/$location/$dateString?unitGroup=us&key=$API_KEY&include=days&elements=datetime,moonphase"

            println("🌙 Fetching moon phase from Visual Crossing API...")
            val response = URL(url).readText()
            val apiResponse = json.decodeFromString<VisualCrossingResponse>(response)

            val dayData = apiResponse.days.firstOrNull()
            if (dayData?.moonphase != null) {
                val moonPhase = convertMoonPhaseNumber(dayData.moonphase)
                val illumination = calculateIllumination(dayData.moonphase)

                val moonPhaseData = MoonPhaseData(
                    date = dayData.datetime,
                    phase = moonPhase.displayName,
                    illumination = illumination,
                    age = dayData.moonphase * 29.53059 // Convert to days
                )

                // Cache the result
                moonPhaseCache[dateString] = moonPhaseData
                println("🌙 Visual Crossing API Success: ${moonPhase.displayName} (${(illumination * 100).toInt()}%)")
                moonPhaseData
            } else {
                println("🌙 No moon phase data in API response")
                null
            }

        } catch (e: Exception) {
            println("🌙 Visual Crossing API Error: ${e.message}")
            println("🌙 Falling back to local calculation")
            null
        }
    }

    /**
     * Gets the moon phase for a given date, using API if available or fallback calculation.
     *
     * @param date The date to get moon phase for
     * @return MoonPhase enum representing the current phase
     */
    suspend fun getMoonPhase(date: Date = Date()): MoonPhase {
        val apiData = fetchMoonPhaseData(date)

        return if (apiData != null) {
            // Use API data to determine phase
            when {
                apiData.phase.contains("New", ignoreCase = true) -> MoonPhase.NEW_MOON
                apiData.phase.contains("Waxing Crescent", ignoreCase = true) -> MoonPhase.WAXING_CRESCENT
                apiData.phase.contains("First Quarter", ignoreCase = true) -> MoonPhase.FIRST_QUARTER
                apiData.phase.contains("Waxing Gibbous", ignoreCase = true) -> MoonPhase.WAXING_GIBBOUS
                apiData.phase.contains("Full", ignoreCase = true) -> MoonPhase.FULL_MOON
                apiData.phase.contains("Waning Gibbous", ignoreCase = true) -> MoonPhase.WANING_GIBBOUS
                apiData.phase.contains("Last Quarter", ignoreCase = true) -> MoonPhase.LAST_QUARTER
                apiData.phase.contains("Waning Crescent", ignoreCase = true) -> MoonPhase.WANING_CRESCENT
                else -> getFallbackMoonPhase(date)
            }
        } else {
            // Fallback to local calculation
            getFallbackMoonPhase(date)
        }
    }

    /**
     * Fallback moon phase calculation using local astronomical calculations.
     */
    fun getFallbackMoonPhase(date: Date): MoonPhase {
        val calendar = Calendar.getInstance().apply { time = date }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val lunarCycle = 29.53
        val knownNewMoon = 6.0
        val daysSinceEpoch = getDaysSinceEpoch(year, month, day)
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
    suspend fun getMoonPhaseDisplay(date: Date = Date()): String {
        val moonPhase = getMoonPhase(date)
        // Always use local calculation for reliability
        return "${moonPhase.emoji} ${moonPhase.displayName}"
    }

    /**
     * Converts Visual Crossing moon phase number (0-1) to our MoonPhase enum.
     * Based on Visual Crossing documentation:
     * 0 = new moon, 0.25 = first quarter, 0.5 = full moon, 0.75 = last quarter
     */
    private fun convertMoonPhaseNumber(phaseNumber: Double): MoonPhase {
        return when {
            phaseNumber == 0.0 -> MoonPhase.NEW_MOON
            phaseNumber in 0.0..0.25 -> MoonPhase.WAXING_CRESCENT
            phaseNumber == 0.25 -> MoonPhase.FIRST_QUARTER
            phaseNumber in 0.25..0.5 -> MoonPhase.WAXING_GIBBOUS
            phaseNumber == 0.5 -> MoonPhase.FULL_MOON
            phaseNumber in 0.5..0.75 -> MoonPhase.WANING_GIBBOUS
            phaseNumber == 0.75 -> MoonPhase.LAST_QUARTER
            else -> MoonPhase.WANING_CRESCENT
        }
    }

    /**
     * Calculates illumination percentage from moon phase number.
     * 0 = 0%, 0.5 = 100%, 1 = 0%
     */
    private fun calculateIllumination(phaseNumber: Double): Double {
        return if (phaseNumber <= 0.5) {
            phaseNumber * 2.0 // 0 to 0.5 maps to 0% to 100%
        } else {
            (1.0 - phaseNumber) * 2.0 // 0.5 to 1 maps to 100% to 0%
        }
    }

    /**
     * Test function to verify API connectivity and show current moon phase data.
     * Call this from your app to debug API status.
     */
    suspend fun testApiConnection(): String {
        return try {
            val today = Date()
            val apiData = fetchMoonPhaseData(today)
            if (apiData != null) {
                "✅ API Working: ${apiData.phase} (${(apiData.illumination * 100).toInt()}%)"
            } else {
                "❌ API Failed: Using fallback calculation"
            }
        } catch (e: Exception) {
            "❌ API Error: ${e.message}"
        }
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
