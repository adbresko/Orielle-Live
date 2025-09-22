package com.orielle.ui.screens.mood

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.orielle.R
import com.orielle.ui.theme.*
import com.orielle.ui.util.ScreenUtils
import com.orielle.ui.components.WaterDropLoading
import com.orielle.domain.model.MoodCheckIn
import kotlin.math.min

@Composable
fun InnerWeatherHistoryScreen(
    navController: NavController,
    viewModel: InnerWeatherHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeColors = MaterialTheme.colorScheme
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var checkInToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadMoodHistory()
    }

    Scaffold(
        containerColor = themeColors.background,
        topBar = {
            HistoryTopBar(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(themeColors.background)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WaterDropLoading(
                        size = ScreenUtils.responsiveImageSize(60.dp).value.toInt(),
                        modifier = Modifier.size(ScreenUtils.responsiveImageSize(60.dp))
                    )
                    Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing() * 2))
                    Text(
                        text = "Loading your inner weather history...",
                        style = Typography.bodyMedium.copy(color = themeColors.onBackground),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(themeColors.background)
                    .padding(paddingValues)
                    .padding(horizontal = ScreenUtils.responsivePadding()),
                contentPadding = PaddingValues(bottom = ScreenUtils.responsiveIconSize()),
                verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
            ) {

                // Filter Section
                if (uiState.availableMoods.isNotEmpty() || uiState.availableTags.isNotEmpty()) {
                    item {
                        FilterSection(
                            selectedMoods = uiState.selectedMoods,
                            selectedTags = uiState.selectedTags,
                            availableMoods = uiState.availableMoods,
                            availableTags = uiState.availableTags,
                            onMoodToggle = { viewModel.toggleMoodFilter(it) },
                            onTagToggle = { viewModel.toggleTagFilter(it) },
                            onClearFilters = { viewModel.clearAllFilters() }
                        )
                    }

                }

                // Results Section
                if (uiState.filteredMoodCheckIns.isEmpty() && (uiState.selectedMoods.isNotEmpty() || uiState.selectedTags.isNotEmpty())) {
                    item {
                        EmptyHistoryResults()
                    }
                } else if (uiState.filteredMoodCheckIns.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = ScreenUtils.responsiveSpacing()),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Inner Weather History (${uiState.filteredMoodCheckIns.size})",
                                fontFamily = Lora,
                                fontSize = (18 * ScreenUtils.getTextScaleFactor()).sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            // Selection mode controls
                            if (uiState.isSelectionMode) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Selection count
                                    Text(
                                        text = "${uiState.selectedCheckIns.size} of ${uiState.filteredMoodCheckIns.size} selected",
                                        fontFamily = NotoSans,
                                        fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp,
                                        color = themeColors.onBackground.copy(alpha = 0.7f)
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    // Select All / Deselect All button
                                    TextButton(
                                        onClick = {
                                            if (uiState.selectedCheckIns.size == uiState.filteredMoodCheckIns.size) {
                                                // Deselect all
                                                uiState.filteredMoodCheckIns.forEach { checkIn ->
                                                    viewModel.toggleCheckInSelection(checkIn.id)
                                                }
                                            } else {
                                                // Select all
                                                uiState.filteredMoodCheckIns.forEach { checkIn ->
                                                    if (!uiState.selectedCheckIns.contains(checkIn.id)) {
                                                        viewModel.toggleCheckInSelection(checkIn.id)
                                                    }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = StillwaterTeal
                                        )
                                    ) {
                                        Text(
                                            text = if (uiState.selectedCheckIns.size == uiState.filteredMoodCheckIns.size) "Deselect All" else "Select All",
                                            fontFamily = NotoSans,
                                            fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp
                                        )
                                    }

                                    // Delete Selected button (only show if items are selected)
                                    if (uiState.selectedCheckIns.isNotEmpty()) {
                                        Button(
                                            onClick = { viewModel.deleteSelectedCheckIns() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ErrorRed,
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(ScreenUtils.responsivePadding())
                                        ) {
                                            Text(
                                                text = "Delete (${uiState.selectedCheckIns.size})",
                                                fontFamily = NotoSans,
                                                fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp
                                            )
                                        }
                                    }

                                    // Cancel button
                                    TextButton(
                                        onClick = { viewModel.toggleSelectionMode() },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = themeColors.onBackground
                                        )
                                    ) {
                                        Text(
                                            text = "Cancel",
                                            fontFamily = NotoSans,
                                            fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp
                                        )
                                    }
                                }
                            } else {
                                // Normal mode - just show Select button
                                TextButton(
                                    onClick = { viewModel.toggleSelectionMode() },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = StillwaterTeal
                                    )
                                ) {
                                    Text(
                                        text = "Select",
                                        fontFamily = NotoSans,
                                        fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp
                                    )
                                }
                            }
                        }
                    }

                    items(uiState.filteredMoodCheckIns) { moodCheckIn ->
                        MoodHistoryCard(
                            moodCheckIn = moodCheckIn,
                            formatDate = { viewModel.formatMoodDate(it) },
                            isSelected = uiState.selectedCheckIns.contains(moodCheckIn.id),
                            isSelectionMode = uiState.isSelectionMode,
                            onSelect = { viewModel.toggleCheckInSelection(moodCheckIn.id) },
                            onDelete = {
                                checkInToDelete = moodCheckIn.id
                                showDeleteConfirmation = true
                            }
                        )
                    }
                } else if (uiState.moodCheckIns.isEmpty()) {
                    item {
                        EmptyHistoryState()
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                checkInToDelete = null
            },
            title = {
                Text(
                    text = "Delete Mood Check-in",
                    fontFamily = NotoSans,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this mood check-in? This action cannot be undone.",
                    fontFamily = NotoSans
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        checkInToDelete?.let { viewModel.deleteCheckIn(it) }
                        showDeleteConfirmation = false
                        checkInToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Delete", fontFamily = NotoSans)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        checkInToDelete = null
                    }
                ) {
                    Text("Cancel", fontFamily = NotoSans)
                }
            }
        )
    }
}

