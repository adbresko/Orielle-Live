package com.orielle.ui.screens.mood

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.R
import com.orielle.data.local.model.QuoteEntity
import com.orielle.ui.theme.*
import com.orielle.ui.util.ScreenUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * ViewModel for the Share Screen.
 * Handles image generation and sharing functionality.
 */
@HiltViewModel
class ShareViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ShareUiState())
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    private var currentQuote: QuoteEntity? = null
    private var currentMood: String? = null

    fun setQuoteData(quote: QuoteEntity, mood: String) {
        currentQuote = quote
        currentMood = mood
    }

    fun shareQuote(context: Context) {
        val quote = currentQuote ?: return
        val mood = currentMood ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSharing = true, error = null)

            try {
                val bitmap = generateQuoteImage(quote, mood)
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

    private fun generateQuoteImage(quote: QuoteEntity, mood: String): Bitmap {
        // Create a high-resolution bitmap (1080x1080 for best social media compatibility)
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background color based on mood
        val backgroundColor = getMoodBackgroundColor(mood).toArgb()
        canvas.drawColor(backgroundColor)

        // Calculate dimensions
        val padding = 80
        val contentWidth = width - (padding * 2)

        // Paint for text
        val textPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Quote text
        val quoteText = "\"${quote.quote}\""
        val quoteTextSize = 36f
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
            canvas.drawText(line, width / 2f, currentY, textPaint)
            currentY += lineHeight
        }

        // Source text
        val sourceText = "— ${quote.source}"
        val sourceTextSize = 24f
        textPaint.apply {
            textSize = sourceTextSize
            color = Color.Black.toArgb()
            typeface = Typeface.DEFAULT
            alpha = 180 // 70% opacity
        }

        val sourceY = currentY + 60f
        canvas.drawText(sourceText, width / 2f, sourceY, textPaint)

        // Mood indicator
        val moodText = mood
        val moodTextSize = 20f
        textPaint.apply {
            textSize = moodTextSize
            color = getMoodBackgroundColor(mood).toArgb()
            typeface = Typeface.DEFAULT_BOLD
            alpha = 255
        }

        val moodY = sourceY + 80f
        canvas.drawText(moodText, width / 2f, moodY, textPaint)

        // Orielle branding
        val brandingText = "Orielle"
        val brandingTextSize = 18f
        textPaint.apply {
            textSize = brandingTextSize
            color = Color.Black.toArgb()
            typeface = Typeface.DEFAULT
            alpha = 150 // 60% opacity
        }

        val brandingY = height - 60f
        canvas.drawText(brandingText, width / 2f, brandingY, textPaint)

        return bitmap
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

    private fun getMoodBackgroundColor(mood: String): Color {
        return when (mood.lowercase()) {
            "happy" -> HappyBackground
            "sad" -> SadBackground
            "angry" -> AngryBackground
            "surprised" -> SurprisedBackground
            "playful" -> PlayfulBackground
            "shy" -> ShyBackground
            "frustrated" -> FrustratedBackground
            "scared" -> ScaredBackground
            "peaceful" -> PeacefulBackground
            else -> Color(0xFFE0E0E0) // Default color without MaterialTheme
        }
    }
}

/**
 * UI state for the Share Screen.
 */
data class ShareUiState(
    val isSharing: Boolean = false,
    val error: String? = null
)
