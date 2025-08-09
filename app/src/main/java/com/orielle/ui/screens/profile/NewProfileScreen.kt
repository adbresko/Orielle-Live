package com.orielle.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.orielle.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    navController: NavController,
    userId: String,
    userName: String?,
    userEmail: String?,
    profileImageUrl: String?,
    onLogOut: () -> Unit,
    homeViewModel: com.orielle.ui.screens.home.HomeViewModel,
    viewModel: ProfileSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = !MaterialTheme.colorScheme.background.equals(SoftSand)

    val backgroundColor = if (isDark) DarkGray else SoftSand
    val textColor = if (isDark) SoftSand else Charcoal
    val cardColor = if (isDark) Color(0xFF2A2A2A) else Color.White

    // Initialize with current data
    LaunchedEffect(Unit) {
        viewModel.initializeUserData(userId, userName, userEmail, profileImageUrl)
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            // THIS IS THE KEY FIX - PROPER BACK NAVIGATION!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Settings",
                    style = Typography.headlineSmall.copy(
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = WaterBlue,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = uiState.userName ?: "Welcome",
                        style = Typography.headlineSmall.copy(
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    if (uiState.userEmail != null) {
                        Text(
                            text = uiState.userEmail!!,
                            style = Typography.bodyMedium.copy(
                                color = textColor.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Settings Items
            SettingsSection(title = "Profile", cardColor = cardColor, textColor = textColor) {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "Edit Name",
                    subtitle = uiState.userName ?: "Not set",
                    textColor = textColor
                ) { viewModel.showEditNameDialog() }

                SettingsItem(
                    icon = Icons.Default.Email,
                    title = "Edit Email",
                    subtitle = uiState.userEmail ?: "Not set",
                    textColor = textColor
                ) { viewModel.showEditEmailDialog() }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = "Security", cardColor = cardColor, textColor = textColor) {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    subtitle = "Update your password",
                    textColor = textColor
                ) { viewModel.showChangePasswordDialog() }

                SettingsToggleItem(
                    icon = Icons.Default.Security,
                    title = "Two-Factor Auth",
                    subtitle = if (uiState.twoFactorEnabled) "Enabled" else "Disabled",
                    isEnabled = uiState.twoFactorEnabled,
                    textColor = textColor
                ) { viewModel.toggleTwoFactor(it) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = "Account", cardColor = cardColor, textColor = textColor) {
                SettingsItem(
                    icon = Icons.Default.Logout,
                    title = "Sign Out",
                    subtitle = "Sign out of your account",
                    textColor = ErrorRed
                ) { viewModel.showLogoutConfirmation() }

                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Delete Account",
                    subtitle = "Permanently delete your account",
                    textColor = ErrorRed
                ) { viewModel.showDeleteAccountConfirmation() }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Handle dialogs
    HandleDialogs(uiState, viewModel, onLogOut, navController, homeViewModel)
}

@Composable
private fun SettingsSection(
    title: String,
    cardColor: Color,
    textColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Text(
        text = title,
        style = Typography.titleMedium.copy(
            color = textColor,
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = WaterBlue,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.titleSmall.copy(
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = subtitle,
                style = Typography.bodySmall.copy(
                    color = textColor.copy(alpha = 0.7f)
                )
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = textColor.copy(alpha = 0.4f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    textColor: Color,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = WaterBlue,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.titleSmall.copy(
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = subtitle,
                style = Typography.bodySmall.copy(
                    color = textColor.copy(alpha = 0.7f)
                )
            )
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = WaterBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = textColor.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun HandleDialogs(
    uiState: ProfileSettingsUiState,
    viewModel: ProfileSettingsViewModel,
    onLogOut: () -> Unit,
    navController: NavController,
    homeViewModel: com.orielle.ui.screens.home.HomeViewModel
) {
    if (uiState.showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideEditNameDialog() },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = uiState.editingName,
                    onValueChange = { viewModel.updateEditingName(it) },
                    label = { Text("Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WaterBlue,
                        focusedLabelColor = WaterBlue
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveName()
                        homeViewModel.refreshUserProfile()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaterBlue)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.hideEditNameDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showEditEmailDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideEditEmailDialog() },
            title = { Text("Edit Email") },
            text = {
                OutlinedTextField(
                    value = uiState.editingEmail,
                    onValueChange = { viewModel.updateEditingEmail(it) },
                    label = { Text("Email") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WaterBlue,
                        focusedLabelColor = WaterBlue
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.saveEmail() },
                    colors = ButtonDefaults.buttonColors(containerColor = WaterBlue)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.hideEditEmailDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideChangePasswordDialog() },
            title = { Text("Change Password") },
            text = {
                OutlinedTextField(
                    value = uiState.newPassword,
                    onValueChange = { viewModel.updateNewPassword(it) },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WaterBlue,
                        focusedLabelColor = WaterBlue
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.changePassword() },
                    colors = ButtonDefaults.buttonColors(containerColor = WaterBlue)
                ) {
                    Text("Change", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.hideChangePasswordDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.hideLogoutConfirmation() },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.hideLogoutConfirmation()
                        onLogOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Sign Out", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.hideLogoutConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showDeleteAccountConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteAccountConfirmation() },
            title = { Text("Delete Account") },
            text = { Text("This action cannot be undone. All your data will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.hideDeleteAccountConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSettingsScreenLightPreview() {
    OrielleTheme(darkTheme = false) {
        ProfileSettingsScreen(
            navController = rememberNavController(),
            userId = "user123",
            userName = "Jane Doe",
            userEmail = "jane@example.com",
            profileImageUrl = null,
            onLogOut = {},
            homeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSettingsScreenDarkPreview() {
    OrielleTheme(darkTheme = true) {
        ProfileSettingsScreen(
            navController = rememberNavController(),
            userId = "user123",
            userName = "Jane Doe",
            userEmail = "jane@example.com",
            profileImageUrl = null,
            onLogOut = {},
            homeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        )
    }
}
