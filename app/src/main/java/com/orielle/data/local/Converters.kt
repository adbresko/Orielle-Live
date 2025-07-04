package com.orielle.data.local

import androidx.room.TypeConverter
import java.util.Date

/**
 * A utility class to help Room convert complex types (like Date)
 * into a format it can store in the database.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
