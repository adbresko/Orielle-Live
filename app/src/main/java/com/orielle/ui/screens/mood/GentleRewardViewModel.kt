package com.orielle.ui.screens.mood

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.data.local.model.QuoteEntity
import com.orielle.data.manager.DailyContentManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * ViewModel for the Gentle Reward Screen.
 * Handles quote loading and state management.
 */
@HiltViewModel
class GentleRewardViewModel @Inject constructor(
    private val dailyContentManager: DailyContentManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GentleRewardUiState())
    val uiState: StateFlow<GentleRewardUiState> = _uiState.asStateFlow()

    // Cache the current quote to avoid re-fetching during the same session
    private var currentQuote: QuoteEntity? = null
    private var currentMood: String? = null

    fun loadQuoteForMood(mood: String) {
        viewModelScope.launch {
            android.util.Log.d("GentleRewardViewModel", "Loading quote for mood: $mood")

            // Check if we already have a quote for this mood in the current session
            if (currentQuote != null && currentMood == mood) {
                android.util.Log.d("GentleRewardViewModel", "Using cached quote for mood: $mood")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    quote = currentQuote,
                    error = null
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Get today's quote for this mood (will be the same all day)
                val quote = dailyContentManager.getTodaysQuote(mood)
                android.util.Log.d("GentleRewardViewModel", "Retrieved quote for today: ${quote?.id ?: "null"}")

                if (quote != null) {
                    // Cache the quote for this session
                    currentQuote = quote
                    currentMood = mood

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        quote = quote,
                        error = null
                    )
                } else {
                    android.util.Log.w("GentleRewardViewModel", "No quote found for mood: $mood")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        quote = null,
                        error = "No quotes available for $mood mood"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("GentleRewardViewModel", "Error loading quote for mood: $mood", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    quote = null,
                    error = e.message ?: "Failed to load quote"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun shareQuote(context: Context) {
        val quote = currentQuote ?: return
        val mood = currentMood ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSharing = true, error = null)

            try {
                val bitmap = generateQuoteImage(quote)
                val imageFile = saveBitmapToFile(bitmap, context)

                if (imageFile != null) {
                    shareImage(context, imageFile)
                    _uiState.value = _uiState.value.copy(isSharing = false, error = null)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSharing = false,
                        error = "Failed to save image"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSharing = false,
                    error = e.message ?: "Failed to share quote"
                )
            }
        }
    }

    private fun generateQuoteImage(quote: QuoteEntity): Bitmap {
        // Create a high-resolution bitmap (1080x1080 for best social media compatibility)
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Parchment/aged paper background like the card design
        val backgroundColor = Color(0xFFF5F5DC).toArgb()
        canvas.drawColor(backgroundColor)

        // Draw glowing orb effect behind the text
        drawGlowEffect(canvas, width, height)

        // Calculate dimensions
        val padding = 120
        val contentWidth = width - (padding * 2)

        // Paint for text
        val textPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.LEFT // Left-aligned text
        }

        // Quote text (no quotes around it, just the text)
        val quoteText = quote.quote
        val quoteTextSize = 42f
        textPaint.apply {
            textSize = quoteTextSize
            color = Color.Black.toArgb()
            typeface = Typeface.DEFAULT_BOLD
        }

        // Measure and draw quote text with line breaks
        val quoteLines = breakTextIntoLines(quoteText, textPaint, contentWidth)
        val lineHeight = quoteTextSize * 1.4f
        val totalQuoteHeight = quoteLines.size * lineHeight

        var currentY = (height - totalQuoteHeight) / 2f + quoteTextSize

        for (line in quoteLines) {
            canvas.drawText(line, padding.toFloat(), currentY, textPaint)
            currentY += lineHeight
        }

        // Source text (only if author exists)
        if (quote.source.isNotBlank()) {
            val sourceText = quote.source
            val sourceTextSize = 28f
            textPaint.apply {
                textSize = sourceTextSize
                color = Color.Black.toArgb()
                typeface = Typeface.DEFAULT
                alpha = 180 // 70% opacity
            }

            val sourceY = currentY + 80f
            canvas.drawText(sourceText, padding.toFloat(), sourceY, textPaint)
        }

        return bitmap
    }

    private fun drawGlowEffect(canvas: Canvas, width: Int, height: Int) {
        // Create a radial gradient paint for the glow effect
        val glowPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            shader = android.graphics.RadialGradient(
                width / 2f, height / 2f, // center
                width * 0.4f, // radius
                intArrayOf(
                    Color(0xFF87CEEB).copy(alpha = 0.4f).toArgb(), // Light blue center
                    Color(0xFF87CEEB).copy(alpha = 0.2f).toArgb(), // Medium blue
                    Color(0xFF87CEEB).copy(alpha = 0.1f).toArgb(), // Light blue edge
                    Color.Transparent.toArgb() // Transparent outer edge
                ),
                floatArrayOf(0f, 0.3f, 0.7f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }

        // Draw the glow effect
        canvas.drawCircle(width / 2f, height / 2f, width * 0.4f, glowPaint)
    }

    private fun breakTextIntoLines(text: String, paint: Paint, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)

            if (width <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                    currentLine = word
                } else {
                    // Single word is too long, add it anyway
                    lines.add(word)
                }
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }

    private fun saveBitmapToFile(bitmap: Bitmap, context: Context): File? {
        return try {
            val cacheDir = context.cacheDir
            val imageFile = File(cacheDir, "quote_share_${System.currentTimeMillis()}.png")

            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            imageFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun shareImage(context: Context, imageFile: File) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share your insight")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
}

/**
 * UI state for the Gentle Reward Screen.
 */
data class GentleRewardUiState(
    val isLoading: Boolean = false,
    val quote: QuoteEntity? = null,
    val error: String? = null,
    val isSharing: Boolean = false
)
