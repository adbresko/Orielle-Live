package com.orielle.domain.model

import java.util.Date

/**
 * Represents a daily journal prompt.
 *
 * @param id Unique identifier for the prompt.
 * @param text The prompt text to display to users.
 * @param category Optional category for the prompt (e.g., "growth", "gratitude", "reflection").
 * @param isActive Whether this prompt is currently available for use.
 * @param createdAt When this prompt was created.
 */
data class JournalPrompt(
    val id: String,
    val text: String,
    val category: String? = null,
    val isActive: Boolean = true,
    val createdAt: Date = Date()
)

/**
 * Predefined journal prompts for V1 implementation.
 */
object DefaultJournalPrompts {
    val prompts = listOf(
        JournalPrompt(
            id = "prompt_001",
            text = "How can you grow from this experience? What can you learn from this matter?",
            category = "growth"
        ),
        JournalPrompt(
            id = "prompt_002",
            text = "What moment today made you feel most like yourself?",
            category = "reflection"
        ),
        JournalPrompt(
            id = "prompt_003",
            text = "What are you grateful for in this very moment?",
            category = "gratitude"
        ),
        JournalPrompt(
            id = "prompt_004",
            text = "If your future self could send you a message today, what would it say?",
            category = "growth"
        ),
        JournalPrompt(
            id = "prompt_005",
            text = "What small act of kindness could you offer someone today?",
            category = "compassion"
        ),
        JournalPrompt(
            id = "prompt_006",
            text = "What emotion are you carrying today, and what does it need from you?",
            category = "emotion"
        ),
        JournalPrompt(
            id = "prompt_007",
            text = "What would you do if you knew you couldn't fail?",
            category = "dreams"
        ),
        JournalPrompt(
            id = "prompt_008",
            text = "How has a recent challenge helped you discover your strength?",
            category = "resilience"
        ),
        JournalPrompt(
            id = "prompt_009",
            text = "What does peace feel like to you right now?",
            category = "mindfulness"
        ),
        JournalPrompt(
            id = "prompt_010",
            text = "What story are you telling yourself today, and is it serving you?",
            category = "self-awareness"
        )
    )

    /**
     * Gets today's prompt based on the current date.
     * This creates a consistent daily rotation.
     */
    fun getTodaysPrompt(): JournalPrompt {
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        return prompts[dayOfYear % prompts.size]
    }
}
