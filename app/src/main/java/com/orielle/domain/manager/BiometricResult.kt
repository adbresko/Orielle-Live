package com.orielle.domain.manager

sealed class BiometricResult {
    data object Success : BiometricResult()
    data class Error(val code: Int, val message: String) : BiometricResult()
    data object Failed : BiometricResult()
}