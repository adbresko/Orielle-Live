package com.orielle.data.mapper

import com.orielle.data.local.model.MoodCheckInEntity
import com.orielle.domain.model.MoodCheckIn
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mapper functions to convert between MoodCheckInEntity and MoodCheckIn domain model.
 */
fun MoodCheckInEntity.toMoodCheckIn(): MoodCheckIn {
    return MoodCheckIn(
        id = id,
        userId = userId,
        mood = mood,
        tags = tags,
        timestamp = timestamp,
        notes = notes
    )
}

fun MoodCheckIn.toMoodCheckInEntity(): MoodCheckInEntity {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dateKey = dateFormat.format(timestamp)

    return MoodCheckInEntity(
        id = id,
        userId = userId,
        mood = mood,
        tags = tags,
        timestamp = timestamp,
        notes = notes,
        dateKey = dateKey
    )
}