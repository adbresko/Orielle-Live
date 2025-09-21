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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.orielle.R
import com.orielle.ui.components.AvatarOption
import com.orielle.ui.components.AvatarLibraryDialog
import java.io.File

@Composable
fun ProfileImageSelector(
    profileImageUrl: String?,
    localImagePath: String?,
    selectedAvatarId: String?,
    isUploading: Boolean,
    onImageUpload: (Uri) -> Unit,
    onAvatarSelect: (AvatarOption) -> Unit,
    onImageRemove: () -> Unit,
    avatarLibrary: List<AvatarOption>,
    isPremiumUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showImageOptions by remember { mutableStateOf(false) }
    var showAvatarLibrary by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageUpload(it) }
    }

    Column(
        modifier = modifier
    ) {
        // Top row: Profile image on left, action buttons on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Profile Image Display (left side)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .clickable { showImageOptions = true }
            ) {
                when {
                    isUploading -> {
                        // Loading state
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    localImagePath != null && File(localImagePath).exists() -> {
                        // Show local image first (offline access)
                        Image(
                            painter = rememberAsyncImagePainter(File(localImagePath)),
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    profileImageUrl != null -> {
                        // Show remote image
                        Image(
                            painter = rememberAsyncImagePainter(profileImageUrl),
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        // Check if there's a selected avatar
                        val selectedAvatar = avatarLibrary.find { it.id == selectedAvatarId }
                        if (selectedAvatar?.emoji != null) {
                            // Display selected emoji avatar
                            Text(
                                text = selectedAvatar.emoji,
                                fontSize = 50.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else if (selectedAvatar?.imageUrl != null) {
                            // Check if it's a drawable resource
                            if (selectedAvatar.imageUrl.startsWith("drawable://")) {
                                val drawableName = selectedAvatar.imageUrl.removePrefix("drawable://")
                                val resourceId = getDrawableResourceId(drawableName)
                                if (resourceId != null) {
                                    Image(
                                        painter = painterResource(id = resourceId),
                                        contentDescription = selectedAvatar.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    // Fallback to default icon
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Default Avatar",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .align(Alignment.Center),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                // Display URL image
                                Image(
                                    painter = rememberAsyncImagePainter(selectedAvatar.imageUrl),
                                    contentDescription = selectedAvatar.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            // Default avatar
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Avatar",
                                modifier = Modifier
                                    .size(50.dp)
                                    .align(Alignment.Center),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Edit overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile Image",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Action buttons (right side)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Upload Photo Button
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.width(140.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Photo", style = MaterialTheme.typography.bodyMedium)
                }

                // Choose Avatar Button
                Button(
                    onClick = { showAvatarLibrary = true },
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.width(140.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose Avatar", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Remove button (only show if there's an image) - centered below
        if (profileImageUrl != null || localImagePath != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = onImageRemove,
                    enabled = !isUploading,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remove Image", style = MaterialTheme.typography.bodyMedium)
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
                onImageRemove()
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

                Divider()

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
                    Divider()
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
                            text = "Remove Image",
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

// Helper function to get drawable resource ID
private fun getDrawableResourceId(drawableName: String): Int? {
    return when (drawableName) {
        "ic_happy" -> R.drawable.ic_happy
        "ic_playful" -> R.drawable.ic_playful
        "ic_surprised" -> R.drawable.ic_surprised
        "ic_peaceful" -> R.drawable.ic_peaceful
        "ic_shy" -> R.drawable.ic_shy
        "ic_sad" -> R.drawable.ic_sad
        "ic_angry" -> R.drawable.ic_angry
        "ic_frustrated" -> R.drawable.ic_frustrated
        "ic_scared" -> R.drawable.ic_scared
        else -> null
    }
}