@Composable
private fun HistoryTopBar(
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(ScreenUtils.responsivePadding()),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp))
            )
        }

        // Title
        Text(
            text = "Inner Weather History",
            fontFamily = Lora,
            fontSize = (24 * ScreenUtils.getTextScaleFactor()).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Empty space to center the title
        Spacer(modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp)))
    }
}

@Composable
private fun FilterSection(
    selectedMoods: Set<String>,
    selectedTags: Set<String>,
    availableMoods: List<String>,
    availableTags: List<String>,
    onMoodToggle: (String) -> Unit,
    onTagToggle: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    val themeColors = MaterialTheme.colorScheme
    Column {
        // Clear filters button
        if (selectedMoods.isNotEmpty() || selectedTags.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters Applied",
                    fontFamily = NotoSans,
                    fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(onClick = onClearFilters) {
                    Text(
                        text = "Clear All",
                        fontFamily = NotoSans,
                        fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp,
                        color = themeColors.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing()))
        }

        // Mood Filter
        if (availableMoods.isNotEmpty()) {
            FilterGroup(
                title = "Mood",
                content = {
                    // Create a 3x3 grid layout for mood icons
                    val moodChunks = availableMoods.chunked(3)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
                    ) {
                        moodChunks.forEach { moodRow ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                moodRow.forEach { mood ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        MoodFilterChip(
                                            mood = mood,
                                            isSelected = selectedMoods.contains(mood),
                                            onClick = { onMoodToggle(mood) }
                                        )
                                    }
                                }
                                // Fill remaining space if row has less than 3 items
                                repeat(3 - moodRow.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))
        }

        // Tags Filter
        if (availableTags.isNotEmpty()) {
            FilterGroup(
                title = "Tags",
                content = {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                        verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                        modifier = Modifier.heightIn(max = ScreenUtils.responsiveImageSize(200.dp)) // Limit height to prevent too much space
                    ) {
                        items(availableTags) { tag ->
                            TagFilterChip(
                                tag = tag,
                                isSelected = selectedTags.contains(tag),
                                onClick = { onTagToggle(tag) }
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun FilterGroup(
    title: String,
    content: @Composable () -> Unit
) {
    val themeColors = MaterialTheme.colorScheme
    Column {
        Text(
            text = title,
            fontFamily = NotoSans,
            fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.onBackground,
            modifier = Modifier.padding(bottom = ScreenUtils.responsiveSpacing())
        )
        content()
    }
}

@Composable
private fun MoodFilterChip(
    mood: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (baseContainerSize, baseIconSize) = ScreenUtils.getResponsiveSizes()
    // Increase sizes slightly using dynamic scaling
    val containerSize = baseContainerSize * 1.15f
    val iconSize = baseIconSize * 1.15f

    // Color mapping similar to mood check-in screen - using true colors
    val themeColors = MaterialTheme.colorScheme

    val moodColors = mapOf(
        "happy" to AuroraGold.copy(alpha = 0.2f),
        "sad" to themeColors.surfaceVariant,
        "angry" to ErrorRed.copy(alpha = 0.2f),
        "frustrated" to ErrorRed.copy(alpha = 0.15f),
        "scared" to themeColors.primary.copy(alpha = 0.2f),
        "surprised" to StillwaterTeal.copy(alpha = 0.2f),
        "playful" to AuroraGold.copy(alpha = 0.2f),
        "shy" to StillwaterTeal.copy(alpha = 0.15f),
        "peaceful" to themeColors.primary.copy(alpha = 0.15f)
    )

    val backgroundColor = moodColors[mood.lowercase()] ?: themeColors.surfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(containerSize)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) (containerSize * 0.05f).coerceAtLeast(2.dp) else (containerSize * 0.017f).coerceAtLeast(1.dp),
                    color = if (isSelected) themeColors.primary else themeColors.outline,
                    shape = CircleShape
                )
                .background(
                    color = if (isSelected) backgroundColor.copy(alpha = 0.3f) else backgroundColor.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Use mood icons without tint to preserve their native designed colors
            when (mood.lowercase()) {
                "happy" -> Icon(
                    painter = painterResource(id = R.drawable.ic_happy),
                    contentDescription = "Happy",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "sad" -> Icon(
                    painter = painterResource(id = R.drawable.ic_sad),
                    contentDescription = "Sad",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "angry" -> Icon(
                    painter = painterResource(id = R.drawable.ic_angry),
                    contentDescription = "Angry",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "peaceful" -> Icon(
                    painter = painterResource(id = R.drawable.ic_peaceful),
                    contentDescription = "Peaceful",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "playful" -> Icon(
                    painter = painterResource(id = R.drawable.ic_playful),
                    contentDescription = "Playful",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "scared" -> Icon(
                    painter = painterResource(id = R.drawable.ic_scared),
                    contentDescription = "Scared",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "shy" -> Icon(
                    painter = painterResource(id = R.drawable.ic_shy),
                    contentDescription = "Shy",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "surprised" -> Icon(
                    painter = painterResource(id = R.drawable.ic_surprised),
                    contentDescription = "Surprised",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "frustrated" -> Icon(
                    painter = painterResource(id = R.drawable.ic_frustrated),
                    contentDescription = "Frustrated",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                else -> Text(
                    text = mood.take(2).uppercase(),
                    fontFamily = NotoSans,
                    fontSize = (12 * ScreenUtils.getTextScaleFactor()).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) themeColors.primary else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Add mood label below the icon
        Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing() * 0.5f))
        Text(
            text = mood.replaceFirstChar { it.uppercase() },
            fontFamily = NotoSans,
            fontSize = (12 * ScreenUtils.getTextScaleFactor()).sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) themeColors.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TagFilterChip(
    tag: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val themeColors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(ScreenUtils.responsivePadding()),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) themeColors.primary.copy(alpha = 0.15f) else themeColors.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, themeColors.primary)
        else
            getCardBorder()
    ) {
        Text(
            text = tag,
            fontFamily = NotoSans,
            fontSize = (12 * ScreenUtils.getTextScaleFactor()).sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) StillwaterTeal else themeColors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = ScreenUtils.responsiveSpacing(), vertical = ScreenUtils.responsiveSpacing() * 0.75f)
        )
    }
}

@Composable
private fun MoodHistoryCard(
    moodCheckIn: MoodCheckIn,
    formatDate: (java.util.Date) -> String,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onSelect: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val themeColors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isSelectionMode && onSelect != null) {
                    onSelect()
                }
            }
    ) {
        // Title with color-coded type (matching Remember screen style)
        Text(
            text = "Mood Check-in",
            fontFamily = NotoSans,
            fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.onBackground
        )

        // Preview content - mood and date
        Text(
            text = "${moodCheckIn.mood.replaceFirstChar { it.uppercase() }} • ${formatDate(moodCheckIn.timestamp)}",
            fontFamily = NotoSans,
            fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp,
            color = themeColors.onSurface,
            maxLines = 1,
            modifier = Modifier.padding(top = ScreenUtils.responsiveSpacing())
        )

        // Tags preview (matching Remember screen style)
        if (moodCheckIn.tags.isNotEmpty()) {
            Text(
                text = moodCheckIn.tags.joinToString(", "),
                fontFamily = NotoSans,
                fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp,
                color = themeColors.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                modifier = Modifier.padding(top = ScreenUtils.responsiveSpacing() * 0.5f)
            )
        }

        // Action link (matching Remember screen style)
        Text(
            text = "View details...",
            fontFamily = NotoSans,
            fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp,
            color = themeColors.onBackground,
            modifier = Modifier.padding(top = ScreenUtils.responsiveSpacing())
        )

        // Selection mode controls
        if (isSelectionMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ScreenUtils.responsiveSpacing()),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox with label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing() * 0.5f)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelect?.invoke() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = StillwaterTeal,
                            uncheckedColor = themeColors.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = if (isSelected) "Selected" else "Select",
                        fontFamily = NotoSans,
                        fontSize = (12 * ScreenUtils.getTextScaleFactor()).sp,
                        color = if (isSelected) StillwaterTeal else themeColors.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Individual delete button (only show if selected)
                if (isSelected && onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(ScreenUtils.responsiveIconSize(32.dp))
                            .background(
                                color = ErrorRed.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ErrorRed,
                            modifier = Modifier.size(ScreenUtils.responsiveIconSize(16.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryResults() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = ScreenUtils.responsiveImageSize(80.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Water drop icon
        Icon(
            painter = painterResource(id = R.drawable.ic_orielle_drop),
            contentDescription = "Water Drop",
            modifier = Modifier.size(ScreenUtils.responsiveImageSize()),
            tint = Color.Unspecified // No tint - preserve native colors
        )

        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))

        Text(
            text = "No Inner Weather Found",
            fontFamily = NotoSans,
            fontSize = (18 * ScreenUtils.getTextScaleFactor()).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing()))

        Text(
            text = "Try adjusting your filters to see more results",
            fontFamily = NotoSans,
            fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = ScreenUtils.responsiveImageSize(80.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Water drop icon
        Icon(
            painter = painterResource(id = R.drawable.ic_orielle_drop),
            contentDescription = "Water Drop",
            modifier = Modifier.size(ScreenUtils.responsiveImageSize()),
            tint = Color.Unspecified // No tint - preserve native colors
        )

        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))

        Text(
            text = "No Inner Weather Yet",
            fontFamily = NotoSans,
            fontSize = (18 * ScreenUtils.getTextScaleFactor()).sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing()))

        Text(
            text = "Start your first mood check-in to track your inner weather patterns",
            fontFamily = NotoSans,
            fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "Inner Weather History Screen - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun InnerWeatherHistoryScreenPreview() {
    OrielleTheme(darkTheme = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            HistoryTopBar(
                onNavigateBack = {}
            )

            Column(
                modifier = Modifier.padding(ScreenUtils.responsivePadding())
            ) {
                FilterSection(
                    selectedMoods = setOf("happy"),
                    selectedTags = setOf("inspired"),
                    availableMoods = listOf("happy", "sad", "angry", "peaceful", "playful", "scared", "shy", "surprised", "frustrated"),
                    availableTags = listOf("inspired", "family", "work", "growth"),
                    onMoodToggle = {},
                    onTagToggle = {},
                    onClearFilters = {}
                )

                Spacer(modifier = Modifier.height(ScreenUtils.responsiveIconSize()))

                // Sample mood history cards
                val sampleMoodCheckIn = MoodCheckIn(
                    id = "1",
                    userId = "user1",
                    mood = "Happy",
                    tags = listOf("Inspired", "Family"),
                    timestamp = java.util.Date(),
                    notes = null
                )

                MoodHistoryCard(
                    moodCheckIn = sampleMoodCheckIn,
                    formatDate = { "Monday, Sep 8, 2025" }
                )
            }
        }
    }
}

@Preview(name = "Inner Weather History Screen - Dark", showBackground = true, backgroundColor = 0xFF2C2C2C)
@Composable
fun InnerWeatherHistoryScreenDarkPreview() {
    OrielleTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            HistoryTopBar(
                onNavigateBack = {}
            )

            Column(
                modifier = Modifier.padding(ScreenUtils.responsivePadding())
            ) {
                FilterSection(
                    selectedMoods = setOf("happy"),
                    selectedTags = setOf("inspired"),
                    availableMoods = listOf("happy", "sad", "angry", "peaceful", "playful", "scared", "shy", "surprised", "frustrated"),
                    availableTags = listOf("inspired", "family", "work", "growth"),
                    onMoodToggle = {},
                    onTagToggle = {},
                    onClearFilters = {}
                )

                Spacer(modifier = Modifier.height(ScreenUtils.responsiveIconSize()))

                // Sample mood history cards
                val sampleMoodCheckIn = MoodCheckIn(
                    id = "1",
                    userId = "user1",
                    mood = "Happy",
                    tags = listOf("Inspired", "Family"),
                    timestamp = java.util.Date(),
                    notes = null
                )

                MoodHistoryCard(
                    moodCheckIn = sampleMoodCheckIn,
                    formatDate = { "Monday, Sep 8, 2025" }
                )
            }
        }
    }
}
