package com.orielle.domain.model

/**
 * Base exception for authentication-related errors
 */
sealed class AuthException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when an email is already in use during sign-up
 */
class EmailAlreadyInUseException(message: String? = "This email is already registered") : AuthException(message)

/**
 * Exception thrown when a password is too weak
 */
class WeakPasswordException(message: String? = "Password is too weak") : AuthException(message)

/**
 * Exception thrown when user credentials are invalid
 */
class InvalidCredentialsException(message: String? = "Invalid email or password") : AuthException(message)

/**
 * Exception thrown when user account is disabled
 */
class UserDisabledException(message: String? = "This account has been disabled") : AuthException(message)

/**
 * Exception thrown when too many failed login attempts
 */
class TooManyRequestsException(message: String? = "Too many failed attempts. Please try again later") : AuthException(message)

/**
 * Exception thrown when network connectivity issues occur
 */
class NetworkException(message: String? = "Network error. Please check your connection") : AuthException(message)

/**
 * Exception thrown when biometric authentication fails
 */
class BiometricException(message: String? = "Biometric authentication failed") : AuthException(message)

/**
 * Exception thrown when social login fails
 */
class SocialLoginException(message: String? = "Social login failed") : AuthException(message)