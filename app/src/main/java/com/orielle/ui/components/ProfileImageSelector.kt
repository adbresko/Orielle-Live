package com.orielle.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.orielle.R
import com.orielle.ui.components.AvatarOption
import com.orielle.ui.components.AvatarLibraryDialog
import com.orielle.ui.components.ColorPicker
import com.orielle.ui.components.hexToColor
import com.orielle.ui.util.ScreenUtils
import java.io.File

@Composable
fun ProfileImageSelector(
    profileImageUrl: String?,
    localImagePath: String?,
    selectedAvatarId: String?,
    backgroundColorHex: String?,
    userName: String?,
    isUploading: Boolean,
    onImageUpload: (Uri) -> Unit,
    onAvatarSelect: (AvatarOption) -> Unit,
    onColorSelect: (String) -> Unit,
    onResetToDefault: () -> Unit,
    avatarLibrary: List<AvatarOption>,
    isPremiumUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Debug logging
    android.util.Log.d("ProfileImageSelector", "Profile data - ImageUrl: $profileImageUrl, LocalPath: $localImagePath, AvatarId: $selectedAvatarId, BackgroundColor: $backgroundColorHex, UserName: $userName")

    // Additional debug logging for display logic
    LaunchedEffect(profileImageUrl, localImagePath, selectedAvatarId, backgroundColorHex) {
        android.util.Log.d("ProfileImageSelector", "Display logic check:")
        android.util.Log.d("ProfileImageSelector", "- isUploading: $isUploading")
        android.util.Log.d("ProfileImageSelector", "- localImagePath exists: ${localImagePath != null && File(localImagePath).exists()}")
        android.util.Log.d("ProfileImageSelector", "- profileImageUrl valid: ${profileImageUrl != null && !profileImageUrl.startsWith("mood_icon")}")
        android.util.Log.d("ProfileImageSelector", "- selectedAvatarId: $selectedAvatarId")
        android.util.Log.d("ProfileImageSelector", "- backgroundColorHex: $backgroundColorHex")
    }

    var showImageOptions by remember { mutableStateOf(false) }
    var showAvatarLibrary by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }


    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageUpload(it) }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding() * 2),
        verticalAlignment = Alignment.Top
    ) {
        // Column 1: Profile Image Display (spans full height)
        Box(
            modifier = Modifier
                .size(ScreenUtils.responsiveIconSize(120.dp))
                .clip(CircleShape)
                .background(
                    // Only show background color if no other content is being displayed
                    if (localImagePath == null && profileImageUrl == null && selectedAvatarId == null && backgroundColorHex == null) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        Color.Transparent
                    }
                )
                .border(
                    width = ScreenUtils.responsivePadding() * 0.5f,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .clickable { showImageOptions = true }
        ) {
            when {
                isUploading -> {
                    android.util.Log.d("ProfileImageSelector", "Displaying: Loading state")
                    // Loading state
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                localImagePath != null && File(localImagePath).exists() -> {
                    android.util.Log.d("ProfileImageSelector", "Displaying: Local image from $localImagePath")
                    // Show local image first (offline access)
                    Image(
                        painter = rememberAsyncImagePainter(File(localImagePath)),
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                profileImageUrl != null && !profileImageUrl.startsWith("mood_icon") -> {
                    android.util.Log.d("ProfileImageSelector", "Displaying: Remote image from $profileImageUrl")
                    // Show remote image (but not mood icons)
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUrl),
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                selectedAvatarId != null -> {
                    android.util.Log.d("ProfileImageSelector", "Displaying: Avatar with ID $selectedAvatarId")
                    // Show selected mood icon directly
                    val resourceId = getDrawableResourceId(selectedAvatarId)
                    android.util.Log.d("ProfileImageSelector", "Selected avatar ID: $selectedAvatarId, Resource ID: $resourceId")
                    if (resourceId != null) {
                        Image(
                            painter = painterResource(id = resourceId),
                            contentDescription = "Your Mood Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        android.util.Log.w("ProfileImageSelector", "No resource found for avatar ID: $selectedAvatarId")
                        // Fallback to initials
                        UserInitialsDisplay(userName = userName)
                    }
                }
                backgroundColorHex != null -> {
                    android.util.Log.d("ProfileImageSelector", "Displaying: Initials with background color $backgroundColorHex")
                    // Show initials on colored background
                    UserInitialsDisplay(userName = userName, backgroundColorHex = backgroundColorHex)
                }
                else -> {
                    android.util.Log.d("ProfileImageSelector", "Displaying: Default initials (no special data)")
                    // Default - show initials
                    UserInitialsDisplay(userName = userName)
                }
            }

            // Edit overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(ScreenUtils.responsiveIconSize(32.dp))
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .padding(ScreenUtils.responsivePadding() * 0.5f)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile Image",
                    tint = Color.White,
                    modifier = Modifier.size(ScreenUtils.responsiveIconSize(20.dp))
                )
            }
        }

        // Column 2: Action buttons (stacked vertically)
        Column(
            verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding()),
            modifier = Modifier.weight(1f)
        ) {
            // Upload Photo Button
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = ScreenUtils.responsivePadding() * 1.5f,
                    vertical = ScreenUtils.responsivePadding() * 0.75f
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(ScreenUtils.responsiveIconSize(18.dp))
                )
                Spacer(modifier = Modifier.width(ScreenUtils.responsivePadding() * 0.5f))
                Text(
                    "Upload Photo",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp
                    )
                )
            }

            // Choose Avatar Button
            Button(
                onClick = { showAvatarLibrary = true },
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = ScreenUtils.responsivePadding() * 1.5f,
                    vertical = ScreenUtils.responsivePadding() * 0.75f
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = null,
                    modifier = Modifier.size(ScreenUtils.responsiveIconSize(18.dp))
                )
                Spacer(modifier = Modifier.width(ScreenUtils.responsivePadding() * 0.5f))
                Text(
                    "Choose Avatar",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp
                    )
                )
            }

            // Choose Color Button
            Button(
                onClick = { showColorPicker = true },
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = ScreenUtils.responsivePadding() * 1.5f,
                    vertical = ScreenUtils.responsivePadding() * 0.75f
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    modifier = Modifier.size(ScreenUtils.responsiveIconSize(18.dp))
                )
                Spacer(modifier = Modifier.width(ScreenUtils.responsivePadding() * 0.5f))
                Text(
                    "Choose Color",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp
                    )
                )
            }

            // Reset to Default button (show if there's any profile data)
            if (profileImageUrl != null || localImagePath != null || selectedAvatarId != null || backgroundColorHex != null) {
                TextButton(
                    onClick = onResetToDefault,
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(
                        horizontal = ScreenUtils.responsivePadding() * 0.5f,
                        vertical = ScreenUtils.responsivePadding() * 0.25f
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(ScreenUtils.responsivePadding() * 0.25f))
                    Text(
                        "Reset to Default",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = (12 * ScreenUtils.getTextScaleFactor()).sp
                        )
                    )
                }
            }
        }
    }

    // Image Options Dialog
    if (showImageOptions) {
        ImageOptionsDialog(
            onDismiss = { showImageOptions = false },
            onUploadPhoto = {
                showImageOptions = false
                imagePickerLauncher.launch("image/*")
            },
            onChooseAvatar = {
                showImageOptions = false
                showAvatarLibrary = true
            },
            onRemoveImage = {
                showImageOptions = false
                onResetToDefault()
            },
            hasImage = profileImageUrl != null || localImagePath != null
        )
    }

    // Avatar Library Dialog
    if (showAvatarLibrary) {
        AvatarLibraryDialog(
            avatars = avatarLibrary,
            selectedAvatarId = selectedAvatarId,
            onAvatarSelected = { avatar ->
                onAvatarSelect(avatar)
                showAvatarLibrary = false
            },
            onDismiss = { showAvatarLibrary = false },
            isPremiumUser = isPremiumUser
        )
    }

    // Color Picker Dialog
    if (showColorPicker) {
        Dialog(
            onDismissRequest = { showColorPicker = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ColorPicker(
                        selectedColor = backgroundColorHex,
                        onColorSelected = { color ->
                            onColorSelect(color)
                            showColorPicker = false
                        }
                    )

                    Button(
                        onClick = { showColorPicker = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageOptionsDialog(
    onDismiss: () -> Unit,
    onUploadPhoto: () -> Unit,
    onChooseAvatar: () -> Unit,
    onRemoveImage: () -> Unit,
    hasImage: Boolean
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Profile Image Options",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Upload Photo Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUploadPhoto() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Upload Photo",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                HorizontalDivider()

                // Choose Avatar Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChooseAvatar() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Choose Avatar",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Remove Image Option (only if there's an image)
                if (hasImage) {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRemoveImage() }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Reset to Default",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

private fun getDrawableResourceId(avatarId: String): Int? {
    return when (avatarId) {
        "happy" -> R.drawable.ic_happy
        "playful" -> R.drawable.ic_playful
        "surprised" -> R.drawable.ic_surprised
        "peaceful" -> R.drawable.ic_peaceful
        "shy" -> R.drawable.ic_shy
        "sad" -> R.drawable.ic_sad
        "angry" -> R.drawable.ic_angry
        "frustrated" -> R.drawable.ic_frustrated
        "scared" -> R.drawable.ic_scared
        else -> null
    }
}

@Composable
private fun UserInitialsDisplay(userName: String?, backgroundColorHex: String? = null) {
    val initials = userName?.let { name ->
        name.split(" ").mapNotNull { it.firstOrNull()?.toString() }
            .take(2).joinToString("").uppercase()
    } ?: "U"

    val backgroundColor = backgroundColorHex?.let {
        android.util.Log.d("ProfileImageSelector", "Converting hex to color: $backgroundColorHex")
        val color = hexToColor(it)
        android.util.Log.d("ProfileImageSelector", "Converted color: $color")
        color
    } ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = backgroundColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}


// Preview functions
@Preview(showBackground = true, name = "Profile Image Selector - Light")
@Composable
private fun ProfileImageSelectorLightPreview() {
    com.orielle.ui.theme.OrielleTheme(darkTheme = false) {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            ProfileImageSelector(
                profileImageUrl = null,
                localImagePath = null,
                selectedAvatarId = null,
                backgroundColorHex = null,
                userName = "John Doe",
                isUploading = false,
                onImageUpload = {},
                onAvatarSelect = {},
                onColorSelect = {},
                onResetToDefault = {},
                avatarLibrary = listOf(
                    AvatarOption(id = "happy", name = "Happy", imageUrl = "mood_icon", isPremium = false),
                    AvatarOption(id = "sad", name = "Sad", imageUrl = "mood_icon", isPremium = false)
                ),
                isPremiumUser = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Profile Image Selector - Dark")
@Composable
private fun ProfileImageSelectorDarkPreview() {
    com.orielle.ui.theme.OrielleTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            ProfileImageSelector(
                profileImageUrl = null,
                localImagePath = null,
                selectedAvatarId = null,
                backgroundColorHex = null,
                userName = "Jane Smith",
                isUploading = false,
                onImageUpload = {},
                onAvatarSelect = {},
                onColorSelect = {},
                onResetToDefault = {},
                avatarLibrary = listOf(
                    AvatarOption(id = "happy", name = "Happy", imageUrl = "mood_icon", isPremium = false),
                    AvatarOption(id = "sad", name = "Sad", imageUrl = "mood_icon", isPremium = false)
                ),
                isPremiumUser = true
            )
        }
    }
}
