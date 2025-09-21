package com.orielle.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

@Composable
fun AvatarLibraryDialog(
    avatars: List<AvatarOption>,
    selectedAvatarId: String?,
    onAvatarSelected: (AvatarOption) -> Unit,
    onDismiss: () -> Unit,
    isPremiumUser: Boolean = false
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choose Your Avatar",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Select from our curated collection of beautiful avatars",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Avatar Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(avatars) { avatar ->
                        AvatarItem(
                            avatar = avatar,
                            isSelected = avatar.id == selectedAvatarId,
                            isPremiumUser = isPremiumUser,
                            onClick = { onAvatarSelected(avatar) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Premium notice
                if (!isPremiumUser) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Premium",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Premium avatars require a premium subscription",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarItem(
    avatar: AvatarOption,
    isSelected: Boolean,
    isPremiumUser: Boolean,
    onClick: () -> Unit
) {
    val isClickable = !avatar.isPremium || isPremiumUser

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = isClickable) { onClick() }
            .alpha(if (isClickable) 1f else 0.5f)
    ) {
        // Avatar image or emoji
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
            if (avatar.emoji != null) {
                // Display emoji
                Text(
                    text = avatar.emoji,
                    fontSize = 40.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (avatar.imageUrl != null) {
                // Check if it's a drawable resource
                if (avatar.imageUrl.startsWith("drawable://")) {
                    val drawableName = avatar.imageUrl.removePrefix("drawable://")
                    val resourceId = getDrawableResourceId(drawableName)
                    if (resourceId != null) {
                        Image(
                            painter = painterResource(id = resourceId),
                            contentDescription = avatar.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        // Fallback to default icon
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = avatar.name,
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Display URL image
                    Image(
                        painter = rememberAsyncImagePainter(avatar.imageUrl),
                        contentDescription = avatar.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // Fallback to default icon
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = avatar.name,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Premium badge
            if (avatar.isPremium) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Premium",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Avatar name
        Text(
            text = avatar.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
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
