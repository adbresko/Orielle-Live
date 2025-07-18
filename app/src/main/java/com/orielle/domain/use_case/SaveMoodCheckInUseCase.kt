package com.orielle.domain.use_case

import com.orielle.domain.model.MoodCheckIn
import com.orielle.domain.model.Response
import com.orielle.domain.repository.MoodCheckInRepository
import javax.inject.Inject

/**
 * Use case for saving a mood check-in.
 */
class SaveMoodCheckInUseCase @Inject constructor(
    private val moodCheckInRepository: MoodCheckInRepository
) {

    suspend operator fun invoke(moodCheckIn: MoodCheckIn): Response<Unit> {
        return moodCheckInRepository.saveMoodCheckIn(moodCheckIn)
    }
}