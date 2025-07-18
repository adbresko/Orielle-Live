package com.orielle.data.mapper

import com.orielle.data.local.model.MoodCheckInEntity
import com.orielle.domain.model.MoodCheckIn

/**
 * Mapper functions to convert between MoodCheckInEntity and MoodCheckIn domain model.
 */
fun MoodCheckInEntity.toMoodCheckIn(): MoodCheckIn {
    return MoodCheckIn(
        id = id,
        userId = userId,
        mood = mood,
        timestamp = timestamp,
        notes = notes
    )
}

fun MoodCheckIn.toMoodCheckInEntity(): MoodCheckInEntity {
    return MoodCheckInEntity(
        id = id,
        userId = userId,
        mood = mood,
        timestamp = timestamp,
        notes = notes
    )
}