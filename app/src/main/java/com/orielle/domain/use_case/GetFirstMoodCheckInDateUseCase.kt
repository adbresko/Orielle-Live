package com.orielle.domain.use_case

import com.orielle.domain.model.AppError
import com.orielle.domain.repository.MoodCheckInRepository
import com.orielle.domain.model.Response
import kotlinx.coroutines.flow.first
import java.util.Date
import javax.inject.Inject

/**
 * Use case to get the date of the first mood check-in for a user.
 * This can be used to calculate how many days the user has been using the app.
 */
class GetFirstMoodCheckInDateUseCase @Inject constructor(
    private val repository: MoodCheckInRepository
) {
    /**
     * Gets the date of the first mood check-in for the given user.
     * @param userId The user ID to get the first check-in date for
     * @return Response containing the first check-in date, or null if no check-ins exist
     */
    suspend operator fun invoke(userId: String): Response<Date?> {
        return try {
            val moodCheckIns = repository.getMoodCheckInsByUserId(userId).first()
            when (moodCheckIns) {
                is Response.Success -> {
                    val firstCheckIn = moodCheckIns.data.minByOrNull { it.timestamp }
                    Response.Success(firstCheckIn?.timestamp)
                }
                is Response.Failure -> Response.Failure(moodCheckIns.error, moodCheckIns.exception)
                is Response.Loading -> Response.Loading
            }
        } catch (e: Exception) {
            Response.Failure(AppError.Unknown, e)
        }
    }
}
