package com.orielle.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.orielle.R
import kotlinx.coroutines.tasks.await
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.ui.text.input.VisualTransformation
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.launch

@Composable
fun ProfileSettingsScreen(
    navController: NavController,
    userId: String,
    userName: String?,
    userEmail: String?,
    profileImageUrl: String?,
    onLogOut: () -> Unit,
    homeViewModel: com.orielle.ui.screens.home.HomeViewModel // Add this parameter
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }
    var uploadedImageUrl by remember { mutableStateOf(profileImageUrl) }
    var showEditName by remember { mutableStateOf(false) }
    var showEditEmail by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showOtpReset by remember { mutableStateOf(false) }
    var showDeleteAccount by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(userName ?: "") }
    var newEmail by remember { mutableStateOf(userEmail ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var resetEmail by remember { mutableStateOf("") }
    var resetStatus by remember { mutableStateOf<String?>(null) }
    var twoFactorEnabled by remember { mutableStateOf(false) }
    var show2FADialog by remember { mutableStateOf(false) }
    var twoFACode by remember { mutableStateOf("") }
    var twoFAStatus by remember { mutableStateOf<String?>(null) }
    var showExportStatus by remember { mutableStateOf(false) }
    var exportStatus by remember { mutableStateOf<String?>(null) }
    var biometricsEnabled by remember { mutableStateOf(false) }
    var showBiometricDialog by remember { mutableStateOf(false) }
    var pinEnabled by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinCode by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var pinStatus by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }
    val sessionManager = remember { com.orielle.data.manager.SessionManagerImpl(context, FirebaseAuth.getInstance()) }

    LaunchedEffect(imageUri) {
        if (imageUri != null) {
            uploading = true
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
            val uploadTask = storageRef.putFile(imageUri!!)
            uploadTask.await()
            val url = storageRef.downloadUrl.await().toString()
            // Save to Firestore
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("profileImageUrl", url)
            uploadedImageUrl = url
            uploading = false
        }
    }

    // Fetch 2FA enabled flag from Firestore on launch
    LaunchedEffect(userId) {
        val doc = FirebaseFirestore.getInstance().collection("users").document(userId).get().await()
        twoFactorEnabled = doc.getBoolean("twoFactorEnabled") == true
    }

    // Fetch biometrics and PIN flags from DataStore/Firestore on launch (pseudo-code, adjust as needed)
    LaunchedEffect(userId) {
        biometricsEnabled = sessionManager.isBiometricsEnabled()
        pinEnabled = sessionManager.getPinCode() != null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        // Profile Image
        Box(contentAlignment = Alignment.Center) {
            if (uploadedImageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(uploadedImageUrl),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .clickable { launcher.launch("image/*") }
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Placeholder",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .clickable { launcher.launch("image/*") },
                    tint = Color.LightGray
                )
            }
            if (uploading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(text = userName ?: "User", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Text(text = userEmail ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
        Spacer(Modifier.height(32.dp))
        // --- Profile Section ---
        Text("Profile", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
        ListItem(
            headlineContent = { Text("Edit Name", color = Color.White) },
            supportingContent = { Text(userName ?: "", color = Color.LightGray) },
            modifier = Modifier.clickable { showEditName = true }
        )
        ListItem(
            headlineContent = { Text("Edit Email", color = Color.White) },
            supportingContent = { Text(userEmail ?: "", color = Color.LightGray) },
            modifier = Modifier.clickable { showEditEmail = true }
        )
        // --- Security Section ---
        Spacer(Modifier.height(24.dp))
        Text("Security", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
        ListItem(
            headlineContent = { Text("Change Password", color = Color.White) },
            modifier = Modifier.clickable { showChangePassword = true }
        )
        ListItem(
            headlineContent = { Text("Reset Password via OTP", color = Color.White) },
            modifier = Modifier.clickable { showOtpReset = true }
        )
        ListItem(
            headlineContent = { Text("Two-Factor Authentication (2FA)", color = Color.White) },
            supportingContent = { Text(if (twoFactorEnabled) "Enabled" else "Disabled", color = Color.LightGray) },
            trailingContent = {
                Switch(
                    checked = twoFactorEnabled,
                    onCheckedChange = { enabled ->
                        twoFactorEnabled = enabled
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                            .update("twoFactorEnabled", enabled)
                        if (enabled) show2FADialog = true
                    }
                )
            }
        )
        if (show2FADialog) {
            AlertDialog(
                onDismissRequest = { show2FADialog = false },
                title = { Text("2FA Setup") },
                text = {
                    Column {
                        Text("A 6-digit code will be sent to your email. Enter it below to verify.")
                        OutlinedTextField(
                            value = twoFACode,
                            onValueChange = { twoFACode = it },
                            label = { Text("2FA Code") }
                        )
                        if (twoFAStatus != null) Text(twoFAStatus!!, color = Color.Green)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        // Simulate sending code via email (in production, use backend or Firebase Functions)
                        val code = (100000..999999).random().toString()
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                            .update("twoFACode", code)
                        // Instruct user to check their email (simulate for now)
                        twoFAStatus = "Code sent to your email (simulated: $code)"
                    }) { Text("Send Code") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { show2FADialog = false }) { Text("Cancel") }
                }
            )
        }
        ListItem(
            headlineContent = { Text("Enable Biometrics", color = Color.White) },
            supportingContent = { Text(if (biometricsEnabled) "Enabled" else "Disabled", color = Color.LightGray) },
            trailingContent = {
                Switch(
                    checked = biometricsEnabled,
                    onCheckedChange = { enabled ->
                        biometricsEnabled = enabled
                        showBiometricDialog = enabled
                        scope.launch { sessionManager.setBiometricsEnabled(enabled) }
                    }
                )
            }
        )
        if (showBiometricDialog) {
            AlertDialog(
                onDismissRequest = { showBiometricDialog = false },
                title = { Text("Enable Biometrics") },
                text = { Text("Authenticate with your fingerprint or face to enable biometrics.") },
                confirmButton = {
                    Button(onClick = {
                        // Launch BiometricPrompt
                        val biometricManager = BiometricManager.from(context)
                        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Biometric Authentication")
                                .setSubtitle("Authenticate to enable biometrics")
                                .setNegativeButtonText("Cancel")
                                .build()
                            val biometricPrompt = BiometricPrompt(
                                context as androidx.fragment.app.FragmentActivity,
                                ContextCompat.getMainExecutor(context),
                                object : BiometricPrompt.AuthenticationCallback() {
                                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                        biometricsEnabled = true
                                        showBiometricDialog = false
                                        // TODO: Save to DataStore/Firestore
                                    }
                                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                        biometricsEnabled = false
                                        showBiometricDialog = false
                                    }
                                }
                            )
                            biometricPrompt.authenticate(promptInfo)
                        } else {
                            showBiometricDialog = false
                        }
                    }) { Text("Authenticate") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showBiometricDialog = false }) { Text("Cancel") }
                }
            )
        }
        ListItem(
            headlineContent = { Text("Set PIN Code", color = Color.White) },
            supportingContent = { Text(if (pinEnabled) "Enabled" else "Disabled", color = Color.LightGray) },
            trailingContent = {
                Switch(
                    checked = pinEnabled,
                    onCheckedChange = { enabled ->
                        pinEnabled = enabled
                        showPinDialog = enabled
                        // TODO: Save to DataStore/Firestore
                    }
                )
            }
        )
        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = { Text("Set PIN Code") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it },
                            label = { Text("Enter PIN") },
                            visualTransformation = PasswordVisualTransformation()
                        )
                        OutlinedTextField(
                            value = pinConfirm,
                            onValueChange = { pinConfirm = it },
                            label = { Text("Confirm PIN") },
                            visualTransformation = PasswordVisualTransformation()
                        )
                        if (pinStatus != null) Text(pinStatus!!, color = Color.Red)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (pinCode.length < 4) {
                            pinStatus = "PIN must be at least 4 digits."
                        } else if (pinCode != pinConfirm) {
                            pinStatus = "PINs do not match."
                        } else {
                            // TODO: Save PIN securely (EncryptedSharedPreferences or DataStore)
                            scope.launch {
                                sessionManager.setPinCode(pinCode)
                                pinStatus = "PIN set successfully!"
                                showPinDialog = false
                            }
                        }
                    }) { Text("Save PIN") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showPinDialog = false }) { Text("Cancel") }
                }
            )
        }
        // --- Preferences Section ---
        Spacer(Modifier.height(24.dp))
        Text("Preferences", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
        ListItem(
            headlineContent = { Text("Theme", color = Color.White) },
            supportingContent = { Text("Dark", color = Color.LightGray) },
            // TODO: Add theme toggle
        )
        ListItem(
            headlineContent = { Text("Notifications", color = Color.White) },
            supportingContent = { Text("On", color = Color.LightGray) },
            // TODO: Add notifications toggle
        )
        ListItem(
            headlineContent = { Text("Export My Data", color = Color.White) },
            modifier = Modifier.clickable {
                showExportStatus = true
                // Use context here, not inside a lambda
                val db = FirebaseFirestore.getInstance()
                val exportContext = context // capture context for use in callback
                db.collection("journalEntries").whereEqualTo("userId", userId).get().addOnSuccessListener { snapshot ->
                    val csv = StringBuilder()
                    csv.append("id,content,timestamp,mood\n")
                    for (doc in snapshot.documents) {
                        val id = doc.getString("id") ?: ""
                        val content = doc.getString("content")?.replace(",", " ") ?: ""
                        val timestamp = doc.getDate("timestamp")?.time ?: 0L
                        val mood = doc.getString("mood") ?: ""
                        csv.append("$id,$content,$timestamp,$mood\n")
                    }
                    // Save to file and share
                    val file = File(exportContext.cacheDir, "orielle_export_${UUID.randomUUID()}.csv")
                    FileOutputStream(file).use { it.write(csv.toString().toByteArray()) }
                    val uri = androidx.core.content.FileProvider.getUriForFile(exportContext, exportContext.packageName + ".provider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(exportContext, Intent.createChooser(intent, "Export Data"), null)
                    exportStatus = "Exported successfully!"
                }.addOnFailureListener {
                    exportStatus = "Export failed: ${it.message}"
                }
            }
        )
        if (showExportStatus) {
            AlertDialog(
                onDismissRequest = { showExportStatus = false },
                title = { Text("Export Data") },
                text = { Text(exportStatus ?: "Exporting...") },
                confirmButton = {
                    Button(onClick = { showExportStatus = false }) { Text("OK") }
                }
            )
        }
        // --- Account Section ---
        Spacer(Modifier.height(24.dp))
        Text("Account", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
        ListItem(
            headlineContent = { Text("Log Out", color = Color.Red) },
            modifier = Modifier.clickable { onLogOut() }
        )
        ListItem(
            headlineContent = { Text("Delete Account", color = Color.Red) },
            modifier = Modifier.clickable { showDeleteAccount = true }
        )
    }
    // --- Dialogs ---
    if (showEditName) {
        EditFieldDialog(
            title = "Edit Name",
            value = newName,
            onValueChange = { newName = it },
            onDismiss = { showEditName = false },
            onSave = {
                // Save to Firestore
                FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update("firstName", newName)
                showEditName = false
                homeViewModel.refreshUserProfile() // Refresh dashboard name
            }
        )
    }
    if (showEditEmail) {
        EditFieldDialog(
            title = "Edit Email",
            value = newEmail,
            onValueChange = { newEmail = it },
            onDismiss = { showEditEmail = false },
            onSave = {
                // Save to Firestore
                FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update("email", newEmail)
                showEditEmail = false
            }
        )
    }
    if (showChangePassword) {
        EditFieldDialog(
            title = "Change Password",
            value = newPassword,
            onValueChange = { newPassword = it },
            onDismiss = { showChangePassword = false },
            onSave = {
                // Change password with Firebase Auth
                FirebaseAuth.getInstance().currentUser?.updatePassword(newPassword)
                showChangePassword = false
            },
            isPassword = true
        )
    }
    if (showOtpReset) {
        OtpResetDialog(
            email = resetEmail,
            onEmailChange = { resetEmail = it },
            otp = otpCode,
            onOtpChange = { otpCode = it },
            newPassword = newPassword,
            onPasswordChange = { newPassword = it },
            onDismiss = { showOtpReset = false },
            onSendOtp = {
                // Send password reset email
                FirebaseAuth.getInstance().sendPasswordResetEmail(resetEmail)
                resetStatus = "OTP sent to $resetEmail"
            },
            onReset = {
                // Firebase handles password reset via email link, not OTP, but you can guide the user
                resetStatus = "Check your email for the reset link."
                showOtpReset = false
            },
            status = resetStatus
        )
    }
    if (showDeleteAccount) {
        ConfirmDialog(
            title = "Delete Account",
            message = "Are you sure you want to delete your account? This cannot be undone.",
            onDismiss = { showDeleteAccount = false },
            onConfirm = {
                FirebaseAuth.getInstance().currentUser?.delete()
                navController.navigate("sign_in") { popUpTo(0) { inclusive = true } }
            }
        )
    }
}

@Composable
fun EditFieldDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    isPassword: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(title) },
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
            )
        },
        confirmButton = {
            Button(onClick = onSave) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun OtpResetDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    otp: String,
    onOtpChange: (String) -> Unit,
    newPassword: String,
    onPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSendOtp: () -> Unit,
    onReset: () -> Unit,
    status: String?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password via Email Link") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") }
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onSendOtp) { Text("Send Reset Email") }
                Spacer(Modifier.height(8.dp))
                if (status != null) Text(status, color = Color.Green)
            }
        },
        confirmButton = {
            Button(onClick = onReset) { Text("Done") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Delete") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}