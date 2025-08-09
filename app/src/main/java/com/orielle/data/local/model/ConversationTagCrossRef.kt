package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Cross-reference table for many-to-many relationship between conversations and tags.
 * A conversation can have multiple tags, and a tag can be used in multiple conversations.
 */
@Entity(
    tableName = "conversation_tag_cross_ref",
    primaryKeys = ["conversationId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = ChatConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["tagId"])
    ]
)
data class ConversationTagCrossRef(
    val conversationId: String,
    val tagId: String
)
