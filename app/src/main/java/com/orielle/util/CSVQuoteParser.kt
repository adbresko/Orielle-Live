package com.orielle.util

import android.content.Context
import com.orielle.data.local.model.QuoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Utility class for parsing CSV files containing quotes.
 * Handles loading quotes from assets and converting them to QuoteEntity objects.
 */
object CSVQuoteParser {

    /**
     * Parses the quotes CSV file from assets and returns a list of QuoteEntity objects.
     *
     * @param context The application context to access assets
     * @param fileName The name of the CSV file in assets (default: "quotes.csv")
     * @return List of QuoteEntity objects parsed from the CSV
     */
    suspend fun parseQuotesFromAssets(
        context: Context,
        fileName: String = "quotes.csv"
    ): List<QuoteEntity> = withContext(Dispatchers.IO) {
        val quotes = mutableListOf<QuoteEntity>()

        try {
            // Debug: List all available assets
            val assetFiles = context.assets.list("")
            android.util.Log.d("CSVQuoteParser", "Available assets: ${assetFiles?.joinToString(", ")}")
            android.util.Log.d("CSVQuoteParser", "Looking for file: $fileName")

            // Try to find the file with case-insensitive search
            val actualFileName = assetFiles?.find {
                it.equals(fileName, ignoreCase = true)
            } ?: fileName

            android.util.Log.d("CSVQuoteParser", "Using file: $actualFileName")

            val inputStream = context.assets.open(actualFileName)
            val reader = BufferedReader(InputStreamReader(inputStream))

            // Skip header line
            reader.readLine()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let { csvLine ->
                    if (csvLine.isNotBlank()) {
                        val quote = parseCSVLine(csvLine)
                        quote?.let { quotes.add(it) }
                    }
                }
            }

            reader.close()
            inputStream.close()
        } catch (e: Exception) {
            // Log error or handle gracefully
            e.printStackTrace()
        }

        quotes
    }

    /**
     * Parses a single CSV line into a QuoteEntity.
     * Expected format: quote_id,quote,source,mood
     *
     * @param csvLine The CSV line to parse
     * @return QuoteEntity if parsing is successful, null otherwise
     */
    private fun parseCSVLine(csvLine: String): QuoteEntity? {
        return try {
            // Split by comma, but handle quoted fields that might contain commas
            val fields = parseCSVFields(csvLine)

            if (fields.size >= 4) {
                QuoteEntity(
                    id = fields[0].trim(),
                    quote = fields[1].trim(),
                    source = fields[2].trim(),
                    mood = fields[3].trim()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Parses CSV fields, handling quoted fields that might contain commas.
     *
     * @param csvLine The CSV line to parse
     * @return List of field values
     */
    private fun parseCSVFields(csvLine: String): List<String> {
        val fields = mutableListOf<String>()
        var currentField = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < csvLine.length) {
            val char = csvLine[i]

            when {
                char == '"' -> {
                    if (inQuotes && i + 1 < csvLine.length && csvLine[i + 1] == '"') {
                        // Escaped quote
                        currentField.append('"')
                        i++ // Skip next quote
                    } else {
                        // Toggle quote state
                        inQuotes = !inQuotes
                    }
                }
                char == ',' && !inQuotes -> {
                    // Field separator
                    fields.add(currentField.toString())
                    currentField = StringBuilder()
                }
                else -> {
                    currentField.append(char)
                }
            }
            i++
        }

        // Add the last field
        fields.add(currentField.toString())

        return fields
    }
}
