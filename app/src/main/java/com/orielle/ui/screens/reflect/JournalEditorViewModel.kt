package com.orielle.ui.screens.reflect

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.model.JournalEntryType
import com.orielle.domain.use_case.GetJournalEntriesUseCase
import com.orielle.domain.use_case.SaveJournalEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class JournalEditorViewModel @Inject constructor(
    private val saveJournalEntryUseCase: SaveJournalEntryUseCase,
    private val getJournalEntriesUseCase: GetJournalEntriesUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalEditorUiState())
    val uiState: StateFlow<JournalEditorUiState> = _uiState.asStateFlow()

    init {
        generateTempEntryId()
    }

    private fun generateTempEntryId() {
        _uiState.value = _uiState.value.copy(
            tempEntryId = "temp_${UUID.randomUUID()}"
        )
    }

    fun loadEntry(entryId: String) {
        viewModelScope.launch {
            getJournalEntriesUseCase().first().find { it.id == entryId }?.let { entry ->
                _uiState.value = _uiState.value.copy(
                    entryId = entry.id,
                    title = entry.title ?: "",
                    content = entry.content,
                    timestamp = entry.timestamp,
                    location = entry.location,
                    tags = entry.tags,
                    photoUrl = entry.photoUrl,
                    promptText = entry.promptText,
                    entryType = entry.entryType,
                    isEditMode = true
                )
            }
        }
    }

    fun setPromptText(prompt: String) {
        _uiState.value = _uiState.value.copy(
            promptText = prompt,
            entryType = JournalEntryType.PROMPT
        )
    }

    fun setQuickEntry(isQuickEntry: Boolean) {
        if (isQuickEntry) {
            _uiState.value = _uiState.value.copy(
                entryType = JournalEntryType.QUICK_ENTRY
            )
        }
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(
            content = content,
            hasUnsavedChanges = true
        )
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            hasUnsavedChanges = true
        )
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            lastKnownLocation?.let { location ->
                // In a real app, you would use a geocoding service to convert coordinates to a readable address
                // For now, we'll use a placeholder
                val locationString = "Current Location" // TODO: Implement geocoding
                _uiState.value = _uiState.value.copy(location = locationString)
            }
        } catch (e: Exception) {
            // Handle location error silently
        }
    }

    fun saveEntry(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                println("JournalEditorViewModel: Starting to save entry")
                val userId = sessionManager.currentUserId.first()
                if (userId == null) {
                    println("JournalEditorViewModel: No user ID found!")
                    showMessage("Error: No user session found")
                    return@launch
                }

                println("JournalEditorViewModel: Saving for user: $userId")

                val entry = JournalEntry(
                    id = _uiState.value.entryId.ifEmpty { UUID.randomUUID().toString() },
                    userId = userId,
                    title = _uiState.value.title.takeIf { it.isNotBlank() },
                    content = _uiState.value.content,
                    timestamp = if (_uiState.value.isEditMode) _uiState.value.timestamp else Date(),
                    location = _uiState.value.location,
                    tags = _uiState.value.tags,
                    photoUrl = _uiState.value.photoUrl,
                    promptText = _uiState.value.promptText,
                    entryType = _uiState.value.entryType
                )

                println("JournalEditorViewModel: Entry created with ID: ${entry.id}")

                val result = saveJournalEntryUseCase(entry)

                when (result) {
                    is com.orielle.domain.model.Response.Success -> {
                        println("JournalEditorViewModel: Entry saved successfully")
                        _uiState.value = _uiState.value.copy(
                            hasUnsavedChanges = false,
                            showSavedMessage = true
                        )

                        kotlinx.coroutines.delay(500) // Brief delay to show the saved message
                        onSuccess()
                    }
                    is com.orielle.domain.model.Response.Failure -> {
                        println("JournalEditorViewModel: Save failed: ${result.exception}")
                        showMessage("Failed to save entry: ${result.exception?.message ?: "Unknown error"}")
                    }
                    is com.orielle.domain.model.Response.Loading -> {
                        println("JournalEditorViewModel: Save still loading")
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save entry: ${e.message}"
                )
            }
        }
    }

    fun showDiscardDialog() {
        _uiState.value = _uiState.value.copy(showDiscardDialog = true)
    }

    fun hideDiscardDialog() {
        _uiState.value = _uiState.value.copy(showDiscardDialog = false)
    }

    fun hideSavedMessage() {
        _uiState.value = _uiState.value.copy(showSavedMessage = false)
    }

    fun toggleBold() {
        // TODO: Implement text formatting
        // This would involve tracking text selection and applying bold formatting
    }

    fun addTags(tags: List<String>) {
        _uiState.value = _uiState.value.copy(
            tags = tags,
            hasUnsavedChanges = true
        )
    }

    private fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }
}

data class JournalEditorUiState(
    val entryId: String = "",
    val tempEntryId: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Date = Date(),
    val location: String? = null,
    val tags: List<String> = emptyList(),
    val photoUrl: String? = null,
    val promptText: String? = null,
    val entryType: JournalEntryType = JournalEntryType.FREE_WRITE,
    val isEditMode: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val showSavedMessage: Boolean = false,
    val error: String? = null
)