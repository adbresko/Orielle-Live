package com.orielle.domain.manager

import androidx.fragment.app.FragmentActivity

interface BiometricAuthManager {
    fun isBiometricAuthAvailable(): Boolean
    fun promptForBiometricAuth(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onResult: (BiometricResult) -> Unit
    )
}