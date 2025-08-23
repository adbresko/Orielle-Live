package com.orielle.domain.model

/**
 * Metadata for tracking cache freshness and managing data synchronization
 */
data class CacheMetadata(
    val lastUpdated: Long = System.currentTimeMillis(),
    val lastSynced: Long = System.currentTimeMillis(),
    val version: Long = 1L,
    val isStale: Boolean = false,
    val conflictResolution: ConflictResolution = ConflictResolution.NONE,
    val source: DataSource = DataSource.LOCAL
)

/**
 * How conflicts were resolved during sync
 */
enum class ConflictResolution {
    NONE,           // No conflicts detected
    LOCAL_WINS,     // Local data was newer
    CLOUD_WINS,     // Cloud data was newer
    MERGED,         // Data was intelligently merged
    NEEDS_USER_INPUT // User needs to decide
}

/**
 * Where the data originated from
 */
enum class DataSource {
    LOCAL,      // Local database
    CLOUD,      // Firebase/Firestore
    MERGED      // Combined from multiple sources
}

/**
 * Types of data for cache management
 */
enum class DataType {
    MOOD_CHECK_IN,
    JOURNAL_ENTRY,
    CHAT_MESSAGE,
    USER_PROFILE,
    TAG,
    CHAT_CONVERSATION
}

/**
 * User activity levels for cache TTL decisions
 */
enum class UserActivityLevel {
    ACTIVE,     // User actively using app
    INACTIVE,   // User not actively using app
    BACKGROUND  // App in background
}
