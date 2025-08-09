package com.orielle.ui.screens.reflect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.orielle.R
import com.orielle.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEditorScreen(
    navController: NavController,
    promptText: String? = null,
    isQuickEntry: Boolean = false,
    entryId: String? = null, // For editing existing entries
    viewModel: JournalEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isDark = !MaterialTheme.colorScheme.background.equals(SoftSand)

    val backgroundColor = if (isDark) DarkGray else SoftSand
    val textColor = if (isDark) SoftSand else Charcoal
    val cardColor = if (isDark) Color(0xFF2A2A2A) else Color.White

    // Initialize the editor with prompt text or existing entry
    LaunchedEffect(promptText, entryId) {
        if (entryId != null) {
            viewModel.loadEntry(entryId)
        } else if (promptText != null) {
            viewModel.setPromptText(promptText)
        }
        viewModel.setQuickEntry(isQuickEntry)
    }

    // Handle location permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.getCurrentLocation(context)
        }
    }

    // Request location on startup if permission is granted
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.getCurrentLocation(context)
        }
    }

    // Show confirmation dialog
    if (uiState.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDiscardDialog() },
            title = { Text("Discard Entry?") },
            text = { Text("Are you sure you want to discard this entry? Your changes will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.hideDiscardDialog()
                        navController.popBackStack()
                    }
                ) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDiscardDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Show snackbar when entry is saved
    if (uiState.showSavedMessage) {
        LaunchedEffect(uiState.showSavedMessage) {
            kotlinx.coroutines.delay(2000)
            viewModel.hideSavedMessage()
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            JournalEditorTopBar(
                textColor = textColor,
                onCloseClick = {
                    if (uiState.hasUnsavedChanges) {
                        viewModel.showDiscardDialog()
                    } else {
                        navController.popBackStack()
                    }
                },
                onSaveClick = {
                    viewModel.saveEntry {
                        navController.popBackStack()
                    }
                }
            )
        },
        bottomBar = {
            if (!isQuickEntry) {
                JournalEditorBottomBar(
                    textColor = textColor,
                    backgroundColor = backgroundColor,
                    onPhotoClick = { /* TODO: Implement photo picker */ },
                    onTagClick = { navController.navigate("journal_tagging/${uiState.tempEntryId}") },
                    onBoldClick = { viewModel.toggleBold() }
                )
            }
        },
        snackbarHost = {
            if (uiState.showSavedMessage) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WaterBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "✨ Saved to your Reflect journal",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        style = Typography.bodyMedium
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Metadata Bar
            JournalMetadataBar(
                timestamp = uiState.timestamp,
                location = uiState.location,
                textColor = textColor,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Prompt Text (if applicable)
            if (uiState.promptText != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = uiState.promptText!!,
                        modifier = Modifier.padding(16.dp),
                        style = Typography.bodyMedium.copy(
                            color = textColor.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Main Text Input
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                BasicTextField(
                    value = uiState.content,
                    onValueChange = { viewModel.updateContent(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    textStyle = Typography.bodyLarge.copy(
                        color = textColor,
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(WaterBlue),
                    decorationBox = { innerTextField ->
                        if (uiState.content.isEmpty()) {
                            Text(
                                text = if (isQuickEntry) {
                                    "What's on your mind?"
                                } else {
                                    "Let your thoughts flow..."
                                },
                                style = Typography.bodyLarge.copy(
                                    color = textColor.copy(alpha = 0.5f),
                                    lineHeight = 24.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun JournalEditorTopBar(
    textColor: Color,
    onCloseClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCloseClick) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = textColor
            )
        }

        Button(
            onClick = onSaveClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = WaterBlue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = "Done",
                style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun JournalMetadataBar(
    timestamp: Date,
    location: String?,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy  HH:mm", Locale.getDefault())

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = dateFormat.format(timestamp),
            style = Typography.bodyMedium.copy(
                color = textColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        )

        if (location != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = location,
                    style = Typography.bodySmall.copy(
                        color = textColor.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}

@Composable
private fun JournalEditorBottomBar(
    textColor: Color,
    backgroundColor: Color,
    onPhotoClick: () -> Unit,
    onTagClick: () -> Unit,
    onBoldClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPhotoClick) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Add Photo",
                    tint = textColor
                )
            }

            IconButton(onClick = onTagClick) {
                Icon(
                    imageVector = Icons.Default.Tag,
                    contentDescription = "Add Tags",
                    tint = textColor
                )
            }

            IconButton(onClick = onBoldClick) {
                Icon(
                    imageVector = Icons.Default.FormatBold,
                    contentDescription = "Bold",
                    tint = textColor
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JournalEditorScreenLightPreview() {
    OrielleTheme(darkTheme = false) {
        JournalEditorScreen(
            navController = rememberNavController(),
            promptText = "How can you grow from this experience? What can you learn from this matter?"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun JournalEditorScreenDarkPreview() {
    OrielleTheme(darkTheme = true) {
        JournalEditorScreen(
            navController = rememberNavController(),
            isQuickEntry = true
        )
    }
}
