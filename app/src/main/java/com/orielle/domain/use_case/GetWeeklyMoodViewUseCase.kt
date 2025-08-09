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
            if (userId == null) {
                flowOf(WeeklyMoodView(emptyList(), 0))
            } else {
                moodCheckInRepository.getRecentMoodCheckIns(userId, 7).map { response ->
                    when (response) {
                        is Response.Success -> generateWeeklyView(response.data)
                        is Response.Failure -> WeeklyMoodView(emptyList(), 0)
                        is Response.Loading -> WeeklyMoodView(emptyList(), 0)
                    }
                }
            }
        }
    }

    private fun generateWeeklyView(recentCheckIns: List<com.orielle.domain.model.MoodCheckIn>): WeeklyMoodView {
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
            }

            // Find mood check-in for this date
            val moodForDay = recentCheckIns.find { checkIn ->
                isSameDay(checkIn.timestamp, currentDate)
            }

            // Generate day label
            val dayLabel = when (i) {
                0 -> "M"
                1 -> "T"
                2 -> "W"
                3 -> "T"
                4 -> "F"
                5 -> "S"
                6 -> "S"
                else -> "?"
            }

            days.add(
                DayMoodData(
                    dayLabel = dayLabel,
                    date = currentDate,
                    moodCheckIn = moodForDay,
                    isToday = isToday
                )
            )
        }

        return WeeklyMoodView(days = days, todayIndex = todayIndex)
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}