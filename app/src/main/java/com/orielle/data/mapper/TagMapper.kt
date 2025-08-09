package com.orielle.data.mapper

import com.orielle.data.local.model.TagEntity
import com.orielle.domain.model.Tag

/**
 * Mapper functions to convert between Tag entities and domain models.
 */

fun TagEntity.toDomain(): Tag {
    return Tag(
        id = id,
        name = name,
        userId = userId,
        usageCount = usageCount,
        createdAt = createdAt,
        isUserCreated = isUserCreated,
        color = color,
        description = description
    )
}

fun Tag.toEntity(): TagEntity {
    return TagEntity(
        id = id,
        name = name,
        userId = userId,
        usageCount = usageCount,
        createdAt = createdAt,
        isUserCreated = isUserCreated,
        color = color,
        description = description
    )
}
