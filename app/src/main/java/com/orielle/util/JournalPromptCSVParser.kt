package com.orielle.util

import android.content.Context
import com.orielle.data.local.model.JournalPromptEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object JournalPromptCsvParser {

    suspend fun parseJournalPromptsCsv(context: Context): List<JournalPromptEntity> = withContext(Dispatchers.IO) {
        val prompts = mutableListOf<JournalPromptEntity>()

        try {
            val inputStream = context.assets.open("journal_prompt.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))

            // Skip header line
            reader.readLine()

            var line: String? = reader.readLine()
            while (line != null) {
                try {
                    // Standard CSV format: Mood,Prompt
                    val parts = line.split(",", limit = 2)

                    if (parts.size >= 2) {
                        val moodCategory = parts[0].trim()
                        val promptText = parts[1].trim()

                        if (moodCategory.isNotEmpty() && promptText.isNotEmpty()) {
                            prompts.add(
                                JournalPromptEntity(
                                    moodCategory = moodCategory,
                                    promptText = promptText
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Skip malformed lines
                    android.util.Log.w("JournalPromptCsvParser", "Skipping malformed line: $line", e)
                }
                line = reader.readLine()
            }

            reader.close()
            inputStream.close()

        } catch (e: Exception) {
            android.util.Log.e("JournalPromptCsvParser", "Error parsing CSV file", e)
        }

        android.util.Log.d("JournalPromptCsvParser", "Parsed ${prompts.size} journal prompts from CSV")
        return@withContext prompts
    }

}
