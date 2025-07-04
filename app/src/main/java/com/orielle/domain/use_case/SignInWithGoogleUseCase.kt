package com.orielle.domain.use_case

import com.orielle.data.repository.AuthRepository
import javax.inject.Inject

/**
 * A use case that encapsulates the business logic for signing in a user with a Google ID token.
 */
class SignInWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Executes the Google sign-in operation.
     * @param idToken The ID token received from the Google Sign-In flow.
     * @return A Flow that emits the authentication response.
     */
    operator fun invoke(idToken: String) = repository.signInWithGoogle(idToken)
}
