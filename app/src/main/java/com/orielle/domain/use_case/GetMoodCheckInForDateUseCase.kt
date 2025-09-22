package com.orielle.domain.use_case

import com.orielle.domain.model.MoodCheckIn
import com.orielle.domain.model.Response
import com.orielle.domain.repository.MoodCheckInRepository
import java.util.Date
import javax.inject.Inject

/**
 * Use case for getting a mood check-in for a specific date.
 */
class GetMoodCheckInForDateUseCase @Inject constructor(
    private val moodCheckInRepository: MoodCheckInRepository
) {

    suspend operator fun invoke(userId: String, date: Date): Response<MoodCheckIn?> {
        return moodCheckInRepository.getMoodCheckInForDate(userId, date)
    }
}
