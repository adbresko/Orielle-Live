package com.orielle.domain.use_case

import com.orielle.domain.manager.SessionManager
import com.orielle.domain.model.DayMoodData
import com.orielle.domain.model.Response
import com.orielle.domain.model.WeeklyMoodView
import com.orielle.domain.repository.MoodCheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject

class GetWeeklyMoodViewUseCase @Inject constructor(
    private val moodCheckInRepository: MoodCheckInRepository,
    private val sessionManager: SessionManager
) {
    operator fun invoke(): Flow<WeeklyMoodView> {
        return sessionManager.currentUserId.flatMapLatest { userId ->
            println("GetWeeklyMoodViewUseCase: Current user ID: $userId")
            if (userId == null) {
                println("GetWeeklyMoodViewUseCase: No user ID, generating empty week structure")
                flowOf(generateWeeklyView(emptyList())) // Generate empty week structure for guests
            } else {
                println("GetWeeklyMoodViewUseCase: Fetching mood check-ins for user: $userId")
                moodCheckInRepository.getRecentMoodCheckIns(userId, 7).map { response ->
                    when (response) {
                        is Response.Success -> {
                            println("GetWeeklyMoodViewUseCase: Got ${response.data.size} mood check-ins")
                            val weeklyView = generateWeeklyView(response.data)
                            println("GetWeeklyMoodViewUseCase: Generated weekly view with ${weeklyView.days.size} days")
                            weeklyView
                        }
                        is Response.Failure -> {
                            println("GetWeeklyMoodViewUseCase: Failed to get mood data: ${response.exception}")
                            generateWeeklyView(emptyList()) // Generate empty week view
                        }
                        is Response.Loading -> {
                            println("GetWeeklyMoodViewUseCase: Loading mood data")
                            generateWeeklyView(emptyList()) // Show empty structure while loading
                        }
                    }
                }
            }
        }
    }

    private fun generateWeeklyView(recentCheckIns: List<com.orielle.domain.model.MoodCheckIn>): WeeklyMoodView {
        println("GetWeeklyMoodViewUseCase: generateWeeklyView called with ${recentCheckIns.size} check-ins")

        val calendar = Calendar.getInstance()
        val today = calendar.time

        // Get Monday of current week as start
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startOfWeek = calendar.time

        val days = mutableListOf<DayMoodData>()
        var todayIndex = 0

        // Generate 7 days starting from Monday
        for (i in 0..6) {
            calendar.time = startOfWeek
            calendar.add(Calendar.DAY_OF_YEAR, i)
            val currentDate = calendar.time

            // Check if this is today
            val isToday = isSameDay(currentDate, today)
            if (isToday) {
                todayIndex = i
                println("GetWeeklyMoodViewUseCase: Today is day $i (${getDayLabel(i)})")
            }

            // Find mood check-in for this date
            val moodForDay = recentCheckIns.find { checkIn ->
                isSameDay(checkIn.timestamp, currentDate)
            }

            // Generate day label
            val dayLabel = getDayLabel(i)

            days.add(
                DayMoodData(
                    dayLabel = dayLabel,
                    date = currentDate,
                    moodCheckIn = moodForDay,
                    isToday = isToday
                )
            )

            println("GetWeeklyMoodViewUseCase: Added day $i: $dayLabel, isToday: $isToday")
        }

        val result = WeeklyMoodView(days = days, todayIndex = todayIndex)
        println("GetWeeklyMoodViewUseCase: Created WeeklyMoodView with ${result.days.size} days, todayIndex: $todayIndex")
        return result
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun getDayLabel(dayIndex: Int): String {
        return when (dayIndex) {
            0 -> "M"  // Monday
            1 -> "T"  // Tuesday
            2 -> "W"  // Wednesday
            3 -> "T"  // Thursday
            4 -> "F"  // Friday
            5 -> "S"  // Saturday
            6 -> "S"  // Sunday
            else -> "?"
        }
    }
}